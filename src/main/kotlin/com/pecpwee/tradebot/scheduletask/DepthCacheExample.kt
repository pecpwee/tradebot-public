package com.pecpwee.tradebot.scheduletask

import com.binance.api.client.BinanceApiCallback
import com.binance.api.client.BinanceApiRestClient
import com.binance.api.client.BinanceApiWebSocketClient
import com.binance.api.client.domain.event.DepthEvent
import com.binance.api.client.domain.market.OrderBookEntry
import java.io.Closeable
import java.io.IOException
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer

/**
 * Illustrates how to use the depth event stream to create a local cache of bids/asks for a symbol.
 * BID（bid price或者offer price）在外汇交易中表示买入价。 ASK（ask price）外汇交易中表示卖出价
 *
 *
 * Snapshots of the order book can be retrieved from the REST API.
 * Delta changes to the book can be received by subscribing for updates via the web socket API.
 *
 *
 * To ensure no updates are missed, it is important to subscribe for updates on the web socket API
 * _before_ getting the snapshot from the REST API. Done the other way around it is possible to
 * miss one or more updates on the web socket, leaving the local cache in an inconsistent state.
 *
 *
 * Steps:
 * 1. Subscribe to depth events and cache any events that are received.
 * 2. Get a snapshot from the rest endpoint and use it to build your initial depth cache.
 * 3. Apply any cache events that have a final updateId later than the snapshot's update id.
 * 4. Start applying any newly received depth events to the depth cache.
 *
 *
 * The example repeats these steps, on a new web socket, should the web socket connection be lost.
 */
class DepthCacheExample(val restClient: BinanceApiRestClient, val wsClient: BinanceApiWebSocketClient, private val symbol: String) {
    private val wsCallback = WsCallback()
    private val depthCache: MutableMap<String, NavigableMap<BigDecimal, BigDecimal>> = HashMap()
    private var lastUpdateId: Long = -1
    companion object {
        private const val BIDS = "BIDS"
        private const val ASKS = "ASKS"

        /**
         * Pretty prints an order book entry in the format "price / quantity".
         */
        private fun toDepthCacheEntryString(depthCacheEntry: Map.Entry<BigDecimal, BigDecimal>): String {
            return depthCacheEntry.key.toPlainString() + " / " + depthCacheEntry.value
        }
    }

    @Volatile
    private var webSocket: Closeable? = null

    init {
        initialize()
    }

    private fun initialize() {
        // 1. Subscribe to depth events and cache any events that are received.
        val pendingDeltas = startDepthEventStreaming()

        // 2. Get a snapshot from the rest endpoint and use it to build your initial depth cache.
        initializeDepthCache()

        // 3. & 4. handled in here.
        applyPendingDeltas(pendingDeltas)
    }

    /**
     * Begins streaming of depth events.
     *
     *
     * Any events received are cached until the rest API is polled for an initial snapshot.
     */
    private fun startDepthEventStreaming(): MutableList<DepthEvent> {
        val pendingDeltas: MutableList<DepthEvent> = CopyOnWriteArrayList()
        wsCallback.setHandler(Consumer { e: DepthEvent -> pendingDeltas.add(e) })
        webSocket = wsClient.onDepthEvent(symbol.lowercase(Locale.getDefault()), wsCallback)
        return pendingDeltas
    }

    /**
     * 2. Initializes the depth cache by getting a snapshot from the REST API.
     */
    private fun initializeDepthCache() {
        val orderBook = restClient.getOrderBook(symbol.uppercase(Locale.getDefault()), 10)
        lastUpdateId = orderBook.lastUpdateId
        val asks: NavigableMap<BigDecimal, BigDecimal> = TreeMap(Comparator.reverseOrder())
        for (ask in orderBook.asks) {
            asks[BigDecimal(ask.price)] = BigDecimal(ask.qty)
        }
        depthCache[ASKS] = asks
        val bids: NavigableMap<BigDecimal, BigDecimal> = TreeMap(Comparator.reverseOrder())
        for (bid in orderBook.bids) {
            bids[BigDecimal(bid.price)] = BigDecimal(bid.qty)
        }
        depthCache[BIDS] = bids
    }

