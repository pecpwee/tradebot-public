package com.pecpwee.tradebot.strategy

import org.knowm.xchange.binance.dto.marketdata.KlineInterval
import org.knowm.xchange.currency.CurrencyPair
import org.ta4j.core.BarSeries
import org.ta4j.core.BaseStrategy
import org.ta4j.core.indicators.MACDIndicator
import org.ta4j.core.indicators.SMAIndicator
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import org.ta4j.core.rules.CrossedDownIndicatorRule
import org.ta4j.core.rules.CrossedUpIndicatorRule


//@Component
class MACrossStrategy : AbsStrategy() {


    override fun buildStrategy(series: BarSeries?): BaseStrategy {
        requireNotNull(series) { "Series cannot be null" }
        val closePrice = ClosePriceIndicator(series)

        val fastMacdIndicator = MACDIndicator(closePrice)
        val slowMacdIndicator = SMAIndicator(fastMacdIndicator, 9)


        val entryRule = CrossedUpIndicatorRule(fastMacdIndicator, slowMacdIndicator) // Trend
        val exitRule = CrossedDownIndicatorRule(fastMacdIndicator, slowMacdIndicator) // Trend

        return BaseStrategy(entryRule, exitRule)
    }

    override fun getBarReuqestConfig(): BarRequestConfig {
        return BarRequestConfig(CurrencyPair.BTC_USDT, KlineInterval.m5, isNeedBacktest = false)
    }


}