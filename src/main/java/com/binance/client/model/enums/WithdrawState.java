package com.binance.client.model.enums;

import com.binance.client.impl.utils.EnumLookup;


/**
 * withdraw, deposit.
 */
public enum WithdrawState {


  SUBMITTED("submitted"),
  REEXAMINE("reexamine"),
  CANCELED("canceled"),
  PASS("pass"),
  REJECT("reject"),
  PRETRANSFER("pre-transfer"),
  WALLETTRANSFER("wallet-transfer"),
  WALEETREJECT("wallet-reject"),
  CONFIRMED("confirmed"),
  CONFIRMERROR("confirm-error"),
  REPEALED("repealed");


  private final String code;

  WithdrawState(String code) {
    this.code = code;
  }

  @Override
  public String toString() {
    return code;
  }

  private static final EnumLookup<com.binance.client.model.enums.WithdrawState> lookup = new EnumLookup<>(com.binance.client.model.enums.WithdrawState.class);

  public static com.binance.client.model.enums.WithdrawState lookup(String name) {
    return lookup.lookup(name);
  }

}
