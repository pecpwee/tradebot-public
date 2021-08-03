package com.pecpwee.tradebot.strategy

import com.pecpwee.tradebot.telegram.TelegramInfoService
import com.pecpwee.tradebot.utils.*
import org.apache.logging.log4j.Logger
import org.knowm.xchange.binance.dto.marketdata.KlineInterval
import org.knowm.xchange.currency.CurrencyPair
import org.springframework.beans.factory.annotation.Autowired
import org.ta4j.core.*
import org.ta4j.core.analysis.CashFlow
import org.ta4j.core.analysis.criteria.*
import org.ta4j.core.analysis.criteria.pnl.GrossReturnCriterion
import org.ta4j.core.cost.LinearTransactionCostModel
import org.ta4j.core.cost.ZeroCostModel
import org.ta4j.core.num.Num
import java.time.Duration
import javax.annotation.PreDestroy


abstract class AbsStrategy {

    @Autowired
    protected lateinit var logger: Logger

    @Autowired
    protected lateinit var telegramInfoService: TelegramInfoService

//    @Autowired
//    private lateinit var mBinanceSpotRestApi: ExchangeService;

    //just for logging
    val historyTradeRecordQueue = LimitQueue<TradeTimeRecord>(3)

    private val mStrategyName = this::class.java.simpleName
    protected lateinit var mStrategy: BaseStrategy
    protected lateinit var mBarRequestConfig: BarRequestConfig

    //用于每个Bar只向外通知一次
    private var mLastNotifyBar: Bar? = null

    private lateinit var mSeries: BarSeries

    var hasAttachedSeries = false

    private val sb = StringBuffer()


    abstract fun buildStrategy(series: BarSeries?): BaseStrategy

    abstract fun getBarReuqestConfig(): BarRequestConfig

    open fun getBackTestConfig(barSeries: BarSeries): BackTestConfig {
        return BackTestConfig()
    }

    fun getStrategyName(): String {
        return mStrategyName
    }


    init {
        mBarRequestConfig = getBarReuqestConfig()
    }

    //用于说明一些buy and sell的原因
    fun addStrategyDescCache(sb: String) {
        this.sb.setLength(0)
        this.sb.append(sb)
    }

    //读并清理所有原因
    protected fun readAndClearStrategyDescCache(): String {
        val str = this.sb.toString()
        sb.setLength(0)
        return str
    }

    fun installBarSeries(barSeries: BarSeries) {
        mSeries = barSeries
        mStrategy = buildStrategy(mSeries)
//        checkBuyOrSell(mSeries.lastBar, true)
        hasAttachedSeries = true
    }

    fun unInstallBarSeries() {
//        checkBuyOrSell(mSeries.lastBar, true)
        //针对mStrategy 和mStrategy不做释放，因为下次启动时就自动覆盖了，没必要手动释放，还得该类型，大可不必

        hasAttachedSeries = false
    }


    fun getLastClosedBarId(): Int {
        return mSeries.endIndex - 1
    }

    fun getLatestUnclosedBarFromSeriesCache(): Bar {
        return mSeries.getBar(mSeries.endIndex)
    }


    fun getLastClosedBarFromSeriesCache(): Bar {
        return mSeries.getBar(getLastClosedBarId())
    }

