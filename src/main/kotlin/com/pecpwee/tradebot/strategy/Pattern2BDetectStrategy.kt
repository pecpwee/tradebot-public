package com.pecpwee.tradebot.strategy

import com.pecpwee.tradebot.utils.toReadableString
import org.apache.logging.log4j.Logger
import org.knowm.xchange.binance.dto.marketdata.KlineInterval
import org.knowm.xchange.currency.CurrencyPair
import org.springframework.stereotype.Component
import org.ta4j.core.BarSeries
import org.ta4j.core.BaseStrategy
import org.ta4j.core.indicators.CachedIndicator
import org.ta4j.core.indicators.EMAIndicator
import org.ta4j.core.indicators.SMAIndicator
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import org.ta4j.core.indicators.helpers.TRIndicator
import org.ta4j.core.indicators.helpers.VolumeIndicator
import org.ta4j.core.num.DecimalNum
import org.ta4j.core.num.Num
import org.ta4j.core.rules.CrossedDownIndicatorRule
import org.ta4j.core.rules.CrossedUpIndicatorRule
import java.lang.IllegalArgumentException

abstract class Pattern2BDetectStrategy : AbsStrategy() {


    override fun buildStrategy(series: BarSeries?): BaseStrategy {

        if (series == null) {
            throw IllegalArgumentException("series not null")
        }
        val pattern2BIndicator = Pattern2BIndicator(series, logger)

        val entryRule = CrossedUpIndicatorRule(pattern2BIndicator, DecimalNum.valueOf(0.5))//大于0，就是处于上升了吧
        val exitRule = CrossedDownIndicatorRule(pattern2BIndicator, DecimalNum.valueOf(-0.5))

        return BaseStrategy(entryRule, exitRule)


    }


//    override fun getBarReuqestConfig(): BarRequestConfig {
//        return BarRequestConfig(CurrencyPair.BTC_USDT, KlineInterval.m5, isNeedBacktest = true)
//    }
}


@Component
class Min5Pattern2BDetectStrategy : Pattern2BDetectStrategy() {


    override fun getBarReuqestConfig(): BarRequestConfig {
        return BarRequestConfig(CurrencyPair.BTC_USDT, KlineInterval.m5, isNeedBacktest = false)
    }
}

//@Component
class Min15Pattern2BDetectStrategy : Pattern2BDetectStrategy() {
    override fun getBarReuqestConfig(): BarRequestConfig {
        return BarRequestConfig(CurrencyPair.BTC_USDT, KlineInterval.m15, isNeedBacktest = false)
    }
}

class Pattern2BIndicator : CachedIndicator<Num> {
    private val TAG = this::class.java.simpleName
    var closePriceIndicator: ClosePriceIndicator
    var volumeIndicator: VolumeIndicator
    var sma6VolIndicator: SMAIndicator
    var fractalVolumeChangeIndicator: FractalVolumeChangeIndicator
    var fractalUpIndicator: FractalUpIndicator
    var fractalDownIndicator: FractalDownIndicator
    var ema20: EMAIndicator
    var atrEma6: EMAIndicator
    var logger: Logger

    constructor(barSeries: BarSeries, logger: Logger) : super(barSeries) {
        this.closePriceIndicator = ClosePriceIndicator(barSeries)
        this.volumeIndicator = VolumeIndicator(barSeries)
        this.sma6VolIndicator = SMAIndicator(volumeIndicator, 6)
        this.fractalVolumeChangeIndicator = FractalVolumeChangeIndicator(barSeries)
        this.fractalUpIndicator = FractalUpIndicator(barSeries, fractalVolumeChangeIndicator)
        this.fractalDownIndicator = FractalDownIndicator(barSeries, fractalVolumeChangeIndicator)
        this.ema20 = EMAIndicator(closePriceIndicator, 20)
        this.atrEma6 = EMAIndicator(TRIndicator(barSeries), 6)
        this.logger = logger
    }


