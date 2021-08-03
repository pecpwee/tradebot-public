package com.pecpwee.tradebot.vpvr

import com.binance.api.client.domain.market.CandlestickInterval
import com.pecpwee.tradebot.repository.TradePair
import com.pecpwee.tradebot.utils.getAvailableNumberMagnitude
import com.pecpwee.tradebot.utils.getShiftedAvailableNumberInLong
import com.pecpwee.tradebot.utils.toDateStr
import com.pecpwee.tradebot.utils.toMinutesCount
import org.apache.logging.log4j.Logger
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.ui.ApplicationFrame
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import org.jfree.ui.RefineryUtilities
import org.knowm.xchange.binance.dto.marketdata.KlineInterval
import org.knowm.xchange.binance.service.BinanceMarketDataService
import org.knowm.xchange.currency.CurrencyPair
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.awt.Dimension
import java.math.BigDecimal
import java.util.*


@Service
class VPVRCalculatorService {
    @Autowired
    private lateinit var logger: Logger

    @Autowired
    private lateinit var binanceApi: BinanceMarketDataService


    fun calculateVpvr(
        tradePair: CurrencyPair,
        barTimeInterval: KlineInterval = KlineInterval.m1,
        startTime: Long,
        endTime: Long
    )
            : PriceVolResult {

        val intervalMinutesCount = barTimeInterval.toMinutesCount()
        var totalRequestCount = 0


        val totalIntervalMinutesCount = (endTime - startTime) / 1000 / 60 //总分钟数
        val countOfBarInterval = totalIntervalMinutesCount / intervalMinutesCount
        totalRequestCount = Math.floor(countOfBarInterval.toDouble() / 1000).toInt()


        val requestTimeInterval = (0..totalRequestCount).mapIndexed { index, value ->
            val countOfMinutes = 1000 * barTimeInterval.toMinutesCount()
            val theIntervalLeftTime = startTime + index * countOfMinutes * 60 * 1000L
            //处理末尾时间的情况
            val theIntervalRightTime = if (totalRequestCount == index) endTime else
                (startTime + (index + 1) * countOfMinutes * 60 * 1000L)

            Pair(theIntervalLeftTime, theIntervalRightTime)
        }.toMutableList()


        val totalCandleStickList = requestTimeInterval.map {
            binanceApi.klines(tradePair, barTimeInterval, 1000, it.first, it.second)
        }.flatMap {//每个list元素取出来，打平
            it
        }.toList()


//        logger.info("candle info is $candlelist")
        val volMap = TreeMap<String, Float>()
        totalCandleStickList.forEach {
            val preciesNumber = 4
            val low = it.lowPrice.toDouble().getShiftedAvailableNumberInLong(preciesNumber)
            val high = it.highPrice.toDouble().getShiftedAvailableNumberInLong(preciesNumber)
            val magnitude = it.highPrice.toDouble().getAvailableNumberMagnitude(preciesNumber)
            for (shiftedPrice in low..high) {
                val the3AvailNumPrice =
                    BigDecimal(shiftedPrice / magnitude).setScale(3, BigDecimal.ROUND_HALF_UP).toString()


                if (!volMap.containsKey(the3AvailNumPrice)) {
                    volMap.put(the3AvailNumPrice, 0.0f)
                }
                val origVol = volMap.get(the3AvailNumPrice)
                if (origVol != null) {
                    volMap.put(the3AvailNumPrice, origVol + it.volume.toFloat())
                }
            }
        }
        if (totalCandleStickList.size > 0) {
            return PriceVolResult(totalCandleStickList.first().openTime, totalCandleStickList.last().closeTime, volMap)
        } else {
            return PriceVolResult(startTime, endTime, volMap)
        }
    }


    private fun showInWindows(priceVolResult: PriceVolResult) {

        //create map
        val dataset = createDataset(priceVolResult.volPriceMap)
        val chart = ChartFactory.createXYLineChart(
            "vpvr ${priceVolResult.startTime.toDateStr()}-${priceVolResult.endTime.toDateStr()}",  //图表标题
            "price",  //目录轴的显示标签
            "vol",  //数值轴的显示标签
            dataset,  //数据集
            PlotOrientation.VERTICAL,  //图表方向 HORIZONTAL(水平的)
            true,  //是否显示图例，对于简单的柱状图必须为false
            true,  //是否生成提示工具
            false
        ) //是否生成url链接
        // combinedChartPanel to contain combinedChart

//        val domainAxis = chart.categoryPlot.domainAxis
//        domainAxis.setMaximumCategoryLabelWidthRatio(0.25f)

        // Application frame
        val frame = ApplicationFrame("VPVR")
        val panel = ChartPanel(chart)
        panel.fillZoomRectangle = true
//        panel.setMouseWheelEnabled(true)
        panel.preferredSize = Dimension(1024, 800)
        panel.setMouseZoomable(true)


        frame.contentPane = panel
        frame.pack()
        RefineryUtilities.centerFrameOnScreen(frame)
        frame.isVisible = true

    }

    private fun createDataset(volMap: TreeMap<String, Float>): XYSeriesCollection? {
//        val dataset = XYSeriesCollection()
        val mCollection = XYSeriesCollection()
        val mSeriesFirst = XYSeries("First")


        for ((price, vol) in volMap) {
            mSeriesFirst.add(price.toDouble(), vol)
            logger.info("price:$price vol:$vol")
            //key
        }
        mCollection.addSeries(mSeriesFirst)
        return mCollection
    }

    data class PriceVolResult(val startTime: Long, val endTime: Long, val volPriceMap: TreeMap<String, Float>)


//    @Scheduled(initialDelay = 500, fixedDelay = 5000000)
//    fun main() {
//        val startTime = "2020-11-07 00:00:00".fromDateStrToTimestamp()
//        val endTime = System.currentTimeMillis()
//        val volPrice = calculateVpvr(TradePair.BTCUSDT, CandlestickInterval.ONE_MINUTE, startTime, endTime);
//        showInWindows(volPrice);
//    }
}
