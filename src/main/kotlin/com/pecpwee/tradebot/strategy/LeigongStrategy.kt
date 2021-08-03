package com.pecpwee.tradebot.strategy

import com.pecpwee.tradebot.strategy.rulelog.*
import com.pecpwee.tradebot.utils.roundToSignificantFigures
import org.knowm.xchange.binance.dto.marketdata.KlineInterval
import org.knowm.xchange.currency.CurrencyPair
import org.springframework.stereotype.Component
import org.ta4j.core.*
import org.ta4j.core.indicators.AbstractIndicator
import org.ta4j.core.indicators.EMAIndicator
import org.ta4j.core.indicators.MACDIndicator
import org.ta4j.core.indicators.SMAIndicator
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import org.ta4j.core.indicators.helpers.ConstantIndicator
import org.ta4j.core.indicators.statistics.SimpleLinearRegressionIndicator
import org.ta4j.core.num.DecimalNum
import org.ta4j.core.num.Num
import org.ta4j.core.rules.*


/***
 * EMA20破线拐头（股价站上去了） MA20拐头，两均线同时向上就是牛市，
 * 关键性信号，要开始对冲
 * 交叉 要完全对冲一下
 * 空头排列 乖离率！
 * todo 均线密集指标
 *
 */
//@Component
class LeigongStrategy : AbsStrategy() {

    protected lateinit var closePrice: ClosePriceIndicator

    protected lateinit var sma20: SMAIndicator
    protected lateinit var ema20: EMAIndicator
    protected lateinit var sma60: SMAIndicator
    protected lateinit var ema60: EMAIndicator
    protected lateinit var sma120: SMAIndicator
    protected lateinit var ema120: EMAIndicator
    protected lateinit var macd: MACDIndicator
    protected lateinit var sma20linearRegressionSlope: SimpleLinearRegressionIndicator


    override fun getBackTestConfig(barSeries: BarSeries): BackTestConfig {
        return BackTestConfig(
            indicatorsToDraw = arrayListOf(
                sma20,
                sma60,
                sma120
            )
        )
    }

    override fun buildStrategy(series: BarSeries?): BaseStrategy {

        closePrice = ClosePriceIndicator(series)

        // The bias is bullish when the shorter-moving average moves above the longer
        // moving average.
        // The bias is bearish when the shorter-moving average moves below the longer
        // moving average.
        sma20 = SMAIndicatorLog(closePrice, 20, logger = logger)
        ema20 = EMAIndicator(closePrice, 20)
        sma60 = SMAIndicatorLog(closePrice, 60, logger = logger)
        ema60 = EMAIndicator(closePrice, 60)

        // Entry rule
        sma120 = SMAIndicatorLog(closePrice, 120, logger = logger) // Trend
        ema120 = EMAIndicator(closePrice, 120) // Trend
        macd = MACDIndicator(closePrice)


        sma20linearRegressionSlope = SimpleLinearRegressionIndicatorWithLog(
            sma20, 3, SimpleLinearRegressionIndicator.SimpleLinearRegressionType.SLOPE, logger
        )
        val zeroConstantIndicator = ConstantIndicator(series, DecimalNum.valueOf(0) as Num)

        val entryRule =
            CrossedUpIndicatorRuleWithLog(
                closePrice, sma20, buildCallbackRecord("Legong UP-1破线 ma20")
            ).or(
                AndRuleWithLog(
                    CrossedUpIndicatorRuleWithLog(
                        sma20linearRegressionSlope, zeroConstantIndicator,
                    ), OverIndicatorRule(closePrice, ema20), buildCallbackRecord("Legong UP-2拐头 ma20向上")
                )
            ).or(
                AndRuleWithLog(
                    CrossedUpIndicatorRule(
                        sma60, sma120
                    ), OverIndicatorRule(sma20, sma60)
                        .and(OverIndicatorRule(closePrice, sma20)), buildCallbackRecord(
                        "Legong UP-3 多头排列完成!"
                    )
                )
            )
        val exitRule =
            CrossDownIndicatorRuleWithLog(
                closePrice, sma20, buildCallbackRecord("Legong DOWN-1破线")
            ).or(
                AndRuleWithLog(
                    CrossDownIndicatorRuleWithLog(
                        sma20linearRegressionSlope, zeroConstantIndicator
                    ), UnderIndicatorRule(
                        closePrice, ema20
                    ), buildCallbackRecord(
                        "Legong DOWN-2拐头"
                    )
                ).or(
                    AndRuleWithLog(
                        CrossDownIndicatorRuleWithLog(
                            sma60, sma120
                        ),
                        UnderIndicatorRule(sma20, sma60).and(
                            UnderIndicatorRule(closePrice, sma20)
                        ),
                        buildCallbackRecord("Legong DOWN-3 空头排列完成")
                    )
                )
            )

        return BaseStrategy(entryRule, exitRule)


    }


    private fun getSMAValue(): String {
        return "\nclose:${closePrice.getValue(getLastClosedBarId())}\n" +
                "ma20:${sma20.getValue(getLastClosedBarId())}\n" +
                "ma60:${sma60.getValue(getLastClosedBarId())}\n" +
                "ma120:${sma120.getValue(getLastClosedBarId())}\n"
    }

    override fun getBarReuqestConfig(): BarRequestConfig {
        return BarRequestConfig(CurrencyPair.BTC_USDT, KlineInterval.m5, isNeedBacktest = false)
    }

}