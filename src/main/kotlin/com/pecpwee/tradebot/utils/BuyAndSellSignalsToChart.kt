package com.pecpwee.tradebot.utils

import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.DateAxis
import org.jfree.chart.plot.Marker
import org.jfree.chart.plot.ValueMarker
import org.jfree.chart.plot.XYPlot
import org.jfree.data.time.Minute
import org.jfree.data.time.TimeSeries
import org.jfree.data.time.TimeSeriesCollection
import org.jfree.ui.ApplicationFrame
import org.jfree.ui.RefineryUtilities
import org.ta4j.core.*
import org.ta4j.core.indicators.helpers.ClosePriceIndicator
import org.ta4j.core.num.Num
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Dimension
import java.text.SimpleDateFormat
import java.util.*

/**
 * This class builds a graphical chart showing the buy/sell signals of a
 * strategy.
 */
object BuyAndSellSignalsToChart {
    /**
     * Builds a JFreeChart time series from a Ta4j bar series and an indicator.
     *
     * @param barSeries the ta4j bar series
     * @param indicator the indicator
     * @param name      the name of the chart time series
     * @return the JFreeChart time series
     */
    private fun buildChartTimeSeries(
        barSeries: BarSeries, indicator: Indicator<Num>,
        name: String
    ): TimeSeries {
        val chartTimeSeries = TimeSeries(name)
        for (i in 0 until barSeries.barCount) {
            val bar: Bar = barSeries.getBar(i)
            chartTimeSeries.add(
                Minute(Date.from(bar.endTime.toInstant())),
                indicator.getValue(i).doubleValue()
            )
        }
        return chartTimeSeries
    }

    /**
     * Runs a strategy over a bar series and adds the value markers corresponding to
     * buy/sell signals to the plot.
     *
     * @param series   the bar series
     * @param strategy the trading strategy
     * @param plot     the plot
     */
    private fun addBuySellSignals(series: BarSeries, strategy: Strategy, plot: XYPlot) {
        // Running the strategy
        val seriesManager = BarSeriesManager(series)
        val positions = seriesManager.run(strategy).positions
        // Adding markers to plot
        // Adding markers to plot
        for (position in positions) {
            // Buy signal
            val buySignalBarTime = Minute(
                Date.from(series.getBar(position.entry.index).endTime.toInstant())
            )
                .firstMillisecond.toDouble()
            val buyMarker: Marker = ValueMarker(buySignalBarTime)
            buyMarker.paint = Color.GREEN
            buyMarker.stroke = BasicStroke(1.3F)
            buyMarker.label = "B"
            plot.addDomainMarker(buyMarker)
            // Sell signal
            val sellSignalBarTime = Minute(
                Date.from(series.getBar(position.exit.index).endTime.toInstant())
            )
                .firstMillisecond.toDouble()
            val sellMarker: Marker = ValueMarker(sellSignalBarTime)
            sellMarker.paint = Color.RED
            sellMarker.label = "S"
            plot.addDomainMarker(sellMarker)
        }
    }

    /**
     * Displays a chart in a frame.
     *
     * @param chart the chart to be displayed
     */
    private fun displayChart(chart: JFreeChart) {
        // Chart panel
        val panel = ChartPanel(chart)
        panel.fillZoomRectangle = true
        panel.isMouseWheelEnabled = true
        panel.preferredSize = Dimension(1024, 400)
        // Application frame
        val frame = ApplicationFrame(title)
        frame.contentPane = panel
        frame.pack()
        RefineryUtilities.centerFrameOnScreen(frame)
        frame.isVisible = true
    }

    lateinit var title: String
    fun showWindows(
        series: BarSeries,
        strategy: Strategy,
        title: String,
        indicatorsToDraw: List<Indicator<Num>> = arrayListOf()
    ) {


        this.title = title
        /*
         * Building chart datasets
         */
        val dataset = TimeSeriesCollection()
        dataset.addSeries(buildChartTimeSeries(series, ClosePriceIndicator(series), "Bitstamp Bitcoin (BTC)"))

        indicatorsToDraw.forEach {
            dataset.addSeries(buildChartTimeSeries(series, it, it::class.java.simpleName))
        }
//        dataset.addSeries(buildChartTimeSeries(series, TrendUpBottomIndicator(series), "TrendUpBottomIndicator"))
//        dataset.addSeries(buildChartTimeSeries(series, TrendDownTopIndicator(series), "TrendDownTopIndicator"))
//        dataset.addSeries(buildChartTimeSeries(series, SuperTrendWrapperIndicator(series), "supertrend"))

        /*
         * Creating the chart
         */
        val chart: JFreeChart = ChartFactory.createTimeSeriesChart(
            title,  // title
            "Date",  // x-axis label
            "Price",  // y-axis label
            dataset,  // data
            true,  // create legend?
            true,  // generate tooltips?
            false // generate URLs?
        )
        val plot: XYPlot = chart.plot as XYPlot
        val axis: DateAxis = plot.domainAxis as DateAxis
        axis.dateFormatOverride = SimpleDateFormat("MM-dd HH:mm")

        /*
         * Running the strategy and adding the buy and sell signals to plot
         */addBuySellSignals(series, strategy, plot)

        /*
         * Displaying the chart
         */displayChart(chart)
    }
}