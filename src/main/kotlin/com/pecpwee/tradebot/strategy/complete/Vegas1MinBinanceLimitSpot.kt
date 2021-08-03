package com.pecpwee.tradebot.strategy.complete

import com.pecpwee.tradebot.strategy.Vegas1Minutes
import org.knowm.xchange.binance.dto.trade.TimeInForce
import org.knowm.xchange.binance.service.BinanceAccountService
import org.knowm.xchange.binance.service.BinanceTradeService
import org.knowm.xchange.currency.Currency
import org.knowm.xchange.currency.CurrencyPair
import org.knowm.xchange.dto.Order
import org.knowm.xchange.dto.trade.LimitOrder
import org.knowm.xchange.dto.trade.StopOrder
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import javax.annotation.PostConstruct


/***
 * 通过价格识别是否属于本订单
 *
 */
//@Component
class Vegas1MinBinanceLimitSpot : Vegas1Minutes() {


//    @Autowired
//    lateinit var binanceSpot: BinanceSpotServiceImpl;

    companion object {
        val isTest = true
        val tradeAmount = "0.001"
        val priceMarkThatBelongTheSystem = "0.93"
    }

    @Autowired
    lateinit var binanceTradeService: BinanceTradeService

    @Autowired
    lateinit var binanceAccountService: BinanceAccountService

//    @Scheduled(initialDelay = 1, fixedDelay = 5000000)

    var mBuyOrderId: String? = null
    var mStopOrderId: String? = null


    //config设定
    override fun getBarReuqestConfig(): BarRequestConfig {
        val config = super.getBarReuqestConfig()
        config.isNeedBacktest = false
        return config
    }

    @PostConstruct
    fun initAfter() {
        syncStatus()
    }

    fun syncStatus() {
        val allBalances = binanceAccountService.account().balances
        val totalAccountUSDT = allBalances.filter { it.currency.equals(Currency.USDT) }
        val totalAccountBTC = allBalances.filter { it.currency.equals(Currency.BTC) }

        val allOpenBTCUSDTOrders = binanceTradeService.openOrders(CurrencyPair.BTC_USDT)

        //find all the prices
        val theQuantOrder = allOpenBTCUSDTOrders.filter {
            it.price.toString().endsWith(priceMarkThatBelongTheSystem)
        }.toList()

        //cancel all exist orders
        theQuantOrder.forEach {
            val result = binanceTradeService.cancelOrder(it.orderId.toString())
            logger.debug("cancel order ${it.orderId} complete, ${result}")
        }

    }


    override fun toBuy(barId: Int) {
        super.toBuy(barId)
        if (isTest) {
            return
        }
        val buyPrice = ema144.getValue(barId).doubleValue().toString()
        val limitOrder = buildLimitBuyOrder(buyPrice)
        mBuyOrderId = binanceTradeService.placeLimitOrder(limitOrder)
    }

    override fun toSell(barId: Int) {
        super.toSell(barId)
        if (isTest) {
            return
        }
        if (mBuyOrderId != null) {
            binanceTradeService.cancelOrder(mBuyOrderId)
        }

    }

    fun refreshStopOrder(barId: Int) {
        val sellPrice = ema144.getValue(barId).doubleValue().toString()
        val limitOrder = buildStopOrder(sellPrice, sellPrice, mStopOrderId)
        mBuyOrderId = binanceTradeService.placeStopOrder(limitOrder)
    }

    override fun onTickReceived(barId: Int) {
        super.onTickReceived(barId)
        refreshStopOrder(barId = barId)
    }

    fun buildLimitBuyOrder(price: String, orderId: String? = null): LimitOrder {
        val theOrder = LimitOrder.Builder(Order.OrderType.BID, CurrencyPair.BTC_USDT)
            .originalAmount(BigDecimal(tradeAmount))
            .limitPrice(BigDecimal(price).markThePrice())
            .build()
        return theOrder
    }


    fun buildStopOrder(stopPrice: String, limitPrice: String, orderId: String? = null): StopOrder {

        val theStopOrder = StopOrder.Builder(Order.OrderType.ASK, CurrencyPair.BTC_USDT)
            .stopPrice(BigDecimal(stopPrice).markThePrice())//止损触发价
            .limitPrice(BigDecimal(stopPrice).markThePrice())//挂单价
            .originalAmount(BigDecimal(tradeAmount))
            .intention(StopOrder.Intention.STOP_LOSS)
            .flag(TimeInForce.GTC)

        if (orderId != null) {
            theStopOrder.id(orderId)
        }

        return theStopOrder.build()
    }

    fun BigDecimal.markThePrice(): BigDecimal {
        val l = this.setScale(0, BigDecimal.ROUND_DOWN) // 向上取整
        return l.add(BigDecimal(priceMarkThatBelongTheSystem))
    }

}