package com.binance.client.model.enums;

import com.binance.client.impl.utils.EnumLookup;

public enum  EtfSwapType {
  ETF_SWAP_IN("1"),
  ETF_SWAP_OUT("2");

  private final String code;

  EtfSwapType(String code) {
    this.code = code;
  }

  @Override
  public String toString() {
    return code;
  }

  private static final EnumLookup<com.binance.client.model.enums.EtfSwapType> lookup = new EnumLookup<>(com.binance.client.model.enums.EtfSwapType.class);

  public static com.binance.client.model.enums.EtfSwapType lookup(String name) {
    return lookup.lookup(name);
  }
}
