package com.binance.client.model.enums;

public enum StopOrderOperator {
    /**
     * GTE,greater than and equal (>=) ,LTE less than and equal (<=)
     */
    GTE("gte", "greater than and equal (>=)"), LTE("lte", "less than and equal (<=)");

    private final String operator;

    private final String desc;

    StopOrderOperator(String operator, String desc) {
        this.operator = operator;
        this.desc = desc;
    }

    public String getOperator() {
        return operator;
    }

    public String getDesc() {
        return desc;
    }

    public static com.binance.client.model.enums.StopOrderOperator find(String operator) {
        for (com.binance.client.model.enums.StopOrderOperator op : com.binance.client.model.enums.StopOrderOperator.values()) {
            if (op.getOperator().equals(operator)) {
                return op;
            }
        }
        return null;
    }

}
