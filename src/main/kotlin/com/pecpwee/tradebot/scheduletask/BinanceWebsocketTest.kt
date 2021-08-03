package com.pecpwee.tradebot.scheduletask

import com.binance.api.client.BinanceApiRestClient
import com.binance.api.client.BinanceApiWebSocketClient
import com.binance.api.client.domain.market.CandlestickInterval
import com.pecpwee.tradebot.telegram.TelegramInfoService
import com.pecpwee.tradebot.utils.toDateStr
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import java.util.*
import javax.annotation.PostConstruct


//@Component
class BinanceWebsocketTest {

    @Autowired
    private lateinit var logger: Logger


    @Autowired
    private lateinit var binanceSocketApi: BinanceApiWebSocketClient

    @Autowired
    private lateinit var binanceRestApi: BinanceApiRestClient

    @Autowired
    private lateinit var telegramInfoService: TelegramInfoService


    private var mDepthInfo: String = ""
    private var mTradeInfo: String = ""

    @PostConstruct
    fun tick() {
//        startCandlestickEventStreaming("ETHUSDT", CandlestickInterval.ONE_MINUTE)
////         Listen for aggregated trade events for ETH/BTC
//
//
//        binanceSocketApi.onDepthEvent("ethusdt", {
//            logger.info("onDepthEvent")
//            logger.info(it)
//            mDepthInfo = it.toString()
//
//        })
//
////        聚合100毫秒交易
//        binanceSocketApi.onAggTradeEvent("ethusdt", {
//            logger.info("onAggTradeEvent ${it.tradeTime.toDateStr()} -> ${it.price} ${it.quantity}")
//            mTradeInfo = ("onAggTradeEvent ${it.tradeTime.toDateStr()} \nprice:${it.price} \nvol:${it.quantity}")
//        })
//
//        binanceSocketApi.onAggTradeEvent("ethusdt", {
//            logger.info("onAggTradeEvent ${it.tradeTime.toDateStr()} -> ${it.price} ${it.quantity}")
//            mTradeInfo = ("onAggTradeEvent ${it.tradeTime.toDateStr()} \nprice:${it.price} \nvol:${it.quantity}")
//        })

//        binanceSocketApi.onTradeEvent("ethusdt", {
//            logger.info("onTrade $it")
//        })
    }

    @Scheduled(initialDelay = 10 * 1000, fixedDelay = 1 * 60 * 1000)
    fun task() {
        telegramInfoService.notifyInfo(mTradeInfo)
    }

    private fun startDepthCache(symbol: String, interval: CandlestickInterval) {
        val depthCache = DepthCacheExample(binanceRestApi, binanceSocketApi, "ethusdt")
        telegramInfoService.notifyInfo(depthCache.toString())
    }


    /**
     * Begins streaming of depth events.
     */
    private fun startCandlestickEventStreaming(symbol: String, interval: CandlestickInterval) {
        binanceSocketApi.onCandlestickEvent(symbol.lowercase(Locale.getDefault()), interval) {
            logger.info("onCandlestickEvent $it")
            logger.info("open ${it.openTime.toDateStr()} close ${it.closeTime.toDateStr()}")

        }
    }


}