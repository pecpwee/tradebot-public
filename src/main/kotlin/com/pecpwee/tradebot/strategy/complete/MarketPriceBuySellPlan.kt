package com.pecpwee.tradebot.strategy.complete

import com.binance.client.model.enums.OrderSide
import com.pecpwee.tradebot.strategy.AbsStrategy
import org.knowm.xchange.currency.CurrencyPair
import org.ta4j.core.indicators.EMAIndicator

class MarketPriceBuySellPlan(
    barRequestConfig: AbsStrategy.BarRequestConfig, val ema144: EMAIndicator
) : AbsBuySellPlan(barRequestConfig) {

    val buyOrderSet = hashSetOf<com.binance.client.model.trade.Order>()
    val stoplossOrderSet = hashSetOf<com.binance.client.model.trade.Order>()

    override fun buy() {
        val theOrder = binanceFuture.postMarketOrder(
            symbol = barRequestConfig.tradePair, side = OrderSide.BUY, quantity = "0.01"
        )
        if (theOrder == null) {
            logger.error("buy failed")
            return
        }
        buyOrderSet.add(theOrder)
    }

    override fun sell() {


    }


    override fun onTickReceived(barId: Int) {
        adjustStoploss(barId)
    }

    fun adjustStoploss(barId: Int) {
        if (stoplossOrderSet.size > 0) {
            stoplossOrderSet.onEach {
                binanceFuture.cancelOrder(
                    symbol = barRequestConfig.tradePair,
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