package com.pecpwee.tradebot.strategy.analysis

import com.pecpwee.tradebot.strategy.AbsStrategy
import org.knowm.xchange.binance.dto.marketdata.KlineInterval
import org.knowm.xchange.currency.CurrencyPair
import org.ta4j.core.BarSeries
import org.ta4j.core.BaseStrategy
import org.ta4j.core.indicators.SMAIndicator
import org.ta4j.core.indicators.helpers.VolumeIndicator
import org.ta4j.core.num.DecimalNum
import org.ta4j.core.num.Num
import org.ta4j.core.rules.CrossedDownIndicatorRule
import org.ta4j.core.rules.CrossedUpIndicatorRule

/***
 * 异常交易量提醒策略
 */
//@Component
class AbnormalVolumeStrategy : AbsStrategy() {


    protected lateinit var maVolume: SMAIndicator

    override fun buildStrategy(series: BarSeries?): BaseStrategy {

        val volume = VolumeIndicator(series)

        // The bias is bullish when the shorter-moving average moves above the longer
        // moving average.
        // The bias is bearish when the shorter-moving average moves below the longer
        // moving average.
        maVolume = object : SMAIndicator(volume, 20) {
            override fun calculate(index: Int): Num {
                val value = super.calculate(index)
                return value.multipliedBy(DecimalNum.valueOf(3))
            }

        }


        // Exit rule
        val entryRule = CrossedUpIndicatorRule(
            volume, maVolume
        )
        val exitRule = CrossedDownIndicatorRule(volume, maVolume) // Trend
        return BaseStrategy(entryRule, exitRule)

    }


    override fun getBarReuqestConfig(): BarRequestConfig {

        return BarRequestConfig(CurrencyPair.BTC_USDT, KlineInterval.m5, isNeedBacktest = false)
    }


}