package com.binance.client.impl;

import com.alibaba.fastjson.JSONObject;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import com.binance.client.SyncRequestClient;
import com.binance.client.model.ResponseResult;
import com.binance.client.model.enums.*;
import com.binance.client.model.market.*;
import com.binance.client.model.trade.*;

import java.util.List;

public class SyncRequestImpl implements SyncRequestClient {

    private final com.binance.client.impl.RestApiRequestImpl requestImpl;

    SyncRequestImpl(com.binance.client.impl.RestApiRequestImpl requestImpl) {
        this.requestImpl = requestImpl;
    }


    @Override
    public ExchangeInformation getExchangeInformation() {
        return com.binance.client.impl.RestApiInvoker.callSync(requestImpl.getExchangeInformation());
    }

    @Override
    public OrderBook getOrderBook(String symbol, Integer limit) {
        return com.binance.client.impl.RestApiInvoker.callSync(requestImpl.getOrderBook(symbol, limit));
    }

    @Override
    public List<Trade> getRecentTrades(String symbol, Integer limit) {
        return com.binance.client.impl.RestApiInvoker.callSync(requestImpl.getRecentTrades(symbol, limit));
    }

    @Override
    public List<Trade> getOldTrades(String symbol, Integer limit, Long fromId) {
        return com.binance.client.impl.RestApiInvoker.callSync(requestImpl.getOldTrades(symbol, limit, fromId));
    }

    @Override
    public List<AggregateTrade> getAggregateTrades(String symbol, Long fromId, Long startTime,
                                                   Long endTime, Integer limit) {
        return com.binance.client.impl.RestApiInvoker.callSync(requestImpl.getAggregateTrades(symbol, fromId, startTime, endTime, limit));
    }

    @Override
    public List<Candlestick> getCandlestick(String symbol, CandlestickInterval interval, Long startTime,
                                            Long endTime, Integer limit) {
        return com.binance.client.impl.RestApiInvoker.callSync(requestImpl.getCandlestick(symbol, interval, startTime, endTime, limit));
    }

    @Override
    public List<MarkPrice> getMarkPrice(String symbol) {
        return com.binance.client.impl.RestApiInvoker.callSync(requestImpl.getMarkPrice(symbol));
    }

    @Override
    public List<FundingRate> getFundingRate(String symbol, Long startTime, Long endTime, Integer limit) {
        return com.binance.client.impl.RestApiInvoker.callSync(requestImpl.getFundingRate(symbol, startTime, endTime, limit));
    }

    @Override
    public List<PriceChangeTicker> get24hrTickerPriceChange(String symbol) {
        return com.binance.client.impl.RestApiInvoker.callSync(requestImpl.get24hrTickerPriceChange(symbol));
    }

    @Override
    public List<SymbolPrice> getSymbolPriceTicker(String symbol) {
        return com.binance.client.impl.RestApiInvoker.callSync(requestImpl.getSymbolPriceTicker(symbol));
    }

    @Override
    public List<SymbolOrderBook> getSymbolOrderBookTicker(String symbol) {
        return com.binance.client.impl.RestApiInvoker.callSync(requestImpl.getSymbolOrderBookTicker(symbol));
    }

    @Override
    public List<LiquidationOrder> getLiquidationOrders(String symbol, Long startTime, Long endTime, Integer limit) {
        return com.binance.client.impl.RestApiInvoker.callSync(requestImpl.getLiquidationOrders(symbol, startTime, endTime, limit));
    }

    @Override
    public List<Object> postBatchOrders(String batchOrders) {
        return com.binance.client.impl.RestApiInvoker.callSync(requestImpl.postBatchOrders(batchOrders));
    }

    @Override
    public Order postOrder(String symbol, OrderSide side, PositionSide positionSide, OrderType orderType,
                           TimeInForce timeInForce, String quantity, String price, String reduceOnly,
                           String newClientOrderId, String stopPrice, WorkingType workingType, NewOrderRespType newOrderRespType
            , String activationPrice, String callbackRate, boolean closePosition
    ) {
        return com.binance.client.impl.RestApiInvoker.callSync(requestImpl.postOrder(symbol, side, positionSide, orderType,
                timeInForce, quantity, price, reduceOnly,
                newClientOrderId, stopPrice, workingType, newOrderRespType, activationPrice, callbackRate, closePosition));
    }

    @Override
    public Order postTestOrder(String symbol, OrderSide side, PositionSide positionSide, OrderType orderType
            , TimeInForce timeInForce, String quantity, String price, String reduceOnly
            , String newClientOrderId, String stopPrice, WorkingType workingType
            , NewOrderRespType newOrderRespType, String activationPrice, String callbackRate, boolean closePosition) {
        return com.binance.client.impl.RestApiInvoker.callSync(requestImpl.postOrder(symbol, side, positionSide, orderType,
                timeInForce, quantity, price, reduceOnly,
                newClientOrderId, stopPrice, workingType, newOrderRespType, activationPrice, callbackRate, closePosition));
    }

    @Override
    public Order cancelOrder(String symbol, Long orderId, String origClientOrderId) {
        return com.binance.client.impl.RestApiInvoker.callSync(requestImpl.cancelOrder(symbol, orderId, origClientOrderId));
    }

    @Override
    public CountDownCancelResponse cancelAllOpenOrder(String symbol, Long countdownTime, Long timestamp) {
        return com.binance.client.impl.RestApiInvoker.callSync(requestImpl.cancelAllOpenOrder(symbol, countdownTime, timestamp));
    }

