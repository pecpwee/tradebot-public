package com.pecpwee.tradebot.strategy

import com.pecpwee.tradebot.strategy.rulelog.CrossDownIndicatorRuleWithLog
import com.pecpwee.tradebot.strategy.rulelog.CrossedUpIndicatorRuleWithLog
import org.knowm.xchange.binance.dto.marketdata.KlineInterval
import org.knowm.xchange.currency.CurrencyPair
import org.springframework.stereotype.Component
import org.ta4j.core.BarSeries
import org.ta4j.core.BaseStrategy
import org.ta4j.core.indicators.ATRIndicator
import org.ta4j.core.indicators.CachedIndicator
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import org.ta4j.core.indicators.helpers.ConstantIndicator
import org.ta4j.core.indicators.helpers.MedianPriceIndicator
import org.ta4j.core.num.DecimalNum
import org.ta4j.core.num.Num
import org.ta4j.core.rules.CrossedDownIndicatorRule
import org.ta4j.core.rules.CrossedUpIndicatorRule
import java.lang.RuntimeException

//@Component
abstract class SuperTrendStrategy : AbsStrategy() {
    override fun buildStrategy(series: BarSeries?): BaseStrategy {
        if (series == null) {
            throw RuntimeException("null bar series")
        }
        val theSuperStrategy = SuperTrendIndicator(
            series
        )
        val close = ClosePriceIndicator(series)
        val constantIndicator = ConstantIndicator<Num>(series, DecimalNum.valueOf(0))
        val entryRule = CrossedUpIndicatorRuleWithLog(
            theSuperStrategy, constantIndicator, buildCallbackRecord("", series)
        )//大于0，就是处于上升了吧
        val exitRule = CrossDownIndicatorRuleWithLog(
            theSuperStrategy, constantIndicator, buildCallbackRecord("", series)
        )
        return BaseStrategy(entryRule, exitRule)
    }

    fun buildCallbackRecord(desc: String, series: BarSeries): () -> Unit {
        return { addStrategyDescCache("$desc ${series.lastBar.closePrice}") }
    }


}


//@Component
//class SuperTrend1Min : SuperTrendStrategy() {
//    override fun getBarReuqestConfig(): BarRequestConfig {
//        return BarRequestConfig(TradePair.BTCUSDT, CandlestickInterval.ONE_MINUTE, Duration.ofMinutes(1), 700)
//    }
//}

@Component
class SuperTrend1Hour : SuperTrendStrategy() {
    override fun getBarReuqestConfig(): BarRequestConfig {
        return BarRequestConfig(CurrencyPair.BTC_USDT, KlineInterval.h1, isNeedBacktest = false)
    }

}

@Component
class SuperTrend5Min : SuperTrendStrategy() {


    override fun getBarReuqestConfig(): BarRequestConfig {
        return BarRequestConfig(
            tradePair = CurrencyPair.BTC_USDT,
            candlestickInterval = KlineInterval.m5,
            isNeedBacktest = false
        )
    }
}

@Component
class SuperTrend15Min : SuperTrendStrategy() {


    override fun getBarReuqestConfig(): BarRequestConfig {
        return BarRequestConfig(
            tradePair = CurrencyPair.BTC_USDT,
            candlestickInterval = KlineInterval.m15,
            isNeedBacktest = false
        )
    }
}

class SuperTrendIndicator : CachedIndicator<Num> {
    lateinit var closePriceIndicator: ClosePriceIndicator
    lateinit var trendDownTopIndicator: TrendDownTopIndicator
    lateinit var trendUpBottomIndicator: TrendUpBottomIndicator


    constructor(barSeries: BarSeries, atrPeriod: Int = 15, orbitMultiVal: Double = 3.0) : super(barSeries) {
        closePriceIndicator = ClosePriceIndicator(barSeries)
        trendDownTopIndicator =
            TrendDownTopIndicator(barSeries, closePriceIndicator, atrPeriod = atrPeriod, orbitMultiVal = orbitMultiVal)
        trendUpBottomIndicator =
            TrendUpBottomIndicator(barSeries, closePriceIndicator, atrPeriod = atrPeriod, orbitMultiVal = orbitMultiVal)
    }


