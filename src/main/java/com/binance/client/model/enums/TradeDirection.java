package com.binance.client.model.enums;

import com.binance.client.impl.utils.EnumLookup;

/**
 * buy, sell.
 */
public enum TradeDirection {
  BUY("buy"),
  SELL("sell");

  private final String code;

  TradeDirection(String side) {
    this.code = side;
  }

  @Override
  public String toString() {
    return code;
  }

  private static final EnumLookup<com.binance.client.model.enums.TradeDirection> lookup = new EnumLookup<>(com.binance.client.model.enums.TradeDirection.class);

  public static com.binance.client.model.enums.TradeDirection lookup(String name) {
    return lookup.lookup(name);
  }
}
