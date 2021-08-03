package com.binance.client.model.enums;

import com.binance.client.impl.utils.EnumLookup;

public enum QueryDirection {
  PREV("prev"),
  NEXT("next");

  private final String code;

  QueryDirection(String code) {
    this.code = code;
  }

  @Override
  public String toString() {
    return code;
  }

  private static final EnumLookup<com.binance.client.model.enums.QueryDirection> lookup = new EnumLookup<>(com.binance.client.model.enums.QueryDirection.class);

  public static com.binance.client.model.enums.QueryDirection lookup(String name) {
    return lookup.lookup(name);
  }
}
