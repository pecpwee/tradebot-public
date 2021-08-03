package com.pecpwee.tradebot.strategy.analysis

import com.pecpwee.tradebot.bean.XChangeConfig
import info.bitrich.xchangestream.binance.BinanceStreamingExchange
import info.bitrich.xchangestream.binance.BinanceSubscriptionType
import info.bitrich.xchangestream.core.ProductSubscription
import io.reactivex.disposables.Disposable
import org.knowm.xchange.binance.dto.marketdata.BinanceKline
import org.knowm.xchange.binance.dto.marketdata.KlineInterval
import org.knowm.xchange.binance.service.BinanceAccountService
import org.knowm.xchange.binance.service.BinanceMarketDataService
import org.knowm.xchange.binance.service.BinanceTradeService
import org.knowm.xchange.currency.CurrencyPair
import org.knowm.xchange.dto.marketdata.OrderBook
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*
import javax.annotation.PostConstruct

@Service
@Lazy
class BinanceSpotAnalysisService {


    @Autowired
    lateinit var binanceAccountService: BinanceAccountService

    @Autowired
    lateinit var binanceTradeService: BinanceTradeService

    @Autowired
    lateinit var binanceMarketDataService: BinanceMarketDataService


    @Autowired
    lateinit var binanceStreamingExchange: BinanceStreamingExchange


    @PostConstruct
    fun postOrder() {
//        binanceTradeService.getOpenOrders()
    }


    @Autowired
    lateinit var allPairsList: XChangeConfig.AllCurrencyPairs

    /**
     * https://binance-docs.github.io/apidocs/spot/cn/#c59e471e81
     * 最大1000
     *
     *
     * */
    fun getAllBinnaceKlines(
        tradePairs: List<CurrencyPair> = allPairsList.content,
        startTime: Long,
        endTime: Long = System.currentTimeMillis(),
        binanceInterval: KlineInterval = KlineInterval.h1,
    ): List<List<BinanceKline>> {

        val tradePairs = allPairsList.content

//        val endTime = "2021-05-12 09:04:59".fromDateStrToTimestamp()
        val endTime = System.currentTimeMillis()

        val data = tradePairs.map {
            try {
                val klines = getOneKlinesWithAnyTimeLength(
                    tradePair = it, startTime = startTime, endTime = endTime, barTimeInterval = binanceInterval
                )
                binanceMarketDataService.klines(
                    it,
                    binanceInterval,
                    1000,
                    startTime,
                    endTime,
                )
                klines
            } catch (t: Throwable) {
                null
            }
        }.filterNotNull().toList()

        return data
    }


    //    @PostConstruct
    fun orderBookStream() {
        val assets = binanceAccountService.assetDetails
        println(binanceAccountService.accountInfo)
        // First, we subscribe only for one currency pair at connection time (minimum requirement)
        val subscription = ProductSubscription.create()
            .addTrades(CurrencyPair.BTC_USDT)
            .addOrderbook(CurrencyPair.BTC_USDT)
            .build()
        // Note: at connection time, the live subscription is disabled
        binanceStreamingExchange.connect(subscription).blockingAwait()

        val orderBooksBtc: Disposable = binanceStreamingExchange
            .streamingMarketDataService
            .getOrderBook(CurrencyPair.BTC_USDT)
            .doOnDispose({
                binanceStreamingExchange
                    .streamingMarketDataService
                    .unsubscribe(CurrencyPair.BTC_USDT, BinanceSubscriptionType.DEPTH)
            })
            .subscribe({ orderBook: OrderBook? ->
                orderBook

            })
    }

    fun printBooks(orderBook: OrderBook) {

        orderBook.asks.sortByDescending { it.limitPrice }
        val treeMap = TreeMap<BigDecimal, BigDecimal>()

    }

    fun getOneKlinesWithAnyTimeLength(
        tradePair: CurrencyPair,
        startTime: Long,
        endTime: Long,
        barTimeInterval: KlineInterval
    ): List<BinanceKline> {
        val intervalMinutesCount = barTimeInterval.millis / 1000 / 60
        var totalRequestCount = 0


        val totalIntervalMinutesCount = (endTime - startTime) / 1000 / 60 //间隔分钟数

        //总分钟数除以一个间隔有几分钟，等于有几个间隔
        val countOfBarInterval = totalIntervalMinutesCount / intervalMinutesCount

        //计算请求次数
        totalRequestCount = Math.floor(countOfBarInterval.toDouble() / 1000).toInt()


        val requestTimeInterval = (0..totalRequestCount).mapIndexed { index, value ->
            //计算1000个bar之后的时间，单位是分钟
            val countOfMinutes = 1000 * intervalMinutesCount
            val theIntervalLeftTime = startTime + index * countOfMinutes * 60 * 1000L
            //处理末尾时间的情况
            val theIntervalRightTime = if (totalRequestCount == index) endTime else
                (startTime + (index + 1) * countOfMinutes * 60 * 1000L)
            Pair(theIntervalLeftTime, theIntervalRightTime)
        }.toMutableList()


        val totalCandleStickList = requestTimeInterval.map {
            binanceMarketDataService.klines(tradePair, barTimeInterval, 1000, it.first, it.second)
        }.flatMap {//每个list元素取出来，打平
            it
        }.filterNotNull().toList()
        return totalCandleStickList


    }
}