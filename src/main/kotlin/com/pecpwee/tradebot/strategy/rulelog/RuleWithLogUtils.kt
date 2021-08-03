package com.pecpwee.tradebot.strategy.rulelog

import com.pecpwee.tradebot.strategy.AbsStrategy
import org.apache.logging.log4j.Logger
import org.ta4j.core.Indicator
import org.ta4j.core.Rule
import org.ta4j.core.TradingRecord
import org.ta4j.core.indicators.AbstractIndicator
import org.ta4j.core.indicators.SMAIndicator
import org.ta4j.core.indicators.statistics.SimpleLinearRegressionIndicator
import org.ta4j.core.num.Num
import org.ta4j.core.rules.AndRule
import org.ta4j.core.rules.CrossedDownIndicatorRule
import org.ta4j.core.rules.CrossedUpIndicatorRule
import org.ta4j.core.rules.OrRule


fun AbsStrategy.buildCallbackRecord(desc: String): () -> Unit {
    return { addStrategyDescCache(desc) }
}


class SimpleLinearRegressionIndicatorWithLog(
    indicator: Indicator<Num>?,
    barCount: Int,
    type: SimpleLinearRegressionType?, val logger: Logger?
) : SimpleLinearRegressionIndicator(indicator, barCount, type) {
    override fun calculate(index: Int): Num {
        val nul = super.calculate(index)
        if (logger != null) {
            logger.info("SimpleLinearRegressionIndicator value:$nul")
        }
        return nul
    }

}


class SMAIndicatorLog(
    indicator: Indicator<Num>?, val barCount: Int,
    val logger: Logger? = null
) : SMAIndicator(indicator, barCount) {
    override fun calculate(index: Int): Num {
        val nul = super.calculate(index)
        if (logger != null) {
            logger.info("SMA $barCount value:$nul")
        }
        return nul
    }

}

class AndRuleWithLog(rule1: Rule?, rule2: Rule?, val callback: () -> Unit = {}) : AndRule(rule1, rule2) {
    override fun isSatisfied(index: Int, tradingRecord: TradingRecord?): Boolean {
        val isSatisfied = super.isSatisfied(index, tradingRecord)
        if (isSatisfied) {
            callback()
        }
        return isSatisfied
    }
}

class OrRuleWithLog(rule1: Rule?, rule2: Rule?, val callback: () -> Unit = {}) : OrRule(rule1, rule2) {

    override fun isSatisfied(index: Int, tradingRecord: TradingRecord?): Boolean {
        val isSatisfied = super.isSatisfied(index, tradingRecord)
        if (isSatisfied) {
            callback()
        }
        return isSatisfied
    }
}

/**
 * TODO: 2021/6/7 rewrite with aspectj 。目前障碍：aspectJ无法处理talib这个第三方库的内容。原因待查明
 *
 */
class CrossedUpIndicatorRuleWithLog(
    first: Indicator<Num>?,
    second: Indicator<Num>?,
    val callback: () -> Unit = {},
) : CrossedUpIndicatorRule(first, second) {

    override fun isSatisfied(index: Int, tradingRecord: TradingRecord?): Boolean {
        val isSatisfied = super.isSatisfied(index, tradingRecord)
        if (isSatisfied) {
            callback()
        }
        return isSatisfied
    }
}

class CrossDownIndicatorRuleWithLog(
    first: AbstractIndicator<Num>?,
    second: AbstractIndicator<Num>?,
    val callback: () -> Unit = {},
) : CrossedDownIndicatorRule(first, second) {

    private val mStrategyName = this::class.java.simpleName


    override fun isSatisfied(index: Int, tradingRecord: TradingRecord?): Boolean {
        val isSatisfied = super.isSatisfied(index, tradingRecord)
        if (isSatisfied) {
            callback()
        }
        return isSatisfied
    }
}