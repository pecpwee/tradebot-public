package com.pecpwee.tradebot.strategy.analysis

import org.apache.logging.log4j.Logger
import org.knowm.xchange.binance.BinanceExchange
import org.knowm.xchange.binance.service.BinanceMarketDataService
import org.springframework.beans.factory.annotation.Autowired
import javax.annotation.PostConstruct

abstract class AbsSelectCoin {
    @Autowired
    lateinit var binanceExchange: BinanceExchange

    @Autowired
    lateinit var binanceMarketDataService: BinanceMarketDataService


    @Autowired
    lateinit var logger: Logger

    @PostConstruct
    abstract fun afterInit()


}