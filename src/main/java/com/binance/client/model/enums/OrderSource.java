package com.binance.client.model.enums;

import com.binance.client.impl.utils.EnumLookup;

/**
 * sys, web, api, app.
 */
public enum OrderSource {
  SYS("sys"),
  WEB("web"),
  API("api"),
  APP("app"),
  FLSYS("fl-sys"),
  FLMGT("fl-mgt"),
  SPOTWEB("spot-web"),
  SPOTAPI("spot-api"),
  SPOTAPP("spot-app"),
  MARGINAPI("margin-api"),
  MARGINWEB("margin-web"),
  MARGINAPP("margin-app"),
  SUPERMARGINAPI("super_margin_api"),
  SUPERMARGINAPP("super_margin_app"),
  SUPERMARGINWEB("super_margin_web"),
  SUPERMARGINFLSYS("super_margin_fl_sys"),
  SUPERMARGINFLMGT("super_margin_fl_mgt");

  private final String code;

  OrderSource(String code) {
    this.code = code;
  }

  @Override
  public String toString() {
    return code;
  }

  private static final EnumLookup<com.binance.client.model.enums.OrderSource> lookup = new EnumLookup<>(com.binance.client.model.enums.OrderSource.class);

  public static com.binance.client.model.enums.OrderSource lookup(String name) {
    return lookup.lookup(name);
  }
}