    @Override
    public List<Object> batchCancelOrders(String symbol, String orderIdList, String origClientOrderIdList) {
        return com.binance.client.impl.RestApiInvoker.callSync(requestImpl.batchCancelOrders(symbol, orderIdList, origClientOrderIdList));
    }

    @Override
    public ResponseResult changePositionSide(boolean dual) {
        return com.binance.client.impl.RestApiInvoker.callSync(requestImpl.changePositionSide(dual));
    }

    @Override
    public ResponseResult changeMarginType(String symbolName, String marginType) {
        return com.binance.client.impl.RestApiInvoker.callSync(requestImpl.changeMarginType(symbolName, marginType));
    }

    @Override
    public JSONObject addIsolatedPositionMargin(String symbolName, int type, String amount, PositionSide positionSide) {
        return com.binance.client.impl.RestApiInvoker.callSync(requestImpl.addPositionMargin(symbolName, type, amount, positionSide));
    }

    @Override
    public List<WalletDeltaLog> getPositionMarginHistory(String symbolName, int type, long startTime, long endTime, int limit) {
        return com.binance.client.impl.RestApiInvoker.callSync(requestImpl.getPositionMarginHistory(symbolName, type, startTime, endTime, limit));
    }


    @Override
    public JSONObject getPositionSide() {
        return com.binance.client.impl.RestApiInvoker.callSync(requestImpl.getPositionSide());
    }

    @Override
    public Order getOrder(String symbol, Long orderId, String origClientOrderId) {
        return com.binance.client.impl.RestApiInvoker.callSync(requestImpl.getOrder(symbol, orderId, origClientOrderId));
    }

    @Override
    public List<Order> getOpenOrders(String symbol) {
        return com.binance.client.impl.RestApiInvoker.callSync(requestImpl.getOpenOrders(symbol));
    }

    @Override
    public List<Order> getAllOrders(String symbol, Long orderId, Long startTime, Long endTime, Integer limit) {
        return com.binance.client.impl.RestApiInvoker.callSync(requestImpl.getAllOrders(symbol, orderId, startTime, endTime, limit));
    }

    @Override
    public List<AccountBalance> getBalance() {
        return com.binance.client.impl.RestApiInvoker.callSync(requestImpl.getBalance());
    }

    @Override
    public AccountInformation getAccountInformation() {
        return com.binance.client.impl.RestApiInvoker.callSync(requestImpl.getAccountInformation());
    }

    @Override
    public Leverage changeInitialLeverage(String symbol, Integer leverage) {
        return com.binance.client.impl.RestApiInvoker.callSync(requestImpl.changeInitialLeverage(symbol, leverage));
    }

    @Override
    public List<PositionRisk> getPositionRisk() {
        return com.binance.client.impl.RestApiInvoker.callSync(requestImpl.getPositionRisk());
    }

    @Override
    public List<MyTrade> getAccountTrades(String symbol, Long startTime, Long endTime, Long fromId, Integer limit) {
        return com.binance.client.impl.RestApiInvoker.callSync(requestImpl.getAccountTrades(symbol, startTime, endTime, fromId, limit));
    }

    @Override
    public List<Income> getIncomeHistory(String symbol, IncomeType incomeType, Long startTime, Long endTime, Integer limit) {
        return com.binance.client.impl.RestApiInvoker.callSync(requestImpl.getIncomeHistory(symbol, incomeType, startTime, endTime, limit));
    }

    @Override
    public String startUserDataStream() {
        return com.binance.client.impl.RestApiInvoker.callSync(requestImpl.startUserDataStream());
    }

    @Override
    public String keepUserDataStream(String listenKey) {
        return com.binance.client.impl.RestApiInvoker.callSync(requestImpl.keepUserDataStream(listenKey));
    }

    @Override
    public String closeUserDataStream(String listenKey) {
        return com.binance.client.impl.RestApiInvoker.callSync(requestImpl.closeUserDataStream(listenKey));
    }

    @Override
    public List<OpenInterestStat> getOpenInterestStat(String symbol, PeriodType period, Long startTime, Long endTime, Integer limit) {
        return com.binance.client.impl.RestApiInvoker.callSync(requestImpl.getOpenInterestStat(symbol, period, startTime, endTime, limit));
    }

    @Override
    public List<CommonLongShortRatio> getTopTraderAccountRatio(String symbol, PeriodType period, Long startTime, Long endTime, Integer limit) {
        return com.binance.client.impl.RestApiInvoker.callSync(requestImpl.getTopTraderAccountRatio(symbol, period, startTime, endTime, limit));
    }

    @Override
    public List<CommonLongShortRatio> getTopTraderPositionRatio(String symbol, PeriodType period, Long startTime, Long endTime, Integer limit) {
        return com.binance.client.impl.RestApiInvoker.callSync(requestImpl.getTopTraderPositionRatio(symbol, period, startTime, endTime, limit));
    }

    @Override
    public List<CommonLongShortRatio> getGlobalAccountRatio(String symbol, PeriodType period, Long startTime, Long endTime, Integer limit) {
        return com.binance.client.impl.RestApiInvoker.callSync(requestImpl.getGlobalAccountRatio(symbol, period, startTime, endTime, limit));
    }

    @Override
    public List<TakerLongShortStat> getTakerLongShortRatio(String symbol, PeriodType period, Long startTime, Long endTime, Integer limit) {
        return com.binance.client.impl.RestApiInvoker.callSync(requestImpl.getTakerLongShortRatio(symbol, period, startTime, endTime, limit));
    }
}