    var lastUnclosedBarNotifyTime = 0L
    var unclosedBarInterval = 60 * 1000L
    var theLastUnclosedNotifiedBar: Bar? = null
    fun checkBuyOrSell(baseBar: Bar, isOldBar: Boolean) {

        val endIndex = mSeries.endIndex
        if (endIndex <= 1) {
            logger.debug("not enough series bar")
            return
        }
        //最后那个unclosedbar更新了，回调下.为了时效性，如果等closedbar到了再通知，感觉有点延迟了？

        onNewUnclosedBarGot(endIndex)
        val latestUnclosedBar = mSeries.getBar(endIndex)
        val lastUnclosedNotifiedBar = theLastUnclosedNotifiedBar
        if (lastUnclosedNotifiedBar == null || //保证unclosedbar不重复应答
            (lastUnclosedNotifiedBar != null
                    && !lastUnclosedNotifiedBar.beginTime.equals(latestUnclosedBar.beginTime))
        ) {
            if (mStrategy.shouldEnter(endIndex)) {
                theLastUnclosedNotifiedBar = latestUnclosedBar
                toBuyUnstabled(endIndex)

            }
            if (mStrategy.shouldExit(endIndex)) {
                theLastUnclosedNotifiedBar = latestUnclosedBar
                toSellUnstabled(endIndex)
            }
        }


        //以下判断针对已经closed的bar！
        //在新的bar到来的时候，才有去检查已经closed的bar的意义
        if (isOldBar) {
            logger.debug("is old bar,just reutrn")
            return
        }

        val theLastNotifiedBar = mLastNotifyBar
        if (theLastNotifiedBar != null) {
            if (theLastNotifiedBar.equals(getLastClosedBarFromSeriesCache())) {
                logger.info("have notified the notification! just return")
                return
            }
        }

        //之所以减去1,因为减1 的index对应的bar才是处于完结状态，才能用于正式判断
        val theClosedBarId = getLastClosedBarId()
        val theLastClosedBar = getLastClosedBarFromSeriesCache()

        if (mStrategy.shouldEnter(theClosedBarId)) {
            historyTradeRecordQueue.offer(
                TradeTimeRecord(
                    isBuy = true,
                    time = theLastClosedBar.beginTime.toReadableString()
                )
            )
            toBuy(endIndex)
            mLastNotifyBar = theLastClosedBar
        }

        if (mStrategy.shouldExit(theClosedBarId)) {
            historyTradeRecordQueue.offer(
                TradeTimeRecord(
                    isBuy = false,
                    time = theLastClosedBar.beginTime.toReadableString()
                )
            )

            toSell(endIndex)
            mLastNotifyBar = theLastClosedBar
        }


    }

    private fun getBarBriefDesc(bar: Bar): String {
        return "${bar.beginTime} ${bar.closePrice}"

    }


    open fun toBuy(barId: Int) {
        val msg =
            "$mStrategyName:BUY,\n" +
                    "${mSeries.getBar(barId).beginTime.toReadableString()},\n" +
                    "reason:${readAndClearStrategyDescCache()}"
        logger.info(msg)
        telegramInfoService.notifyInfo(msg)
    }


    open fun toSell(barId: Int) {
        val msg =
            "$mStrategyName:SELL,\n" +
                    "${mSeries.getBar(barId).beginTime.toReadableString()},\n" +
                    "reason:${readAndClearStrategyDescCache()}"
        logger.info(msg)
        telegramInfoService.notifyInfo(msg)
    }

    open fun toBuyUnstabled(barId: Int) {
        val msg =
            "$mStrategyName:PRE-Buy,\n" +
                    "${mSeries.getBar(barId).beginTime.toReadableString()},\n" +
                    "reason ${readAndClearStrategyDescCache()}"
        logger.info(msg)
        telegramInfoService.notifyInfo(msg)
    }

    open fun toSellUnstabled(barId: Int) {
        val msg =
            "$mStrategyName:PRE-Sell,\n" +
                    "${mSeries.getBar(barId).beginTime.toReadableString()},\n" +
                    "with reason ${readAndClearStrategyDescCache()}"
        logger.info(msg)
        telegramInfoService.notifyInfo(msg)
    }

    /***
     * 每次新bar插入数据时回调
     */
    open fun onNewUnclosedBarGot(barId: Int) {

    }

    /***
     * 每次websocket收到数据时回调
     */
    open fun onTickReceived(barId: Int) {

    }

    fun getSeries(): BarSeries {
        return mSeries
    }

    @PreDestroy
    fun onDestroy() {
        telegramInfoService.notifyInfo("$mStrategyName strategy stop! ${System.currentTimeMillis().toDateStr()}")
    }

