package com.pecpwee.tradebot.strategy.analysis

import org.knowm.xchange.binance.dto.marketdata.BinanceKline
import org.knowm.xchange.currency.CurrencyPair
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.lang.RuntimeException
import java.math.BigDecimal
import java.time.Duration
import javax.annotation.PostConstruct

/*
*
* */

@Component
class DeclineAndRecoverRank : AbsSelectCoin() {


//    val coinbaseExchage = ExchangeFactory.INSTANCE.createExchange(CoinbaseProExchange::class.java);

    @Autowired
    lateinit var binanceSpotService: BinanceSpotAnalysisService

    @PostConstruct
    fun init() {
    }

    fun getRanks(
        startTime: Long = System.currentTimeMillis() - Duration.ofDays(1).toMillis(),
        endTime: Long = System.currentTimeMillis()
    ): DeclineRecoverResult {
        val binance = binanceMarketDataService
        val tradePairs = binanceSpotService.allPairsList.content
//        val endTime = "2021-05-12 09:04:59".fromDateStrToTimestamp()

        val klineList = binanceSpotService.getAllBinnaceKlines(startTime = startTime, endTime = endTime)
        val data = klineList.map {
            val klines = it
            val theLowestKBar = klines.minByOrNull { it.lowPrice.toDouble() } ?: klines.first()
            val theLowestPrice = theLowestKBar
            val theLowestIndex = klines.indexOfFirst { it.equals(theLowestKBar) }
            val theMaxPrevious =
                klines.subList(0, theLowestIndex).maxByOrNull { it.highPrice.toDouble() }
                    ?: theLowestPrice
            val theMaxAfters =
                klines.subList(theLowestIndex, klines.size)
                    .maxByOrNull { it.highPrice.toDouble() }
                    ?: theLowestPrice

            if (theLowestPrice != null && theMaxPrevious != null && theMaxAfters != null) {
                LowPercentData(

                    pairName = it.first().currencyPair,
                    preHigh = theMaxPrevious,
                    lowerestPrice = theLowestPrice,
                    maxRecoveredHighestPrice = theMaxAfters,
                    percentDeclined = (theMaxPrevious.highPrice.minus(theLowestPrice.lowPrice))
                        .divide(theMaxPrevious.highPrice, 2, BigDecimal.ROUND_HALF_UP).toDouble(),
                    percentMaxRecovery = (theMaxAfters.highPrice.minus(theLowestPrice.lowPrice)).divide(
                        (theMaxPrevious.highPrice.minus(theLowestPrice.lowPrice)),
                        2,
                        BigDecimal.ROUND_HALF_UP
                    ).toDouble(),
                    percentLatestRecovery = (klines.last().closePrice.minus(theLowestPrice.lowPrice)).divide(
                        (theMaxPrevious.highPrice.minus(theLowestPrice.lowPrice)),
                        2,
                        BigDecimal.ROUND_HALF_UP
                    ).toDouble(),
                    latestKline = klines.last(),

                    )
            } else {
                throw RuntimeException("error")
            }
        }.toList()




        println("\npercent declined")

        val declineRanks = data.sortedByDescending {
            it.percentDeclined
        }


        println(declineRanks)

        println("percent latest recovery")
        val recoverRank = data.sortedByDescending {
            it.percentLatestRecovery
        }
        println(recoverRank)


        return DeclineRecoverResult(declineRanks, recoverRank)
//        return DeclineAndRecoverRank()

//        val xy = DefaultOHLCDataset();
//        showPlot()
    }

    data class DeclineRecoverResult(var declineRank: List<LowPercentData>, var recoverRank: List<LowPercentData>)

    class LowPercentData(
        val pairName: CurrencyPair,
        val preHigh: BinanceKline,
        val lowerestPrice: BinanceKline,
        val maxRecoveredHighestPrice: BinanceKline,
        val latestKline: BinanceKline,
        val percentDeclined: Double,
        val percentMaxRecovery: Double,
        val percentLatestRecovery: Double,
    ) {

        override fun toString(): String {
            return "${pairName} preHigh:${preHigh.highPrice} lowest:${lowerestPrice.lowPrice}," +
                    "afterHigh:${maxRecoveredHighestPrice.highPrice}," +
                    "latest:${latestKline.closePrice}," +
                    "decline%${percentDeclined}," +
                    "increaseMax%$percentMaxRecovery," +
                    "latestPercent$percentLatestRecovery"
        }
    }


    override fun afterInit() {

    }

//    fun main(args: Array<String>) {
//
//
//    }

}