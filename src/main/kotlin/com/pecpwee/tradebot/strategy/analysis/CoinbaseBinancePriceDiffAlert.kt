package com.pecpwee.tradebot.strategy.analysis

import com.pecpwee.tradebot.queue.DelayMessage
import com.pecpwee.tradebot.queue.DelayedMessageQueueService
import com.pecpwee.tradebot.utils.*
import org.apache.logging.log4j.Logger
import org.knowm.xchange.binance.dto.marketdata.BinanceKline
import org.knowm.xchange.binance.dto.marketdata.KlineInterval
import org.knowm.xchange.binance.service.BinanceMarketDataService
import org.knowm.xchange.coinbasepro.dto.marketdata.CoinbaseProCandle
import org.knowm.xchange.coinbasepro.service.CoinbaseProMarketDataService
import org.knowm.xchange.currency.CurrencyPair
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.generics.TelegramBot
import java.lang.IllegalArgumentException
import java.util.*


/**
 * test result as followed:
 * startTime :Mon Jun 07 16:35:07 CST 2021
 * endTime: Mon Jun 07 16:59:07 CST 2021
 * coinbaseProK: 16:40 -16:55 ,4Bar
 * binanceK: 16:40 -16:55  ,4Bar
 *
 *
 * close price不太稳定，经常出现binance价格高于coinbase的情况。艹
 */

@Component
class CoinbaseBinancePriceDiffAlert {


    private val TAG = this::class.java.simpleName

    @Autowired
    lateinit var coinbaseProMarketDataService: CoinbaseProMarketDataService

    @Autowired
    lateinit var binanceProMarketDataService: BinanceMarketDataService

    @Autowired
    lateinit var delayedMessageQueueService: DelayedMessageQueueService

    @Autowired
    lateinit var telegramBot: TelegramBot

    @Autowired
    lateinit var logger: Logger

//    @PostConstruct
    fun init() {
        tick()
    }

    fun tick() {
        val period = KlineInterval.m1
        delayedMessageQueueService.putTask(
            delayMessage = DelayMessage(period.millis) {
                logger.info("coinbase price diff tick")
                scheduledRun()

                tick()
            }
        )
    }


    fun scheduledRun() {


        val pairlist = getCoinDiff(
            currencyPair = CurrencyPair.BTC_USDT,
            startTime = (System.currentTimeMillis() - 2 * KlineInterval.m1.millis - 100).toDate(),
            endTime = (System.currentTimeMillis()).toDate(),
            klineInterval = KlineInterval.m1,
        )


        val theNegativePriceDiffBarList = pairlist.map {
            val coinbaseKBar = it.first
            val binanceKBar = it.second

            if (coinbaseKBar == null) {
                null
            } else {
                if (binanceKBar.closePrice.compareTo(coinbaseKBar.close) > 0) {
                    it //收集小于0的情况！
                } else {
                    null
                }
            }

        }.toMutableList()

        if (theNegativePriceDiffBarList.size == 0) {
            logger.error("$TAG pairlist 0")
            return
        }

        theNegativePriceDiffBarList.forEach {
            if (it != null) {
                logger.info(
                    "$TAG detected abnormal Negative premium: time:${it.second.openTime.toDateStr()} " +
                            "coinbasePrice:${it.first?.close} " +
                            "binancePrice:${it.second.closePrice}"
                )
            }else{

            }
        }

    }

    /**
     * because last k line may not closed,needTrimLatestKbar direct that if we need to remove the lastone
     */
    fun getCoinDiff(
        currencyPair: CurrencyPair,
        startTime: Date,
        endTime: Date,
        klineInterval: KlineInterval,
    ): List<Pair<CoinbaseProCandle?, BinanceKline>> {
        //{60, 300, 900, 3600, 21600, 86400}

        if (!isValid(klineInterval)) {
            throw IllegalArgumentException("not valid klines")
        }

        //coinbasePro 服务有问题，中间可能缺失一部分bar
        val coinabseProKlines: MutableList<CoinbaseProCandle?> =
            coinbaseProMarketDataService.getCoinbaseProHistoricalCandles(
                currencyPair,
                startTime.convertToISO8601Str(),
                endTime.convertToISO8601Str(),
                (klineInterval.toMinutesCount() * 60).toString()
            ).toMutableList().let {
                it.reverse() //coinbase k线 0位置是最晚时间，和binance相反，所以要reverse一下
                it
            }


        val binanceKlines =
            binanceProMarketDataService.klines(currencyPair, klineInterval, 300, startTime.time, endTime.time)
                .filterNotNull()

        for (i in 0..binanceKlines.size - 1) {
            val binanceKTime = binanceKlines.get(i).openTime
            if (i >= coinabseProKlines.size) {
                coinabseProKlines.add(null)
            }

            val coinbaseKline = coinabseProKlines.get(i)
            if (coinbaseKline != null) {
                val coinbaseKTime = coinbaseKline.time.toInstant().toEpochMilli()
                if (!binanceKTime.equals(coinbaseKTime)) {
                    coinabseProKlines.add(i, null) //在不协调位置插入null
                    println("fix coinbase time lost:${binanceKTime.toDateStr()}")
                }

            }
        }

//        val theTimeNotSameList = coinabseProKlines.zip(binanceKlines).map {
//            if (!it.first.time.toInstant().toEpochMilli().equals(it.second.openTime)) {
//                it
//            } else {
//                null
//            }
//        }.filterNotNull().toList()
//        println(theTimeNotSameList.toPrettyJson())

        return coinabseProKlines.zip(binanceKlines)
    }


    fun isValid(klineInterval: KlineInterval): Boolean {
        if (klineInterval.code().equals(KlineInterval.m1.code())) {
            return true
        }
        if (klineInterval.code().equals(KlineInterval.m5.code())) {
            return true
        }
        if (klineInterval.code().equals(KlineInterval.m15.code())) {
            return true
        }
        if (klineInterval.code().equals(KlineInterval.h1.code())) {
            return true
        }
        return false
    }

}