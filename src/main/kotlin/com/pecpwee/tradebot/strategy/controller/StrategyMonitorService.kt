package com.pecpwee.tradebot.strategy.controller

import com.pecpwee.tradebot.strategy.AbsStrategy
import com.pecpwee.tradebot.strategy.TradeTimeRecord
import com.pecpwee.tradebot.telegram.TelegramInfoService
import com.pecpwee.tradebot.utils.toReadableString
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.net.InetAddress
import javax.annotation.PostConstruct


@Service
class StrategyMonitorService {

    @Autowired
    private lateinit var strategies: List<AbsStrategy>





    //    @Scheduled(initialDelay = 1000, fixedDelay = 60 * 60 * 1000)
    private fun tickToTelegram() {

//        val status = "strategy monitor service:${getRunningStatus()}"
//        logger.info(status)
    }

    @PostConstruct
    fun init(){
    }

    fun getRunningStatus(): AllSysInfo {
        val strategylist = strategies.map {
            if (it.hasAttachedSeries) {
                val lastBar = it.getSeries().lastBar
                StrategyStatus(
                    name = it.getStrategyName(),
                    lastBarStartTime = lastBar.beginTime.toReadableString(),
                    lastBarEndTime = lastBar.endTime.toReadableString(),
                    lastBuyOrSellActions = it.historyTradeRecordQueue.getAll()
                )
            } else {
                StrategyStatus(
                    name = it.getStrategyName(),
                    lastBarStartTime = "",
                    lastBarEndTime = "",
                    lastBuyOrSellActions = arrayListOf()
                )
            }
        }.toList()

        return AllSysInfo(
            OS = getComputerName(),
            strategy = strategylist
        )
    }

    private fun getComputerName(): String? {
        val env = System.getenv()
        return if (env.containsKey("COMPUTERNAME")) env["COMPUTERNAME"] else if (env.containsKey("HOSTNAME")) env["HOSTNAME"] else "Unknown Computer"
    }

    data class AllSysInfo(
        val OS: String?,
        val strategy: List<StrategyStatus>

    )

    data class StrategyStatus(
        val name: String,
        val lastBarStartTime: String,
        val lastBarEndTime: String,
        val lastBuyOrSellActions: List<TradeTimeRecord>
    )


}




