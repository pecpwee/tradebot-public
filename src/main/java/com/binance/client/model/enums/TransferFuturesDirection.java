package com.binance.client.model.enums;


public enum  TransferFuturesDirection {

  /**
   * FUTURES_TO_PRO,PRO_TO_FUTURES
   */
  FUTURES_TO_PRO("futures-to-pro"),
  PRO_TO_FUTURES("pro-to-futures")
  ;

  private final String direction;

  TransferFuturesDirection(String direction) {
    this.direction = direction;
  }

  public String getDirection() {
    return direction;
  }

  public static com.binance.client.model.enums.TransferFuturesDirection find(String direction){
    for (com.binance.client.model.enums.TransferFuturesDirection d : com.binance.client.model.enums.TransferFuturesDirection.values()) {
      if (d.getDirection().equals(direction)) {
        return d;
      }
    }
    return null;
  }
}
