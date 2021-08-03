package com.binance.client.model.enums;

public enum  SymbolState {

  /**
   * ONLINE, OFFLINE, SUSPEND.
   */
  ONLINE("online"),
  OFFLINE("offline"),
  SUSPEND("suspend")
  ;
  private final String state;

  SymbolState(String state) {
    this.state = state;
  }

  public String getState() {
    return state;
  }

  public static com.binance.client.model.enums.SymbolState find(String state) {
    for (com.binance.client.model.enums.SymbolState st : com.binance.client.model.enums.SymbolState.values()) {
      if (st.getState().equals(state)) {
        return st;
      }
    }
    return null;
  }

}
