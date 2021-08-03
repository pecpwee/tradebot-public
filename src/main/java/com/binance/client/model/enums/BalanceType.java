package com.binance.client.model.enums;

import com.binance.client.impl.utils.EnumLookup;

public enum BalanceType {
  TRADE("trade"),
  FROZEN("frozen"),
  LOAN("loan"),
  INTEREST("interest"),
  LOAN_AVAILABLE("loan-available"),
  TRANSFER_OUT_AVAILABLE("transfer-out-available");



  private final String code;

  BalanceType(String code) {
    this.code = code;
  }

  @Override
  public String toString() {
    return code;
  }

  private static final EnumLookup<com.binance.client.model.enums.BalanceType> lookup = new EnumLookup<>(com.binance.client.model.enums.BalanceType.class);

  public static com.binance.client.model.enums.BalanceType lookup(String name) {
    return lookup.lookup(name);
  }

}
