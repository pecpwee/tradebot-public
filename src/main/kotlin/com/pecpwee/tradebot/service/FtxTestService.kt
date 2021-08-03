package com.pecpwee.tradebot.service

import io.contek.invoker.commons.websocket.ConsumerState
import io.contek.invoker.commons.websocket.ISubscribingConsumer
import io.contek.invoker.commons.websocket.SubscriptionState
import io.contek.invoker.ftx.api.ApiFactory
import io.contek.invoker.ftx.api.common._Market
import io.contek.invoker.ftx.api.websocket.market.TradesChannel
import org.apache.logging.log4j.Logger
import org.knowm.xchange.currency.CurrencyPair
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class FtxTestService {

    @Autowired
    lateinit var logger: Logger

    companion object {
        val TSLA_USD = CurrencyPair("TSLA/USD")
        val BTC_USD = CurrencyPair.BTC_USD
    }

//    @PostConstruct
    fun init() {
//        getCandles(TSLA_USD, 15)
        tickerWebsocket()
}

    /**
     * Get historical prices
    Supports pagination
    Historical prices of expired futures can be retrieved with this end point but make sure to specify start time and end time.
     * Parameters

    Name	Type	Value	Description
    market_name	string	BTC-0628	name of the market
    resolution	number	300	window length in seconds. options: 15, 60, 300, 900, 3600, 14400, 86400
    start_time	number	1559881511	optional
    end_time	number	1559881711	optional
     *
     *
     * 默认返回1500条记录(如果不给出时间范围的话
     *
     *
     *
     * */
    fun getCandles(
        currencyPair: CurrencyPair,
        intervalInSeconds: Long,
        startTime: Long? = null,
        endTime: Long? = null
    ): List<_Market> {

        val markethandle = ApiFactory.getMainNetDefault().rest().market().marketsCandles
        markethandle.setMarketName("BTC-PERP")
        markethandle.setResolution(15)
        if (startTime != null) {
            markethandle.setStartTime(startTime)
        }
        if (endTime != null) {
            markethandle.setEndTime(endTime)
        }
        val result = markethandle.submit()
        println(result)

        return result.result
    }


    fun tickerWebsocket(
//        currencyPair: CurrencyPair,
    ) {

        val marketconnect = ApiFactory.getMainNetDefault().ws().market().getTradesChannel("BTC-PERP")
        marketconnect.addConsumer(object : ISubscribingConsumer<TradesChannel.Message> {
            override fun onNext(t: TradesChannel.Message) {
//                logger.info("onNext ${t.data.size} ${t.data}")
                t.data.forEach {
                    logger.info("$it")
                }
            }

            override fun getState(): ConsumerState {
                return ConsumerState.ACTIVE
            }

            override fun onStateChange(state: SubscriptionState) {
            }

        })

    }
}