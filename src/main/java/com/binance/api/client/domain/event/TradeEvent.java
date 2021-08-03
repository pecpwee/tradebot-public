package com.binance.api.client.domain.event;

import com.binance.api.client.constant.BinanceApiConstants;
import com.binance.api.client.domain.market.AggTrade;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * An aggregated trade event for a symbol.
 * {
 * "e": "24hrTicker",  // 事件类型
 * "E": 123456789,     // 事件时间
 * "s": "BNBBTC",      // 交易对
 * "p": "0.0015",      // 24小时价格变化
 * "P": "250.00",      // 24小时价格变化(百分比)
 * "w": "0.0018",      // 平均价格
 * "x": "0.0009",      // 整整24小时之前，向前数的最后一次成交价格
 * "c": "0.0025",      // 最新成交价格
 * "Q": "10",          // 最新成交交易的成交量
 * "b": "0.0024",      // 目前最高买单价
 * "B": "10",          // 目前最高买单价的挂单量
 * "a": "0.0026",      // 目前最低卖单价
 * "A": "100",         // 目前最低卖单价的挂单量
 * "o": "0.0010",      // 整整24小时前，向后数的第一次成交价格
 * "h": "0.0025",      // 24小时内最高成交价
 * "l": "0.0010",      // 24小时内最低成交加
 * "v": "10000",       // 24小时内成交量
 * "q": "18",          // 24小时内成交额
 * "O": 0,             // 统计开始时间
 * "C": 86400000,      // 统计结束时间
 * "F": 0,             // 24小时内第一笔成交交易ID
 * "L": 18150,         // 24小时内最后一笔成交交易ID
 * "n": 18151          // 24小时内成交数
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TradeEvent extends AggTrade {

    @JsonProperty("e")
    private String eventType;

    @JsonProperty("E")
    private long eventTime;

    @JsonProperty("s")
    private String symbol;

    @JsonProperty("P")
    private String percentage;

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public long getEventTime() {
        return eventTime;
    }

    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE)
                .append("eventType", eventType)
                .append("eventTime", eventTime)
                .append("symbol", symbol)
                .append("aggTrade", super.toString())
                .toString();
    }
}
