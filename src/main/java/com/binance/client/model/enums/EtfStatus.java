package com.binance.client.model.enums;

import com.binance.client.impl.utils.EnumLookup;

public enum EtfStatus {
  NORMAL("1"),
  REBALANCING_START("2"),
  CREATION_AND_REDEMPTION_SUSPEND("3"),
  CREATION_SUSPEND("4"),
  REDEMPTION_SUSPEND("5");

  private final String code;

  EtfStatus(String code) {
    this.code = code;
  }

  @Override
  public String toString() {
    return code;
  }

  private static final EnumLookup<com.binance.client.model.enums.EtfStatus> lookup = new EnumLookup<>(com.binance.client.model.enums.EtfStatus.class);

  public static com.binance.client.model.enums.EtfStatus lookup(String name) {
    return lookup.lookup(name);
  }
}
