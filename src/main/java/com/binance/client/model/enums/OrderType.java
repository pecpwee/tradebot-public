package com.binance.client.model.enums;

import com.binance.client.impl.utils.EnumLookup;

/**
 * buy-market, sell-market, buy-limit, buy-ioc, sell-ioc,
 * buy-limit-maker, sell-limit-maker, buy-stop-limit, sell-stop-limit.
 */
public enum OrderType {
    LIMIT("LIMIT"),
    MARKET("MARKET"),
    STOP("STOP"),
    STOP_MARKET("STOP_MARKET"),
    TAKE_RPOFIT("TAKE_RPOFIT"),
    TAKE_RPOFIT_MARKET("TAKE_RPOFIT_MARKET"),
    TRAILING_STOP_MARKET("TRAILING_STOP_MARKET"),
    INVALID(null);

  private final String code;

  OrderType(String code) {
    this.code = code;
  }

  @Override
  public String toString() {
    return code;
  }

  private static final EnumLookup<com.binance.client.model.enums.OrderType> lookup = new EnumLookup<>(com.binance.client.model.enums.OrderType.class);

  public static com.binance.client.model.enums.OrderType lookup(String name) {
    return lookup.lookup(name);
  }

}
