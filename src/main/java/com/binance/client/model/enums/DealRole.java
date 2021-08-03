package com.binance.client.model.enums;

public enum DealRole {

  /**
   * TAKER,MAKER
   */

  TAKER("taker"),
  MAKER("maker")
  ;

  private final String role;

  DealRole(String role) {
    this.role = role;
  }

  public String getRole() {
    return role;
  }

  public static com.binance.client.model.enums.DealRole find(String role) {
    for (com.binance.client.model.enums.DealRole fr : com.binance.client.model.enums.DealRole.values()) {
      if (fr.getRole().equals(role)) {
        return fr;
      }
    }
    return null;
  }
}
