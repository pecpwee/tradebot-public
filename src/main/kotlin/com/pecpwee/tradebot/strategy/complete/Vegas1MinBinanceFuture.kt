package com.pecpwee.tradebot.strategy.complete

import com.binance.client.model.enums.OrderSide
import com.pecpwee.tradebot.service.BinanceFutureServiceImpl
import com.pecpwee.tradebot.strategy.Vegas1Minutes
import org.knowm.xchange.currency.CurrencyPair
import org.springframework.beans.factory.annotation.Autowired

//@Component
class Vegas1MinBinanceFuture : Vegas1Minutes() {

    @Autowired
    lateinit var binanceFuture: BinanceFutureServiceImpl

    override fun toBuy(barId: Int) {
        super.toBuy(barId)

        telegramInfoService.notifyInfo("${this::javaClass} ")

        val theOrder = binanceFuture.postMarketOrder(
            symbol = getBarReuqestConfig().tradePair, side = OrderSide.BUY, quantity = "0.01"
        )
        if (theOrder == null) {
            logger.error("buy failed")
            return
        }
        buyOrderSet.add(theOrder)
    }

    override fun onTickReceived(barId: Int) {
        super.onTickReceived(barId)
        adjustStoploss(barId)
    }

    fun adjustStoploss(barId: Int) {
        if (stoplossOrderSet.size > 0) {
            stoplossOrderSet.onEach {
                binanceFuture.cancelOrder(
                    symbol = getBarReuqestConfig().tradePair,
                    orderId = it.orderId,
                    origClientOrderId = it.clientOrderId
                )
            }
            stoplossOrderSet.clear()
        }
        val theOrder = binanceFuture.postSTOPMarketOrder(
            symbol = CurrencyPair.BTC_USDT,
            side = OrderSide.SELL,
            stopPrice = ema144.getValue(barId).floatValue().toString(),
            quantity = "0.01"
        )

        if (theOrder == null) {
            logger.error("error in stop market order")
            return
        }
        stoplossOrderSet.add(theOrder)

    }
}
