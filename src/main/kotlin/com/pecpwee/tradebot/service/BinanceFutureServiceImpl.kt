package com.pecpwee.tradebot.service

import com.binance.api.client.domain.market.Candlestick
import com.binance.api.client.domain.market.CandlestickInterval
import com.binance.client.SubscriptionClient
import com.binance.client.SubscriptionErrorHandler
import com.binance.client.SubscriptionListener
import com.binance.client.SyncRequestClient
import com.binance.client.model.enums.*
import com.binance.client.model.event.AggregateTradeEvent
import com.binance.client.model.event.CandlestickEvent
import com.binance.client.model.event.LiquidationOrderEvent
import com.binance.client.model.event.OrderBookEvent
import com.binance.client.model.trade.*
import com.pecpwee.tradebot.repository.TradePair
import com.pecpwee.tradebot.utils.toNoneSplitPair
import org.apache.logging.log4j.Logger
import org.knowm.xchange.currency.CurrencyPair
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*


/***
 * API DOCS: https://binance-docs.github.io/apidocs/futures/cn/#185368440e
 *
 * 根据 order type的不同，某些参数强制要求，具体如下:

Type	强制要求的参数
LIMIT	timeInForce, quantity, price
MARKET	quantity
STOP, TAKE_PROFIT	quantity, price, stopPrice
STOP_MARKET, TAKE_PROFIT_MARKET	stopPrice
TRAILING_STOP_MARKET	callbackRate

 */
@Service
class BinanceFutureServiceImpl {

    @Autowired
    private lateinit var logger: Logger

    @Autowired
    private lateinit var binanceFutureApi: SyncRequestClient

    @Autowired
    private lateinit var binanceFutureStreamApi: SubscriptionClient

    fun getTheQuantity(tradePair: CurrencyPair) {
        binanceFutureApi.getOpenOrders(tradePair.toNoneSplitPair())
    }

    fun getCandleList(
        tradePair: TradePair,
        interval: CandlestickInterval,
        limit: Int,
        startTime: Long?,
        endTime: Long?
    ): List<Candlestick> {
        val candleInfo = binanceFutureApi.getCandlestick(
            tradePair.value,
            interval,
            startTime,
            endTime,
            limit
        )
        return candleInfo
    }


    /***
     * 调整开仓杠杆,1-125倍都可以选择
     */
    fun changeInitialLeverage(symbol: CurrencyPair, leverage: Int) {
        binanceFutureApi.changeInitialLeverage(symbol.toNoneSplitPair(), leverage)
    }

    /***
     * 调整逐仓还是全仓
     */
    fun changeMarginType(symbol: CurrencyPair, marginType: String) {
        binanceFutureApi.changeMarginType(symbol.toNoneSplitPair(), marginType)
    }


    fun getAccountTrades(symbol: CurrencyPair, startTime: Long, endTime: Long, fromId: Long, limit: Int) {
        binanceFutureApi.getAccountTrades(symbol.toNoneSplitPair(), startTime, endTime, fromId, limit)
    }

    fun getAccountBalance(): List<AccountBalance> {
        return binanceFutureApi.balance
    }


    fun getAccountInfo(): AccountInformation {
        return binanceFutureApi.accountInformation
    }


    /**
     *
     * 完成订单请求方法
     * 根据 order type的不同，某些参数强制要求，具体如下:
     *  Type	强制要求的参数
     * LIMIT	timeInForce, quantity, price
     * MARKET	quantity
     * STOP, TAKE_PROFIT	quantity, price, stopPrice
     * STOP_MARKET, TAKE_PROFIT_MARKET	stopPrice
     * TRAILING_STOP_MARKET	callbackRate
     * reduceOnly true或者false
     *
     * */
    fun postOrder(
        symbol: CurrencyPair,
        side: OrderSide,
        positionSide: PositionSide? = null,
        orderType: OrderType,
        timeInForce: TimeInForce? = TimeInForce.GTC,
        quantity: String? = null,
        price: String? = null,
        reduceOnly: Boolean = false,
        newClientOrderId: String? = null,
        stopPrice: String? = null,
        workingType: WorkingType? = null,
        newOrderRespType: NewOrderRespType? = null,
        activationPrice: String? = null,//跟踪止损特有内容
        callbackRate: String? = null, //跟踪止损回调比例【0.1到5】，自带百分比单位
        isClosePosition: Boolean = false
    ): Order? {
        return binanceFutureApi.postOrder(
            symbol.toNoneSplitPair(),
            side,
            positionSide,
            orderType,
            timeInForce,
            quantity,
            price,
            if (reduceOnly) "true" else "false",
            newClientOrderId,
            stopPrice,
            workingType,
            newOrderRespType,
            activationPrice,
            callbackRate,
            isClosePosition
        )

    }

    /**
     * LIMIT 限价成交单
     *
     *
     * */
    fun postLimitOrder(
        symbol: CurrencyPair,
        side: OrderSide,
        quantity: String,
        price: String
    ): Order? {


        return postOrder(
            symbol = symbol,
            side = side,
            orderType = OrderType.LIMIT,
            quantity = quantity,
            price = price.toString()
        )
    }

    /**
     * 市价成交单
     *
     *
     * */
    fun postMarketOrder(symbol: CurrencyPair, side: OrderSide, quantity: String): Order? {
        return postOrder(
            symbol = symbol,
            side = side,
            orderType = OrderType.MARKET,
            quantity = quantity,
            price = null,
            timeInForce = null
        )
    }