    /**
     * Deal with any cached updates and switch to normal running.
     */
    private fun applyPendingDeltas(pendingDeltas: MutableList<DepthEvent>) {
        val updateOrderBook = Consumer { newEvent: DepthEvent ->
            if (newEvent.finalUpdateId > lastUpdateId) {
                println(newEvent)
                lastUpdateId = newEvent.finalUpdateId
                updateOrderBook(asks, newEvent.asks)
                updateOrderBook(bids, newEvent.bids)
                printDepthCache()
            }
        }
        val drainPending = Consumer { newEvent: DepthEvent ->
            pendingDeltas.add(newEvent)

            // 3. Apply any deltas received on the web socket that have an update-id indicating they come
            // after the snapshot.
            pendingDeltas.stream()
                    .filter { e: DepthEvent -> e.finalUpdateId > lastUpdateId } // Ignore any updates before the snapshot
                    .forEach(updateOrderBook)

            // 4. Start applying any newly received depth events to the depth cache.
            wsCallback.setHandler(updateOrderBook)
        }
        wsCallback.setHandler(drainPending)
    }

    /**
     * Updates an order book (bids or asks) with a delta received from the server.
     *
     *
     * Whenever the qty specified is ZERO, it means the price should was removed from the order book.
     */
    private fun updateOrderBook(lastOrderBookEntries: NavigableMap<BigDecimal, BigDecimal>?,
                                orderBookDeltas: List<OrderBookEntry>) {
        for (orderBookDelta in orderBookDeltas) {
            val price = BigDecimal(orderBookDelta.price)
            val qty = BigDecimal(orderBookDelta.qty)
            if (qty.compareTo(BigDecimal.ZERO) == 0) {
                // qty=0 means remove this level
                lastOrderBookEntries!!.remove(price)
            } else {
                lastOrderBookEntries!![price] = qty
            }
        }
    }

    val asks: NavigableMap<BigDecimal, BigDecimal>?
        get() = depthCache[ASKS]
    val bids: NavigableMap<BigDecimal, BigDecimal>?
        get() = depthCache[BIDS]

    /**
     * @return the best ask in the order book
     */
    private val bestAsk: Map.Entry<BigDecimal, BigDecimal>
        private get() = asks!!.lastEntry()

    /**
     * @return the best bid in the order book
     */
    private val bestBid: Map.Entry<BigDecimal, BigDecimal>
        private get() = bids!!.firstEntry()

    /**
     * @return a depth cache, containing two keys (ASKs and BIDs), and for each, an ordered list of book entries.
     */
    fun getDepthCache(): Map<String, NavigableMap<BigDecimal, BigDecimal>> {
        return depthCache
    }

    @Throws(IOException::class)
    fun close() {
        webSocket!!.close()
    }

    /**
     * Prints the cached order book / depth of a symbol as well as the best ask and bid price in the book.
     */
    private fun printDepthCache() {
        println(depthCache)
        println("ASKS:(" + asks!!.size + ")")
        asks!!.entries.forEach(Consumer<Map.Entry<BigDecimal, BigDecimal>> { entry: Map.Entry<BigDecimal, BigDecimal> -> println(toDepthCacheEntryString(entry)) })
        println("BIDS:(" + bids!!.size + ")")
        bids!!.entries.forEach(Consumer<Map.Entry<BigDecimal, BigDecimal>> { entry: Map.Entry<BigDecimal, BigDecimal> -> println(toDepthCacheEntryString(entry)) })
        println("BEST ASK: " + toDepthCacheEntryString(bestAsk))
        println("BEST BID: " + toDepthCacheEntryString(bestBid))
    }

    private inner class WsCallback : BinanceApiCallback<DepthEvent> {
        private val handler = AtomicReference<Consumer<DepthEvent>>()
        override fun onResponse(depthEvent: DepthEvent) {
            try {
                handler.get().accept(depthEvent)
            } catch (e: Exception) {
                System.err.println("Exception caught processing depth event")
                e.printStackTrace(System.err)
            }
        }

        override fun onFailure(cause: Throwable) {
            println("WS connection failed. Reconnecting. cause:" + cause.message)
            initialize()
        }

        fun setHandler(handler: Consumer<DepthEvent>) {
            this.handler.set(handler)
        }
    }




}