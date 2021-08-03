package com.pecpwee.tradebot.strategy.complete

import com.pecpwee.tradebot.strategy.Vegas1Minutes
import org.knowm.xchange.binance.service.BinanceAccountService
import org.knowm.xchange.binance.service.BinanceTradeService
import org.knowm.xchange.currency.CurrencyPair
import org.knowm.xchange.dto.Order
import org.knowm.xchange.dto.trade.MarketOrder
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import javax.annotation.PostConstruct

//@Component
class Vegas1MinBinanceMarketSpot : Vegas1Minutes() {


//    @Autowired
//    lateinit var binanceSpot: BinanceSpotServiceImpl;

    companion object {
        val isTest = true
    }

    @Autowired
    lateinit var binanceTradeService: BinanceTradeService

    @Autowired
    lateinit var binanceAccountService: BinanceAccountService


    //config设定
    override fun getBarReuqestConfig(): BarRequestConfig {
        val config = super.getBarReuqestConfig()
        config.isNeedBacktest = false
        return config
    }

    @PostConstruct
    fun initAfter() {

    }


    override fun toBuy(barId: Int) {
        super.toBuy(barId)
        if (isTest) {
            logger.info("test status,not buy in real")

            return
        }

        val marketOrder = buildMarketBuyOrder()
        val orderId = binanceTradeService.placeMarketOrder(marketOrder)
        logger.info("buy action complete, orderId:$orderId, orderDetail:${marketOrder}")

    }

    override fun toSell(barId: Int) {
        super.toSell(barId)
        if (isTest) {
            logger.info("test status,not sell in real")
            return
        }
        val marketOrder = buildMarketSellOrder()
        val orderId = binanceTradeService.placeMarketOrder(marketOrder)
        logger.info("sell action complete, orderId:$orderId, orderDetail:${marketOrder}")
    }

    fun buildMarketBuyOrder(): MarketOrder {
        val theOrder = MarketOrder.Builder(Order.OrderType.BID, CurrencyPair.BTC_USDT)
            .originalAmount(BigDecimal(Vegas1MinBinanceLimitSpot.tradeAmount))
            .build()

        return theOrder
    }

    fun buildMarketSellOrder(): MarketOrder {
        val theOrder = MarketOrder.Builder(Order.OrderType.BID, CurrencyPair.BTC_USDT)
            .originalAmount(BigDecimal(Vegas1MinBinanceLimitSpot.tradeAmount))
            .build()
        return theOrder
    }


}