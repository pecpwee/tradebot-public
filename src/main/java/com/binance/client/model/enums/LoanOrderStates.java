package com.binance.client.model.enums;

import com.binance.client.impl.utils.EnumLookup;

/**
 * created, accrual, cleared, invalid.
 */
public enum LoanOrderStates {

  CREATED("created"),
  ACCRUAL("accrual"),
  CLEARED("cleared"),
  INVALID("invalid");

  private final String code;

  LoanOrderStates(String state) {
    this.code = state;
  }

  @Override
  public String toString() {
    return code;
  }

  private static final EnumLookup<com.binance.client.model.enums.LoanOrderStates> lookup = new EnumLookup<>(com.binance.client.model.enums.LoanOrderStates.class);

  public static com.binance.client.model.enums.LoanOrderStates lookup(String name) {
    return lookup.lookup(name);
  }
}
