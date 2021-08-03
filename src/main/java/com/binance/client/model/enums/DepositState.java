package com.binance.client.model.enums;

import com.binance.client.impl.utils.EnumLookup;


/**
 * withdraw, deposit.
 */
public enum DepositState {

  UNKNOWN("unknown"),
  CONFIRMING("confirming"),
  SAFE("safe"),
  CONFIRMED("confirmed"),
  ORPHAN("orphan");


  private final String code;

  DepositState(String code) {
    this.code = code;
  }

  @Override
  public String toString() {
    return code;
  }

  private static final EnumLookup<com.binance.client.model.enums.DepositState> lookup = new EnumLookup<>(com.binance.client.model.enums.DepositState.class);

  public static com.binance.client.model.enums.DepositState lookup(String name) {
    return lookup.lookup(name);
  }

}
