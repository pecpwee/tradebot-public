package com.pecpwee.tradebot.scheduletask

import com.binance.client.SyncRequestClient
import com.binance.client.exception.BinanceApiException
import com.binance.client.model.enums.*
import com.pecpwee.tradebot.queue.DelayMessage
import com.pecpwee.tradebot.queue.DelayedMessageQueueService
import com.pecpwee.tradebot.service.BinanceFutureServiceImpl
import org.apache.logging.log4j.Logger
import org.knowm.xchange.currency.CurrencyPair
import org.springframework.beans.factory.annotation.Autowired

//@Component
//@ConditionalOnProperty(prefix = "binance", name = ["is_testnet"], havingValue = "true")
//@ConditionalOnExpression("'\${binance.is_testnet}'=='true' && \${trade.autotrade.is_enable:true}")
class BinanceFutureTask {

    @Autowired
    private lateinit var delayedMessageQueueService: DelayedMessageQueueService

    companion object {
        val TAG: String = "BinanceFutureTask"
    }

    @Autowired
    private lateinit var logger: Logger

    @Autowired
    private lateinit var binanceFutureApi: SyncRequestClient

    @Autowired
    private lateinit var futureService: BinanceFutureServiceImpl


    //    @Scheduled(initialDelay = 500, fixedDelay = 5000000)
    fun testTrade() {
        delayedMessageQueueService.putTask(DelayMessage(5000, Runnable {
            logger.info("delayedMessageQueueService 5000")
        }))

        logger.info("testme 5000")

        val balance = futureService.getAccountBalance()
        logger.info("the account")
        balance.forEach {
            logger.info(it)
        }
        val accountInfo = futureService.getAccountInfo()
        logger.info(accountInfo)


        try {
            val tradePair = CurrencyPair.BTC_USDT
            val quant = "0.001"
            val price = "21000"
            val result = futureService.cancelAllOrder(tradePair, 1000L, System.currentTimeMillis())

            val theBuyer = futureService.postLimitOrder(tradePair, OrderSide.BUY, quant, "21000")
            val theSTOP = futureService.postSTOPMarketOrder(tradePair, OrderSide.SELL, quant, "20000")
            if (theBuyer != null) {
                logger.info(TAG, theBuyer)
                val orderId = theBuyer.orderId
                val order = futureService.getOrder(tradePair, orderId, null)
                logger.info(TAG, order)
                futureService.cancelOrder(tradePair, orderId, null)
            }

        } catch (t: BinanceApiException) {
            println("$TAG errorType: ${t.message}")
        }
    }

    //    @Scheduled(initialDelay = 1000, fixedDelay = 5000000)
    fun tick() {
        logger.info("binance future:")

        logger.info("binance future force liquid:")
        binanceFutureApi.getLiquidationOrders("BTCUSDT", null, null, 1).apply {
            logger.info(this)
        }


        logger.info("binance 大户账户数多空比")
        binanceFutureApi.getTopTraderAccountRatio("ETHUSDT", PeriodType._5m, null, null, 5).apply {
            logger.info(this)
        }

        logger.info("binance future 大户持仓量多空比")
        binanceFutureApi.getTopTraderPositionRatio("ETHUSDT", PeriodType._5m, null, null, 5).apply {
            logger.info(this)
        }

        logger.info("多空持仓人数比")
        binanceFutureApi.getGlobalAccountRatio("ETHUSDT", PeriodType._5m, null, null, 5).apply {
            logger.info(this)
        }
    }
}