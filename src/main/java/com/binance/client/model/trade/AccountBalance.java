package com.binance.client.model.trade;

import com.binance.client.constant.BinanceApiConstants;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.math.BigDecimal;

public class AccountBalance {

    private String asset;

    private BigDecimal balance;

    //crossWalletBalance 全仓余额
    private BigDecimal crossWalletBalance;

    // 全仓持仓未实现盈亏
    private BigDecimal crossUnPnl;
    //// 可用余额
    private BigDecimal availableBalance;
    //最大可用余额
    private BigDecimal maxWithdrawAmount;

    public String getAsset() {
        return asset;
    }

    public void setAsset(String asset) {
        this.asset = asset;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }



    @Override
    public String toString() {
        return new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE).append("asset", asset)
                .append("balance", balance).append("crossWalletBalance", crossWalletBalance).toString();
    }

    public BigDecimal getCrossUnPnl() {
        return crossUnPnl;
    }

    public void setCrossUnPnl(BigDecimal crossUnPnl) {
        this.crossUnPnl = crossUnPnl;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(BigDecimal availableBalance) {
        this.availableBalance = availableBalance;
    }

    public BigDecimal getMaxWithdrawAmount() {
        return maxWithdrawAmount;
    }

    public void setMaxWithdrawAmount(BigDecimal maxWithdrawAmount) {
        this.maxWithdrawAmount = maxWithdrawAmount;
    }

    public BigDecimal getCrossWalletBalance() {
        return crossWalletBalance;
    }

    public void setCrossWalletBalance(BigDecimal crossWalletBalance) {
        this.crossWalletBalance = crossWalletBalance;
    }
}
