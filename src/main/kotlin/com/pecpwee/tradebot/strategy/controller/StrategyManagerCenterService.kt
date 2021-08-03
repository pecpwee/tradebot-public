package com.pecpwee.tradebot.strategy.controller

import com.pecpwee.tradebot.configuration.TradebotApplicationConfig
import com.pecpwee.tradebot.strategy.AbsStrategy
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct


@Service
class StrategyManagerCenterService {
    @Autowired
    private lateinit var strategies: List<AbsStrategy>

    @Autowired
    private lateinit var seriesConnectionCenter: SeriesConnectionCenter

    @Autowired
    protected lateinit var logger: Logger

    @Autowired
    lateinit var tradebotApplicationConfig: TradebotApplicationConfig

    //测试用
    @PostConstruct
    fun init() {
        if (tradebotApplicationConfig.runTradeAutoWhenAppStart) {
            logger.info("config runTradeAutoWhenAppStart enable,will auto start strategy!")
            start()
        } else {
            logger.info("config runTradeAutoWhenAppStart Not enable,NO auto start strategy!")

        }
    }

    fun start(strategies: List<AbsStrategy> = queryStrategies(null)) {
        strategies.forEach {
            val barSeries = seriesConnectionCenter.prepareConnection(it)
            it.installBarSeries(barSeries)

            val isOK = seriesConnectionCenter.startRealConnection(it)
            logger.info("seriesCenter.startRealConnection isok ${isOK}")

            if (it.getBarReuqestConfig().isNeedBacktest || tradebotApplicationConfig.analysisModel) {
                it.runBacktest()
            }
        }
    }

    fun queryStrategies(strategyName: String?): List<AbsStrategy> {
        if (strategyName == null) {
            return strategies
        }
        val list = strategies.filter {
            it.getStrategyName().equals(strategyName)
        }.toList()
        return list
    }


    fun stop(strategies: List<AbsStrategy> = queryStrategies(null)) {
        strategies.forEach {
            seriesConnectionCenter.stopSeriesUpdate(it)
            seriesConnectionCenter.removeStrategyFromConnection(it)
        }

    }

    fun destroyConn(strategies: List<AbsStrategy> = queryStrategies(null)) {
        strategies.forEach {
            seriesConnectionCenter.removeStrategyFromConnection(it)
        }
    }

}