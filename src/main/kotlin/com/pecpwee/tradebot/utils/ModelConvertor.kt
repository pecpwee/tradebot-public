package com.pecpwee.tradebot.utils

import com.binance.api.client.domain.event.CandlestickEvent
import com.binance.api.client.domain.market.Candlestick
import com.binance.api.client.domain.market.CandlestickInterval
import com.google.gson.GsonBuilder
import org.knowm.xchange.binance.dto.marketdata.BinanceKline
import org.knowm.xchange.binance.dto.marketdata.KlineInterval
import org.knowm.xchange.currency.CurrencyPair
import org.ta4j.core.BarSeries
import org.ta4j.core.BaseBar
import org.ta4j.core.BaseBarSeries
import org.ta4j.core.num.DecimalNum
import java.math.BigDecimal
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*


fun List<BinanceKline>.convert2TaLibBarSeries(timePeriod: KlineInterval): BarSeries {
    return this.convert2TaLibBarSeries(Duration.ofMillis(timePeriod.millis))
}

fun Date.convertToISO8601Str(): String {
    val date = this
    val sdf: SimpleDateFormat
    sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    sdf.timeZone = TimeZone.getTimeZone("CET")
    val text = sdf.format(date)
    return text
}

fun BarSeries.addAllKlines(klines: List<BinanceKline>, timePeriod: Duration): BarSeries {
    klines.forEach {
        try {
            this.addBar(it.toTALibBar(timePeriod))
        } catch (e: IllegalArgumentException) {
//            e.printStackTrace()
        }
    }
    return this
}

fun List<BinanceKline>.convert2TaLibBarSeries(timePeriod: Duration): BarSeries {
    val series: BarSeries = BaseBarSeries()
    this.forEach {
        try {
            series.addBar(it.toTALibBar(timePeriod))
        } catch (e: IllegalArgumentException) {
//            e.printStackTrace()
        }
    }
    return series
}

fun CurrencyPair.toNoneSplitPair(): String {
    return this.toString().replace("/", "")

}

fun BinanceKline.toTALibBar(timePeriod: Duration): BaseBar {
    val instant = Instant.ofEpochMilli(this.closeTime)
    val bar = createBar(
        timePeriod,
        ZonedDateTime.ofInstant(instant, ZoneId.of("Asia/Shanghai")),
        this.openPrice.toFloat(),
        this.highPrice.toFloat(),
        this.lowPrice.toFloat(),
        this.closePrice.toFloat(),
        this.volume.toFloat()
    )
    return bar
}

fun KlineInterval.toBinanceRawSocketInterval(): CandlestickInterval {
    for (day in CandlestickInterval.values()) {
        if (day.intervalId.equals(this.code())) {
            return day
        }
    }
    throw RuntimeException("time convert error")

}

fun KlineInterval.toMinutesCount(): Long {

    return this.millis / 1000 / 60
}

fun Candlestick.toTALibBar(timePeriod: Duration): BaseBar {
    val instant = Instant.ofEpochMilli(this.closeTime)
    val bar = createBar(
        timePeriod,
        ZonedDateTime.ofInstant(instant, ZoneId.of("Asia/Shanghai")),
        this.open.toFloat(),
        this.high.toFloat(),
        this.low.toFloat(),
        this.close.toFloat(),
        this.volume.toFloat()
    )
    return bar
}

fun ZonedDateTime.toReadableString(): String {
    val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss")
    return this.format(formatter)
}


fun Any.toPrettyJson(): String {
    val gson = GsonBuilder().setPrettyPrinting().serializeNulls().create()
    return gson.toJson(this)
}

fun CandlestickEvent.toTALibBar(timePeriod: Duration): BaseBar {
    val instant = Instant.ofEpochMilli(this.closeTime)
    val bar = createBar(
        timePeriod,
        ZonedDateTime.ofInstant(instant, ZoneId.of("Asia/Shanghai")),
        this.open.toFloat(),
        this.high.toFloat(),
        this.low.toFloat(),
        this.close.toFloat(),
        this.volume.toFloat()
    )
    return bar

}

private fun createBar(
    timePeriod: Duration, endTime: ZonedDateTime, openPrice: Number, highPrice: Number, lowPrice: Number,
    closePrice: Number, volume: Number
): BaseBar {

    return BaseBar.builder().endTime(endTime)
        .timePeriod(timePeriod)
        .openPrice(DecimalNum.valueOf(openPrice))
        .highPrice(DecimalNum.valueOf(highPrice))
        .lowPrice(DecimalNum.valueOf(lowPrice))
        .closePrice(DecimalNum.valueOf(closePrice))
        .volume(DecimalNum.valueOf(volume))
        .build()

}

fun Long.toDate(): Date {
    val instant = Instant.ofEpochMilli(this)
    val date = Date.from(instant)
    return date
}


fun Long.toDateStr(): String {
    val instant = Instant.ofEpochMilli(this)
//    val odt: OffsetDateTime = instant.atOffset(ZoneOffset.UTC)

    val DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        .withZone(ZoneId.of("Asia/Shanghai"))

//    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
//    val dateStr = formatter.format(Date(this))

    val zdt = instant.atZone(ZoneId.of("Asia/Shanghai"))
    return DATE_TIME_FORMATTER.format(zdt)
}

fun String.fromDateStrToTimestamp(): Long {
    return this.fromDateStrToDate().time
}


fun String.fromDateStrToDate(): Date {
    val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
    val date: Date = dateFormat.parse(this)
    return date
}

private fun String.to2DecimalFloatNumStr(): String {
    val price = this.toDouble()
    val str: String = String.format("%.2f", price)
    return str
}

fun Double.roundToSignificantFigures(n: Int): Double {
    if (this == 0.0) {
        return 0.0
    }
    val d = Math.ceil(Math.log10(if (this < 0) -this else this))
    val power = n - d.toInt()
    val magnitude = Math.pow(10.0, power.toDouble())
    val shifted = Math.round(this * magnitude)
    return shifted / magnitude
}

fun Double.getAvailableNumberMagnitude(n: Int): Double {
    if (this == 0.0) {
        return 0.0
    }
    val d = Math.ceil(Math.log10(if (this < 0) -this else this))
    val power = n - d.toInt()
    val magnitude = Math.pow(10.0, power.toDouble())
    return magnitude
}

fun Double.getShiftedAvailableNumberInLong(n: Int): Long {
    if (this == 0.0) {
        return 0L
    }
    val d = Math.ceil(Math.log10(if (this < 0) -this else this))
    val power = n - d.toInt()
    val magnitude = Math.pow(10.0, power.toDouble())
    val shifted = Math.round(this * magnitude)
    return shifted
}

fun BigDecimal.remainDigitalAnd2Str(): String {
    val df1 = DecimalFormat("0.00")
    val str: String = df1.format(this)
    return str
}

fun Double.remainDecimal(scale: Int): String {
    val d = BigDecimal(this)
    return d.setScale(scale, BigDecimal.ROUND_HALF_UP).toString()
}