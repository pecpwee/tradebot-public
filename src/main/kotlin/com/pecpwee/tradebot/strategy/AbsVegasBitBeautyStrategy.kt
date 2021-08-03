package com.pecpwee.tradebot.strategy

import org.knowm.xchange.binance.dto.marketdata.KlineInterval
import org.knowm.xchange.currency.CurrencyPair
import org.ta4j.core.BarSeries
import org.ta4j.core.BaseStrategy
import org.ta4j.core.indicators.EMAIndicator
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import org.ta4j.core.rules.CrossedDownIndicatorRule
import org.ta4j.core.rules.CrossedUpIndicatorRule
import java.time.Duration

abstract class AbsVegasBitBeautyStrategy : AbsStrategy() {
    protected lateinit var ema144: EMAIndicator
    protected lateinit var ema169: EMAIndicator
    protected lateinit var ema576: EMAIndicator
    protected lateinit var ema676: EMAIndicator
    protected lateinit var ema12: EMAIndicator
    override fun buildStrategy(series: BarSeries?): BaseStrategy {
        val closePrice = ClosePriceIndicator(series)
        ema144 = EMAIndicator(closePrice, 144)
        ema169 = EMAIndicator(closePrice, 169)
        ema576 = EMAIndicator(closePrice, 576)
        ema676 = EMAIndicator(closePrice, 676)
        ema12 = EMAIndicator(closePrice, 1)


        // Entry rule
        val entryRule = CrossedUpIndicatorRule(ema12, ema144)
//            .or(CrossedUpIndicatorRule(ema12, ema169))
//            .or(CrossedUpIndicatorRule(ema12, ema576)).or(CrossedUpIndicatorRule(ema12, ema676))

        // Exit rule
        val exitRule = CrossedDownIndicatorRule(ema12, ema169)
//            .or(CrossedDownIndicatorRule(ema12, ema169))
//            .or(CrossedDownIndicatorRule(ema12, ema576)).or(CrossedDownIndicatorRule(ema12, ema676))

        return BaseStrategy(entryRule, exitRule)
    }


}

open class Vegas1Minutes : AbsVegasBitBeautyStrategy() {


    val buyOrderSet = hashSetOf<com.binance.client.model.trade.Order>()
    val stoplossOrderSet = hashSetOf<com.binance.client.model.trade.Order>()


    override fun getBarReuqestConfig(): BarRequestConfig {
        return BarRequestConfig(CurrencyPair.BTC_USDT, KlineInterval.m1, Duration.ofMinutes(1), 1200)
    }
}


//@Component
class Vegas5Min : AbsVegasBitBeautyStrategy() {
    override fun getBarReuqestConfig(): BarRequestConfig {
        return BarRequestConfig(CurrencyPair.BTC_USDT, KlineInterval.m5, Duration.ofMinutes(5), 700)
    }
}