    /***
     * 1:UP2B
     * -1:DOWN2B
     * 0:NONE-PATTERN
     */
    override fun calculate(index: Int): Num {
        if (index < 5) {
            return numOf(0)
        }

        if (index == barSeries.endIndex) {
            logger.debug("$TAG new calculate:check latest bar")
        } else if (index == barSeries.endIndex - 1) {
            logger.debug("$TAG new calculate:check -1 index bar")
        }


        val isFractalsUp = barSeries.getBar(index - 3).highPrice.isGreaterThan(barSeries.getBar(index - 4).highPrice)
                && barSeries.getBar(index - 4).highPrice.isGreaterThan(barSeries.getBar(index - 5).highPrice)
                && barSeries.getBar(index - 2).highPrice.isLessThan(barSeries.getBar(index - 3).highPrice)
                && barSeries.getBar(index - 1).highPrice.isLessThan(barSeries.getBar(index - 2).highPrice)
                && fractalVolumeChangeIndicator.getValue(index - 3).isGreaterThan(numOf(5))

        val fractalsDonw = barSeries.getBar(index - 3).lowPrice.isLessThan(barSeries.getBar(index - 4).lowPrice)
                && barSeries.getBar(index - 4).lowPrice.isLessThan(barSeries.getBar(index - 5).lowPrice)
                && barSeries.getBar(index - 2).lowPrice.isGreaterThan(barSeries.getBar(index - 3).lowPrice)
                && barSeries.getBar(index - 1).lowPrice.isGreaterThan(barSeries.getBar(index - 2).lowPrice)
                && fractalVolumeChangeIndicator.getValue(index - 3).isGreaterThan(numOf(5))

        val upA = fractalDownIndicator.getValue(index - 1)
        val upB = fractalUpIndicator.getValue(index)
        val upC = fractalDownIndicator.getValue(index)
        val downA = fractalUpIndicator.getValue(index - 1)
        val downB = fractalDownIndicator.getValue(index)
        val downC = fractalUpIndicator.getValue(index)
        val _2B = closePriceIndicator.getValue(index)

        val up2B = upC.isLessThan(upA) && upB.isGreaterThan(upA) && _2B.isLessThan(upB) && _2B.isGreaterThan(upC)
        val down2B =
            downC.isGreaterThan(downA) && downB.isLessThan(downA) && _2B.isGreaterThan(downB) && _2B.isLessThan(downC)


        val reduceNoiseUp =
            fractalsDonw && upA.isLessThan(ema20.getValue(index))
                    && upB.isGreaterThan(ema20.getValue(index))
                    && upC.isLessThan(ema20.getValue(index))
                    && ((upA.minus(upB)).abs()).isGreaterThan(atrEma6.getValue(index).multipliedBy(numOf(4)))
        val reduceNoiseDown =
            isFractalsUp && downA.isGreaterThan(ema20.getValue(index))
                    && downB.isLessThan(ema20.getValue(index))
                    && downC.isGreaterThan(ema20.getValue(index))
                    && ((downA.minus(downB)).abs()).isGreaterThan(atrEma6.getValue(index).multipliedBy(numOf(4)))

        val fractalUpTrend = fractalsDonw
        val fractalDownTrend = isFractalsUp


        logger.debug(
            "$TAG ${barSeries.getBar(index).beginTime.toReadableString()} \n" +
                    "close：${closePriceIndicator.getValue(index)}\n" +
                    "UP parameters:$fractalUpTrend,$reduceNoiseUp,$up2B\n" +
                    "DOWN parameters:$fractalDownTrend,$reduceNoiseDown,$down2B"
        )

        if (fractalUpTrend && reduceNoiseUp && up2B) {
            logger.debug(
                "$TAG buy"
            )
            return numOf(1)
        }
        if (fractalDownTrend && reduceNoiseDown && down2B) {
            logger.debug(
                "$TAG sell"
            )

            return numOf(-1)
        }

        logger.debug(
            "$TAG no pattern met"
        )

        return numOf(0)
    }

}

class FractalVolumeChangeIndicator(
    barSeries: BarSeries
) : CachedIndicator<Num>(barSeries) {
    val volumeIndicator = VolumeIndicator(barSeries)
    val sma6VolIndicator = SMAIndicator(volumeIndicator, 6)
    override fun calculate(index: Int): Num {
        val fractalVolumeChange =
            (volumeIndicator.getValue(index).minus(sma6VolIndicator.getValue(index))).dividedBy(
                sma6VolIndicator.getValue(index)
            ).multipliedBy(
                numOf(100)
            )
        return fractalVolumeChange
    }
}

class FractalUpIndicator(
    barSeries: BarSeries,
    val fractalVolumeChangeIndicator: FractalVolumeChangeIndicator
) : CachedIndicator<Num>(barSeries) {
    override fun calculate(index: Int): Num {
        if (index < 5) {
            return numOf(0)
        }
        val isFractalsUp = barSeries.getBar(index - 3).highPrice.isGreaterThan(barSeries.getBar(index - 4).highPrice)
                && barSeries.getBar(index - 4).highPrice.isGreaterThan(barSeries.getBar(index - 5).highPrice)
                && barSeries.getBar(index - 2).highPrice.isLessThan(barSeries.getBar(index - 3).highPrice)
                && barSeries.getBar(index - 1).highPrice.isLessThan(barSeries.getBar(index - 2).highPrice)
                && fractalVolumeChangeIndicator.getValue(index - 3).isGreaterThan(numOf(5))

        if (isFractalsUp) {
            return barSeries.getBar(index - 3).highPrice
        } else {
            return this.getValue(index - 1)

        }

    }
}

class FractalDownIndicator(
    barSeries: BarSeries,
    val fractalVolumeChangeIndicator: FractalVolumeChangeIndicator

) : CachedIndicator<Num>(barSeries) {
    override fun calculate(index: Int): Num {

        if (index < 5) {
            return numOf(0)
        }
        val fractalsDown = barSeries.getBar(index - 3).lowPrice.isLessThan(barSeries.getBar(index - 4).lowPrice)
                && barSeries.getBar(index - 4).lowPrice.isLessThan(barSeries.getBar(index - 5).lowPrice)
                && barSeries.getBar(index - 2).lowPrice.isGreaterThan(barSeries.getBar(index - 3).lowPrice)
                && barSeries.getBar(index - 1).lowPrice.isGreaterThan(barSeries.getBar(index - 2).lowPrice)
                && fractalVolumeChangeIndicator.getValue(index - 3).isGreaterThan(numOf(5))
        if (fractalsDown) {
            return barSeries.getBar(index - 3).lowPrice
        } else {
            return this.getValue(index - 1)
        }
    }
}