    override fun calculate(index: Int): Num {
        var trend = numOf(1) //默认为上升趋势


        trend = if (index == 0) trend else getValue(index - 1)

        val theClose = closePriceIndicator.getValue(index)
        val theUpperOrbit = trendDownTopIndicator.getValue(index)
        val theLowerOrbit = trendUpBottomIndicator.getValue(index)
        if (trend.equals(DecimalNum.valueOf(-1)) && theClose > theUpperOrbit) { //下降趋势里，close 超过上轨道，那么扭转为上升趋势
            trend = numOf(1)
        } else if (trend.equals(DecimalNum.valueOf(1)) && theClose < theLowerOrbit) { //上升趋势里，close跌破下轨道，那么扭转为下降趋势
            trend = numOf(-1)
        }

        return trend

//        return trend.multipliedBy(numOf(100))
    }
}


//上升趋势计算下半部分轨道
class TrendUpBottomIndicator(
    barSeries: BarSeries,
    val closePriceIndicator: ClosePriceIndicator = ClosePriceIndicator(barSeries),
    val atrPeriod: Int = 10,
    val orbitMultiVal: Double = 3.1

) :
    CachedIndicator<Num>(barSeries) {

    val medianPriceIndicator = MedianPriceIndicator(barSeries)
    val atrIndicator = ATRIndicator(barSeries, atrPeriod)


    override fun calculate(index: Int): Num {
        val theMid = medianPriceIndicator.getValue(index)
        val atr = atrIndicator.getValue(index)
        val theNewLowerOrbit = theMid - atr.multipliedBy(DecimalNum.valueOf(orbitMultiVal))
        val theLastLowOrbit = if (index == 0) theNewLowerOrbit else getValue(index - 1)
        val result = if (theNewLowerOrbit > theLastLowOrbit) theNewLowerOrbit else theLastLowOrbit //下半部分轨道只能更高，不可能下降


        val theLastClose = closePriceIndicator.getValue(index - 1)
        if (theLastClose > theLastLowOrbit) {//上次仍然在上升趋势，那么就是这个结果
            return result
        } else { //上次趋势翻转了，取本次最新结果，即重新设定值了，趋势已经变化了
            return theNewLowerOrbit
        }
    }
}

//下降趋势计算上轨道
class TrendDownTopIndicator(
    barSeries: BarSeries,
    val closePriceIndicator: ClosePriceIndicator = ClosePriceIndicator(barSeries),
    val atrPeriod: Int = 144,
    val orbitMultiVal: Double = 3.1
) :
    CachedIndicator<Num>(barSeries) {

    val medianPriceIndicator = MedianPriceIndicator(barSeries)
    val atrIndicator = ATRIndicator(barSeries, atrPeriod)


    override fun calculate(index: Int): Num {
        val theMid = medianPriceIndicator.getValue(index)
        val atr = atrIndicator.getValue(index)
        val theNewUpperOribit = theMid + atr.multipliedBy(DecimalNum.valueOf(orbitMultiVal))

        val theLastUpperOrbit = if (index == 0) theNewUpperOribit else getValue(index - 1)
        val result: Num = if (theNewUpperOribit < theLastUpperOrbit) theNewUpperOribit else theLastUpperOrbit


        val theLastClose = closePriceIndicator.getValue(index - 1)
        if (theLastClose < theLastUpperOrbit) {//上次仍然在下降
            return result
        } else { //上次趋势翻转了，取本次最新
            return theNewUpperOribit
        }
    }
}


class SuperTrendWrapperIndicator(barSeries: BarSeries) : CachedIndicator<Num>(barSeries) {


    val superTrendIndicator = SuperTrendIndicator(barSeries)
    val closePriceIndicator = ClosePriceIndicator(barSeries)
    val trendDownTopIndicator: TrendDownTopIndicator = TrendDownTopIndicator(barSeries)
    val trendUpBottomIndicator: TrendUpBottomIndicator = TrendUpBottomIndicator(barSeries)

    override fun calculate(index: Int): Num {

        if (superTrendIndicator.getValue(index).isPositive) {
            return trendUpBottomIndicator.getValue(index)
        } else {
            return trendDownTopIndicator.getValue(index)
        }

    }
}

