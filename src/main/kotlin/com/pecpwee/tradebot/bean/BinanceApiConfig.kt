package com.pecpwee.tradebot.bean

import com.binance.api.client.BinanceApiClientFactory
import com.binance.api.client.BinanceApiRestClient
import com.binance.api.client.BinanceApiWebSocketClient
import com.binance.client.RequestOptions
import com.binance.client.SubscriptionClient
import com.binance.client.SyncRequestClient
import com.pecpwee.tradebot.configuration.TradebotApplicationConfig
import okhttp3.OkHttpClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.time.Duration

@Configuration
class BinanceApiConfig {

    @Autowired
    lateinit var tradebotApplicationConfig: TradebotApplicationConfig

    @Autowired
    lateinit var binanceApiClientFactory: BinanceApiClientFactory


    @Bean
    fun configBinanceApi(): BinanceApiRestClient {
        return binanceApiClientFactory.newRestClient()
    }

    @Bean
    fun configWebsocketBinanceApi(): BinanceApiWebSocketClient {
        return binanceApiClientFactory.newWebSocketClient()
    }

    @Bean
    fun configBinanceApiFactory(): BinanceApiClientFactory {
        println("tradebotApplicationConfig.binanceAPI_KEY_ONLINE")

        if (tradebotApplicationConfig.binanceAPI_KEY_ONLINE.isNullOrEmpty() || tradebotApplicationConfig.binanceSECRET_KEY_ONLINE.isNullOrEmpty()) {
            println("binance key has not been configured,may affect core function")
            return BinanceApiClientFactory.newInstance()
        }
        return BinanceApiClientFactory.newInstance(
            tradebotApplicationConfig.binanceAPI_KEY_ONLINE, tradebotApplicationConfig.binanceSECRET_KEY_ONLINE
        )
    }

    @Bean
    fun configOkhttp(): OkHttpClient {
        val okHttpClient = OkHttpClient.Builder()
            .pingInterval(Duration.ofMinutes(11))
            .build()
        return okHttpClient
    }

    @ConditionalOnProperty(prefix = "binance", name = ["is_testnet"], havingValue = "false")
    @Bean
    fun configBinanceFutureSyncApi(): SyncRequestClient {

        if (tradebotApplicationConfig.binanceAPI_KEY_ONLINE.isNullOrEmpty() || tradebotApplicationConfig.binanceSECRET_KEY_ONLINE.isNullOrEmpty()) {
            println("binance key has not been configured,may affect core function")
            return SyncRequestClient.create()
        }
        val options = RequestOptions()
        return SyncRequestClient.create(
            tradebotApplicationConfig.binanceAPI_KEY_ONLINE, tradebotApplicationConfig.binanceSECRET_KEY_ONLINE,
            options
        )
    }

    @Primary
    @ConditionalOnProperty(prefix = "binance", name = ["is_testnet"], havingValue = "true")
    @Bean
    fun configBinanceFutureTestNetSyncApi(): SyncRequestClient {
        if (tradebotApplicationConfig.binanceAPI_KEY_ONLINE.isNullOrEmpty() || tradebotApplicationConfig.binanceSECRET_KEY_ONLINE.isNullOrEmpty()) {
            println("binance key has not been configured,may affect core function")
            return SyncRequestClient.create()
        }
        val options = RequestOptions()
        options.url = "https://testnet.binancefuture.com"
        return SyncRequestClient.create(
            tradebotApplicationConfig.binance_API_KEY_TESTNET, tradebotApplicationConfig.binanceSECRET_KEY_TESTNET,
            options
        )
    }


    @ConditionalOnProperty(prefix = "binance", name = ["is_testnet"], havingValue = "false")
    @Bean
    fun configBinanceFutureWebsocketApi(): SubscriptionClient {
        if (tradebotApplicationConfig.binanceAPI_KEY_ONLINE.isNullOrEmpty() || tradebotApplicationConfig.binanceSECRET_KEY_ONLINE.isNullOrEmpty()) {
            println("binance key has not been configured,may affect core function")
            return SubscriptionClient.create()
        }
        val options = RequestOptions()
        return SubscriptionClient.create(
            tradebotApplicationConfig.binanceAPI_KEY_ONLINE, tradebotApplicationConfig.binanceSECRET_KEY_ONLINE
        )
    }

    @Primary
    @ConditionalOnProperty(prefix = "binance", name = ["is_testnet"], havingValue = "true")
    @Bean
    fun configBinanceFutureTestNetWebsocketApi(): SubscriptionClient {
        if (tradebotApplicationConfig.binanceAPI_KEY_ONLINE.isNullOrEmpty() || tradebotApplicationConfig.binanceSECRET_KEY_ONLINE.isNullOrEmpty()) {
            println("binance key has not been configured,may affect core function")
            return SubscriptionClient.create()
        }
        val options = RequestOptions()
        options.url = "https://testnet.binancefuture.com"
        return SubscriptionClient.create(
            tradebotApplicationConfig.binance_API_KEY_TESTNET, tradebotApplicationConfig.binanceSECRET_KEY_TESTNET
        )
    }
}