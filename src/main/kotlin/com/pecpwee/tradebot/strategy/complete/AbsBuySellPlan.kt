package com.pecpwee.tradebot.strategy.complete

import com.pecpwee.tradebot.service.BinanceFutureServiceImpl
import com.pecpwee.tradebot.strategy.AbsStrategy
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired


abstract class AbsBuySellPlan(val barRequestConfig: AbsStrategy.BarRequestConfig) {

    @Autowired
    protected lateinit var logger: Logger


    @Autowired
    protected lateinit var binanceFuture: BinanceFutureServiceImpl


    abstract fun buy()
    abstract fun sell()

    abstract fun onTickReceived(barId: Int)
}
