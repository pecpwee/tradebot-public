package com.binance.client.model.enums;

import com.binance.client.impl.utils.EnumLookup;

public enum TransferMasterType {


  MASTER_TRANSFER_IN("master-transfer-in"),
  MASTER_TRANSFER_OUT("master-transfer-out"),
  MASTER_POINT_TRANSFER_IN("master-point-transfer-in"),
  MASTER_POINT_TRANSFER_OUT("master-point-transfer-out");
  private final String code;

  TransferMasterType(String side) {
    this.code = side;
  }

  @Override
  public String toString() {
    return code;
  }

  private static final EnumLookup<com.binance.client.model.enums.TransferMasterType> lookup = new EnumLookup<>(com.binance.client.model.enums.TransferMasterType.class);

  public static com.binance.client.model.enums.TransferMasterType lookup(String name) {
    return lookup.lookup(name);
  }
}
