package com.pecpwee.tradebot.strategy

import org.knowm.xchange.binance.dto.marketdata.KlineInterval
import org.knowm.xchange.currency.CurrencyPair
import org.ta4j.core.BarSeries
import org.ta4j.core.BaseStrategy
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import org.ta4j.core.rules.*


//@Component
class RecentDayBreakStrategy : AbsStrategy() {

    lateinit var strategy: BaseStrategy

    override fun buildStrategy(series: BarSeries?): BaseStrategy {
        requireNotNull(series) { "Series cannot be null" }
        val closePrice = ClosePriceIndicator(series)

        // The bias is bullish when the shorter-moving average moves above the longer
        // moving average.
        // The bias is bearish when the shorter-moving average moves below the longer
        // moving average.


        // Entry rule
        val entryRule = IsHighestRule(closePrice, 3) // Trend
        val selling1Rule = IsLowestRule(closePrice, 3) // Trend

        // Exit rule


        // Selling rules
        // We want to sell:
        // - if the 5-bars SMA crosses under 30-bars SMA
        // - or if the price loses more than 3%
        // - or if the price earns more than 2%
//        val sellingRule: Rule = StopLossRule(closePrice, series.numOf(3)).or(StopGainRule(closePrice, series.numOf(2)))
//        val selling1Rule: Rule =
//            TrailingStopLossRule(closePrice, series.numOf(3)).or(StopGainRule(closePrice, series.numOf(2)))

        return BaseStrategy(entryRule, selling1Rule)
    }

    override fun getBarReuqestConfig(): BarRequestConfig {
        return BarRequestConfig(CurrencyPair.BTC_USDT, KlineInterval.m5, isNeedBacktest = false)
    }


}