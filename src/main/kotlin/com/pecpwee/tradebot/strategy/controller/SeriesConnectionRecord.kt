package com.pecpwee.tradebot.strategy.controller

import com.binance.api.client.BinanceApiCallback
import com.binance.api.client.BinanceApiWebSocketClient
import com.binance.api.client.domain.event.CandlestickEvent
import com.pecpwee.tradebot.strategy.AbsStrategy
import com.pecpwee.tradebot.utils.addAllKlines
import com.pecpwee.tradebot.utils.convert2TaLibBarSeries
import com.pecpwee.tradebot.utils.toBinanceRawSocketInterval
import com.pecpwee.tradebot.utils.toTALibBar
import okhttp3.internal.closeQuietly
import org.apache.logging.log4j.Logger
import org.knowm.xchange.binance.service.BinanceMarketDataService
import org.ta4j.core.BarSeries
import org.ta4j.core.BaseBarSeries
import java.io.Closeable
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

class SeriesConnectionRecord(
    val barRequestConfig: AbsStrategy.BarRequestConfig,
    val mBinanceSpotRestApi: BinanceMarketDataService,
    val binanceSocketApi: BinanceApiWebSocketClient,
    val isNeedContinuousKlineUpdate: Boolean = false,
    val logger: Logger
) {


    private lateinit var mWebsocketCloseMethod: Closeable
    private var mSeries: BarSeries
    private var hasConnected = false

    private val attachedStrategiesList = CopyOnWriteArrayList<AbsStrategy>()

    init {
        mSeries = BaseBarSeries()
    }

    fun addStrategyListener(newStrategy: AbsStrategy) {
        attachedStrategiesList.add(newStrategy)
    }

    fun removeStrategyListener(strategy: AbsStrategy) {
        attachedStrategiesList.remove(strategy)
    }

    fun getAttacheStrategyNum(): Int {
        return attachedStrategiesList.size
    }

    @Synchronized
    fun destroyConnection() {
        mWebsocketCloseMethod.closeQuietly()
        hasConnected = false
    }

    fun getSeries(): BarSeries {
        return mSeries
    }

    @Synchronized
    fun retryConnection() {
        destroyConnection()
        makeConnection()
    }


    @Synchronized
    fun checkHealthAndReconnectAuto() {
        if (!hasConnected) {
            return
        }
        if (System.currentTimeMillis() - mSeries.lastBar.endTime.toInstant()
                .toEpochMilli() > barRequestConfig.timePeriod.toMillis() / 2
        ) {
            logger.error("has found websocket delay! lastbar:${mSeries.lastBar.endTime},retry connect auto")
            retryConnection()
        }
    }

    @Synchronized
    fun makeConnection(): Boolean {
        if (hasConnected) {
            return true
        }
        hasConnected = true
        val candlelist = mBinanceSpotRestApi.klines(
            barRequestConfig.tradePair,
            barRequestConfig.candlestickInterval,
            barRequestConfig.minKeepSeriesDataNum,
            null,
            null
        )
        //重置，清空一次bar
        mSeries.maximumBarCount = 1
        mSeries.maximumBarCount = barRequestConfig.minKeepSeriesDataNum
        mSeries.addAllKlines(candlelist, barRequestConfig.timePeriod)
//        mSeries = candlelist.convert2TaLibBarSeries(barRequestConfig.timePeriod);

        //如果不需要持续的K线更新，直接返回（一般处于离线更新模式是这样）
        if (!isNeedContinuousKlineUpdate) {
            return true
        }

        mWebsocketCloseMethod = binanceSocketApi.onCandlestickEvent(
            barRequestConfig.tradePair.toString().replace("/", "").lowercase(Locale.getDefault()),
            barRequestConfig.candlestickInterval.toBinanceRawSocketInterval(),
            object : BinanceApiCallback<CandlestickEvent> {
                override fun onFailure(cause: Throwable?) {
                    logger.info("websocket onfailure")
                    logger.error(cause)
                    retryConnection()
                }

                override fun onResponse(response: CandlestickEvent?) {
                    if (response == null) {
                        logger.info("websocket response null")
                        return
                    }
                    val currentBar = response.toTALibBar(barRequestConfig.timePeriod)
                    logger.info("timeInterval:${barRequestConfig.candlestickInterval.millis},\nthe new websocket bar is:$currentBar")

                    val isOldBar = mSeries.lastBar.inPeriod(currentBar.beginTime)
                    if (isOldBar) {
                        logger.debug("not new bar,just replace")
                        mSeries.addBar(currentBar, true)
                    } else {
                        logger.info("it's new bar,add to series")
                        mSeries.addBar(currentBar, false)
                    }
                    attachedStrategiesList.forEach {
                        try {
                            logger.info("checkstrategy ${it.getStrategyName()}")
                            it.checkBuyOrSell(currentBar, isOldBar)
                        } catch (t: Throwable) {
                            logger.error(t)
                        }
                    }
                }
            })
        return true
    }

    fun checkHealth() {
    }


}