    /**
     * 止损限价单
     * price触发价
     * stopPrice实际止损价
     *
     *
     * STOP 止损限价单
     *
     *
     * */
    fun postSTOPOrder(
        symbol: CurrencyPair,
        side: OrderSide,
        quantity: String,
        invokePrice: String,
        stopPrice: String
    ): Order? {
        return postOrder(
            symbol = symbol,
            side = side,
            orderType = OrderType.STOP,
            quantity = quantity,
            price = invokePrice,
            stopPrice = stopPrice
        )
    }

    /**
     * 止损市价单
     * */
    fun postSTOPMarketOrder(
        symbol: CurrencyPair,
        side: OrderSide,
        quantity: String,
        stopPrice: String
    ): Order? {
        return postOrder(
            symbol = symbol,
            side = side,
            orderType = OrderType.STOP_MARKET,
            quantity = quantity,
            stopPrice = stopPrice,
            reduceOnly = true
        )
    }

    fun postTakeProfitOrder(
        symbol: CurrencyPair,
        side: OrderSide,
        quantity: String,
        stopPrice: String
    ): Order? {
        return postOrder(
            symbol = symbol,
            side = side,
            orderType = OrderType.TAKE_RPOFIT,
            quantity = quantity,
            stopPrice = stopPrice
        )
    }

    /**
     * 市价止盈单
     *
     * */
    fun postTakeProfitMarketOrder(
        symbol: CurrencyPair,
        side: OrderSide,
        quantity: String,
        price: String,
        stopPrice: String
    ): Order? {
        return postOrder(
            symbol = symbol,
            side = side,
            orderType = OrderType.TAKE_RPOFIT_MARKET,
            quantity = quantity,
            price = price,
            stopPrice = stopPrice
        )
    }

    fun postTraitStopMarketOrder(
        symbol: CurrencyPair, side: OrderSide, quantity: String, price: String, activationPrice: String,//跟踪止损特有内容
        callbackRate: String  //跟踪止损回调比例【0.1到5】，自带百分比单位
    ): Order? {
        return postOrder(
            symbol = symbol,
            side = side,
            orderType = OrderType.TRAILING_STOP_MARKET,
            quantity = quantity,
            price = price,
            activationPrice = activationPrice,
            callbackRate = callbackRate
        )
    }

    /*
    * countdownTime：单位毫秒
    *
    *
    * */
    fun cancelAllOrder(symbol: CurrencyPair, countdownTime: Long, timestamp: Long): CountDownCancelResponse {
        return binanceFutureApi.cancelAllOpenOrder(symbol.toNoneSplitPair(), countdownTime, timestamp)
    }

    /**
     * @param orderId 可选，订单号
     * @param origClientOrderId 可选，自定义订单号
     */
    fun getOrder(symbol: CurrencyPair, orderId: Long?, origClientOrderId: String?): Order? {
        return binanceFutureApi.getOrder(symbol.toNoneSplitPair(), orderId, origClientOrderId)
    }


    fun getAllOrders(symbol: CurrencyPair, orderId: Long, startTime: Long, endTime: Long, limit: Int): List<Order> {
        return binanceFutureApi.getAllOrders(symbol.toNoneSplitPair(), orderId, startTime, endTime, limit)
    }


    fun cancelOrder(symbol: CurrencyPair, orderId: Long, origClientOrderId: String?): Order {
        return binanceFutureApi.cancelOrder(symbol.toNoneSplitPair(), orderId, origClientOrderId)
    }

    fun getOpenOrder(symbol: CurrencyPair): List<Order> {
        return binanceFutureApi.getOpenOrders(symbol.toNoneSplitPair())
    }


    fun getPositionRisk(symbol: TradePair): List<PositionRisk> {
        return binanceFutureApi.positionRisk
    }


    /**
     * 一下是websocket 流接口
     */
    fun subscribeAppAggregateTrade(
        symbol: TradePair,
        callback: SubscriptionListener<AggregateTradeEvent?>?, errorHandler: SubscriptionErrorHandler?
    ) {
        binanceFutureStreamApi.subscribeAggregateTradeEvent(symbol.value.lowercase(Locale.getDefault()), callback, errorHandler)
    }

    fun subscribeDepthTrade(
        symbol: TradePair, limit: Int,
        callback: SubscriptionListener<OrderBookEvent>, errorHandler: SubscriptionErrorHandler
    ) {
        binanceFutureStreamApi.subscribeBookDepthEvent(symbol.value.lowercase(Locale.getDefault()), limit, callback, errorHandler)
    }


    fun subscribeLiquidation(
        symbol: TradePair,
        callback: SubscriptionListener<LiquidationOrderEvent>, errorHandler: SubscriptionErrorHandler?
    ) {
        binanceFutureStreamApi.subscribeSymbolLiquidationOrderEvent(symbol.value.lowercase(Locale.getDefault()), callback, errorHandler)
    }


    fun subscribeCandlestickEvent(
        symbol: String?, interval: CandlestickInterval?,
        callback: SubscriptionListener<CandlestickEvent?>?, errorHandler: SubscriptionErrorHandler?
    ) {
        binanceFutureStreamApi.subscribeCandlestickEvent(symbol, interval, callback, errorHandler)

    }


}