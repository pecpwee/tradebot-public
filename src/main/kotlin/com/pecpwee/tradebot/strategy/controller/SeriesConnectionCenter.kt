package com.pecpwee.tradebot.strategy.controller

import com.binance.api.client.BinanceApiWebSocketClient
import com.pecpwee.tradebot.configuration.TradebotApplicationConfig
import com.pecpwee.tradebot.strategy.AbsStrategy
import org.apache.logging.log4j.Logger
import org.knowm.xchange.binance.service.BinanceMarketDataService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.ta4j.core.BarSeries
import java.lang.RuntimeException

/**
 * 引用计数形式的series websocket连接管理和复用
 * */
@Component
class SeriesConnectionCenter {
    @Autowired
    private lateinit var mBinanceSpotRestApi: BinanceMarketDataService

    @Autowired
    private lateinit var mBinanceSocketApi: BinanceApiWebSocketClient

    @Autowired
    private lateinit var tradebotApplicationConfig: TradebotApplicationConfig


    @Autowired
    private lateinit var logger: Logger
    private val mSeriesRecordMap = hashMapOf<String, SeriesConnectionRecord>()


    private fun buildKey(barRequestConfig: AbsStrategy.BarRequestConfig): String {
        return barRequestConfig.tradePair.toString() + barRequestConfig.candlestickInterval.toString()
    }

    @Synchronized
    fun prepareConnection(strategy: AbsStrategy): BarSeries {

        val key = buildKey(strategy.getBarReuqestConfig())
        val existedSeriesRecord = mSeriesRecordMap.get(key)

        //连接还活着，同时合规
        if (existedSeriesRecord != null) {
            //所保留的最小series值不一致，不合理，直接destory
            if (existedSeriesRecord.barRequestConfig.minKeepSeriesDataNum
                >= strategy.getBarReuqestConfig().minKeepSeriesDataNum
            ) {
                existedSeriesRecord.addStrategyListener(strategy)
                return existedSeriesRecord.getSeries()
            } else {
                existedSeriesRecord.destroyConnection()
                mSeriesRecordMap.remove(key)
            }
        }

        //清理干净了，到这里都是得新建连接的
        val connectionRecord =
            SeriesConnectionRecord(
                barRequestConfig = strategy.getBarReuqestConfig(),
                mBinanceSpotRestApi = mBinanceSpotRestApi,
                binanceSocketApi = mBinanceSocketApi,
                isNeedContinuousKlineUpdate = !tradebotApplicationConfig.analysisModel,
                logger = logger
            )
        mSeriesRecordMap.put(key, connectionRecord)
        connectionRecord.addStrategyListener(strategy)
        return connectionRecord.getSeries()
    }

    fun startRealConnection(strategy: AbsStrategy): Boolean {
        val key = buildKey(strategy.getBarReuqestConfig())
        val seriesConnection = mSeriesRecordMap.get(key)
        if (seriesConnection == null) {
            throw RuntimeException("please init connection first")
        }
        return seriesConnection.makeConnection()
    }

    @Scheduled(initialDelay = 1000, fixedDelay = 60 * 1000)
    fun checkConnectHealth() {
        logger.info("check connect health")
        mSeriesRecordMap.values.forEach {
            it.checkHealthAndReconnectAuto()
        }

    }

    @Synchronized
    fun stopSeriesUpdate(strategy: AbsStrategy) {
        val key = buildKey(strategy.getBarReuqestConfig())
        val existConnectionRecord = mSeriesRecordMap.get(key)
        existConnectionRecord?.destroyConnection()
    }

    @Synchronized
    fun removeStrategyFromConnection(strategy: AbsStrategy) {
        strategy.unInstallBarSeries()
        val key = buildKey(strategy.getBarReuqestConfig())
        val existConnectionRecord = mSeriesRecordMap.get(key)

        if (existConnectionRecord != null) {
            existConnectionRecord.removeStrategyListener(strategy)
        }
        if (existConnectionRecord?.getAttacheStrategyNum() == 0) {
            existConnectionRecord.destroyConnection()
            mSeriesRecordMap.remove(key)
        }

    }
}


