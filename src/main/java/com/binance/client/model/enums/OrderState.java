package com.binance.client.model.enums;

import com.binance.client.impl.utils.EnumLookup;

/**
 * SUBMITTED, PARTIALFILLED, CANCELLING. PARTIALCANCELED FILLED CANCELED CREATED
 */
public enum OrderState {
  SUBMITTED("submitted"),
  CREATED("created"),
  PARTIALFILLED("partial-filled"),
  CANCELLING("cancelling"),
  PARTIALCANCELED("partial-canceled"),
  FILLED("filled"),
  CANCELED("canceled");


  private final String code;

  OrderState(String code) {
    this.code = code;
  }

  @Override
  public String toString() {
    return code;
  }

  private static final EnumLookup<com.binance.client.model.enums.OrderState> lookup = new EnumLookup<>(com.binance.client.model.enums.OrderState.class);

  public static com.binance.client.model.enums.OrderState lookup(String name) {
    return lookup.lookup(name);
  }
}
