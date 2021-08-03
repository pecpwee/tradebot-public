package com.binance.client.model.enums;

import com.binance.client.impl.utils.EnumLookup;

/**
 * SPOT, MARGIN, OTC, POINT, UNKNOWN.
 */
public enum AccountType {
  SPOT("spot"),
  MARGIN("margin"),
  OTC("otc"),
  POINT("point"),
  SUPER_MARGIN("super-margin"),
  MINEPOOL("minepool"),
  ETF( "etf"),
  AGENCY( "agency"),
  UNKNOWN("unknown");

  private final String code;

  AccountType(String code) {
    this.code = code;
  }

  @Override
  public String toString() {
    return code;
  }

  private static final EnumLookup<com.binance.client.model.enums.AccountType> lookup = new EnumLookup<>(com.binance.client.model.enums.AccountType.class);

  public static com.binance.client.model.enums.AccountType lookup(String name) {
    return lookup.lookup(name);
  }

}
