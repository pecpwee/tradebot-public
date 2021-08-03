package com.pecpwee.tradebot.bean

import com.pecpwee.tradebot.configuration.TradebotApplicationConfig
import info.bitrich.xchangestream.binance.BinanceStreamingExchange
import info.bitrich.xchangestream.core.StreamingExchangeFactory
import org.knowm.xchange.ExchangeFactory
import org.knowm.xchange.ExchangeSpecification
import org.knowm.xchange.binance.BinanceExchange
import org.knowm.xchange.binance.service.BinanceAccountService
import org.knowm.xchange.binance.service.BinanceMarketDataService
import org.knowm.xchange.binance.service.BinanceTradeService
import org.knowm.xchange.coinbasepro.CoinbaseProExchange
import org.knowm.xchange.coinbasepro.service.CoinbaseProMarketDataService
import org.knowm.xchange.currency.Currency
import org.knowm.xchange.currency.CurrencyPair
import org.knowm.xchange.deribit.v2.DeribitExchange
import org.knowm.xchange.deribit.v2.service.DeribitMarketDataService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.context.annotation.Primary
import java.lang.reflect.Modifier

@Configuration
class XChangeConfig {
    @Autowired
    lateinit var tradebotApplicationConfig: TradebotApplicationConfig

    val isTestnetAPI = false

    @Bean
    @Primary
    fun getBinanceExchage(): BinanceExchange {
        val exchange = ExchangeFactory.INSTANCE.createExchangeWithoutSpecification(
            BinanceExchange::class.java
        )
        val spec = exchange.defaultExchangeSpecification

        if (isTestnetAPI) {
            spec.secretKey = tradebotApplicationConfig.binanceSECRET_KEY_TESTNET
            spec.apiKey = tradebotApplicationConfig.binance_API_KEY_TESTNET
            spec.setExchangeSpecificParametersItem("Use_Sandbox", true)
        } else {
            spec.secretKey = tradebotApplicationConfig.binanceSECRET_KEY_ONLINE
            spec.apiKey = tradebotApplicationConfig.binanceAPI_KEY_ONLINE
        }

        exchange.applySpecification(spec)
        return exchange
    }

    @Bean
    @Lazy
    fun getBinanceStream(): BinanceStreamingExchange {
        val spec = ExchangeSpecification(BinanceStreamingExchange::class.java)
        spec.isShouldLoadRemoteMetaData = true
        spec.secretKey = tradebotApplicationConfig.binanceSECRET_KEY_ONLINE
        spec.apiKey = tradebotApplicationConfig.binanceAPI_KEY_ONLINE

        val a = StreamingExchangeFactory.INSTANCE.createExchange(spec) as BinanceStreamingExchange
        return a
    }


    @Bean
    @Qualifier
    fun getBinanceTrade(binanceExchange: BinanceExchange): BinanceTradeService {
        val tradeService = binanceExchange.tradeService as BinanceTradeService
        return tradeService
    }

    @Bean
    fun getBinanceAccount(binanceExchange: BinanceExchange): BinanceAccountService {
        return binanceExchange.accountService as BinanceAccountService
    }


    @Bean
    fun buildBinanceMarket(binanceExchange: BinanceExchange): BinanceMarketDataService {
        return binanceExchange.marketDataService as BinanceMarketDataService
    }


    @Bean
    @Lazy
    fun buildDeribit(): DeribitExchange {
        val binanceExchange = ExchangeFactory.INSTANCE.createExchange(DeribitExchange::class.java)
        return binanceExchange
    }


    @Bean
    @Lazy
    fun buildDeribitMarket(binanceExchange: DeribitExchange): DeribitMarketDataService {
        return binanceExchange.marketDataService as DeribitMarketDataService
    }


    @Bean
    @Lazy
    fun buildCoinbasePro(): CoinbaseProExchange {
        return ExchangeFactory.INSTANCE.createExchange(CoinbaseProExchange::class.java)
    }

    @Bean
    @Lazy
    fun buildCoinbaseProMarket(coinbasePro: CoinbaseProExchange): CoinbaseProMarketDataService {
        return coinbasePro.marketDataService as CoinbaseProMarketDataService
    }

    @Bean
    fun getAllPairs(): AllCurrencyPairs {
        val fields = CurrencyPair::class.java.declaredFields
        val USDTPairList = fields.filterNotNull()
            .map {
                it.isAccessible = true
                if (Modifier.isStatic(it.modifiers)) {
                    it.get(null)
                } else {
                    null
                }
            }.filterNotNull()
            .filter {
                it is CurrencyPair
            }.map {
                it as CurrencyPair
            }.filter {
                it.counter.equals(Currency.USDT)
            }.toMutableList()


        val extraPairs = arrayListOf(
            CurrencyPair("SHIB/USDT"),
            CurrencyPair("DOGE/USDT"),
            CurrencyPair("SUSHI/USDT"),
            CurrencyPair("BNB/USDT"),
            CurrencyPair("AAVE/USDT"),
            CurrencyPair("FTT/USDT"),
            CurrencyPair("COMP/USDT"),
            CurrencyPair("FIL/USDT"),
            CurrencyPair("UNI/USDT"),
            CurrencyPair("YFI/USDT"),
            CurrencyPair("YFII/USDT"),
            CurrencyPair("CRV/USDT"),
            CurrencyPair("ALGO/USDT"),
            CurrencyPair("LINK/USDT"),
            CurrencyPair("BAT/USDT"),
            CurrencyPair("CAKE/USDT"),
            CurrencyPair("MIR/USDT"),
            CurrencyPair("SNX/USDT"),
            CurrencyPair("MATIC/USDT"),
            CurrencyPair("BAKE/USDT"),
            CurrencyPair("SOL/USDT"),
            CurrencyPair("LUNA/USDT"),
        )
        USDTPairList.addAll(extraPairs)


        return AllCurrencyPairs(USDTPairList)
    }

    class AllCurrencyPairs(val content: List<CurrencyPair>)

}