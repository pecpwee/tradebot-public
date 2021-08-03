package com.binance.api.client.domain.market;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Kline/Candlestick intervals.
 * m -> minutes; h -> hours; d -> days; w -> weeks; M -> months
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public enum CandlestickInterval {
    ONE_MINUTE("1m", 1),
    THREE_MINUTES("3m", 3),
    FIVE_MINUTES("5m", 5),
    FIFTEEN_MINUTES("15m", 15),
    HALF_HOURLY("30m", 30),
    HOURLY("1h", 60),
    TWO_HOURLY("2h", 60 * 2),
    FOUR_HOURLY("4h", 60 * 4),
    SIX_HOURLY("6h", 60 * 6),
    EIGHT_HOURLY("8h", 60 * 8),
    TWELVE_HOURLY("12h", 60 * 12),
    DAILY("1d", 60 * 24),
    THREE_DAILY("3d", 60 * 24 * 3),
    WEEKLY("1w", 60 * 24 * 7),
    MONTHLY("1M", 60 * 24 * 30);

    private final String intervalId;
    private final long minutes;

    CandlestickInterval(String intervalId, long minutes) {
        this.intervalId = intervalId;
        this.minutes = minutes;
    }

    public String getIntervalId() {
        return intervalId;
    }

    public long getMinutesCount() {
        return minutes;
    }

}
