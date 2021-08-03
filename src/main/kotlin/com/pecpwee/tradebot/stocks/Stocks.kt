package com.pecpwee.tradebot.stocks

import org.springframework.stereotype.Component
import yahoofinance.YahooFinance
import yahoofinance.histquotes.Interval
import java.util.*
import javax.annotation.PostConstruct


//@Component
class Stocks {

    @PostConstruct
    fun init() {
        val today = Calendar.getInstance()
        today.set(Calendar.YEAR, 2021)
        today.set(Calendar.MONTH, 6)
        today.set(Calendar.DATE, 29)

        val from = today.clone() as Calendar
        from.add(Calendar.YEAR, -1)


        val goog = YahooFinance.get("GOOG", from, today, Interval.DAILY)

        for (histQuote in goog.history) {
            println(histQuote)
        }

    }


}