    data class BarRequestConfig(
        val tradePair: CurrencyPair,
        val candlestickInterval: KlineInterval,
        val timePeriod: Duration = Duration.ofMinutes(candlestickInterval.toMinutesCount()),
        val minKeepSeriesDataNum: Int = 1000,
        var isNeedBacktest: Boolean = false
    )

    data class BackTestConfig(
        val indicatorsToDraw: List<Indicator<Num>> = arrayListOf(),
    )

    fun runBacktest() {


        // Setting the trading cost models,0.0001 = 0.1% per order,in Binance
        val feePerTrade = 0.00005

        val seriesManager = BarSeriesManager(
            mSeries,
            LinearTransactionCostModel(feePerTrade), ZeroCostModel()
        )
        val tradingRecord = seriesManager.run(mStrategy)
        logger.info("$mStrategyName Number of trades for the strategy: " + tradingRecord.positionCount)
        val endIndex = mSeries.endIndex
        val cashFlow = CashFlow(getSeries(), tradingRecord)

        logger.info("$mStrategyName begin backtest")
        (0..endIndex).forEach {
            if (mStrategy.shouldEnter(it)) {
                logger.info("${mStrategyName} backtest should buy: time ${mSeries.getBar(it)} ")
            } else if (mStrategy.shouldExit(it)) {
                logger.info("${mStrategyName} backtest should sell: ${mSeries.getBar(it)}")
            }
        }

        BuyAndSellSignalsToChart.showWindows(
            mSeries,
            mStrategy,
            "${this.mStrategyName} ${getBarReuqestConfig().timePeriod}",
            getBackTestConfig(mSeries).indicatorsToDraw
        )

        calculateAndPrintBacktestDetail(mSeries, tradingRecord)

        // Getting the winning positions ratio

        // Analysis

        // Getting the winning positions ratio

    }

    fun calculateAndPrintBacktestDetail(series: BarSeries, tradingRecord: TradingRecord) {
        // Total profit

        /*
             * Analysis criteria
             */

        // Total profit
        val totalReturn = GrossReturnCriterion()

        // Number of bars
        // Number of bars
        logger.info(("Total return: " + totalReturn.calculate(series, tradingRecord)))
        logger.info(
            "Average return (per bar): " + AverageReturnPerBarCriterion().calculate(series, tradingRecord)
        )
        // Profitable position ratio
        // Profitable position ratio
        logger.info(
            "Winning positions ratio: " + WinningPositionsRatioCriterion().calculate(series, tradingRecord)
        )
        // Maximum drawdown
        // Maximum drawdown
        logger.info("Maximum drawdown: " + MaximumDrawdownCriterion().calculate(series, tradingRecord))
        // Reward-risk ratio
        logger.info(
            "Return over maximum drawdown: "
                    + ReturnOverMaxDrawdownCriterion().calculate(series, tradingRecord)
        )
        // Total transaction cost
        logger.info(
            ("Total transaction cost (from $1000): "
                    + LinearTransactionCostCriterion(1000.0, 0.005).calculate(series, tradingRecord))
        )
        // Buy-and-hold
        // Buy-and-hold
        logger.info("Buy-and-hold return: " + BuyAndHoldReturnCriterion().calculate(series, tradingRecord))
        // Total profit vs buy-and-hold
        // Total profit vs buy-and-hold
        logger.info(
            ("Custom strategy return vs buy-and-hold strategy return: "
                    + VersusBuyAndHoldCriterion(totalReturn).calculate(series, tradingRecord))
        )

        logger.info("Number of bars: " + NumberOfBarsCriterion().calculate(series, tradingRecord))
        // Number of positions
        logger.info("Number of positions: " + NumberOfPositionsCriterion().calculate(series, tradingRecord))


    }


}


data class TradeTimeRecord(val isBuy: Boolean, val isSell: Boolean = !isBuy, val time: String)

