package com.pecpwee.tradebot.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@ConfigurationProperties(prefix = "tradebot")
@Component
class TradebotApplicationConfig {
    var runTradeAutoWhenAppStart: Boolean = false
    var analysisModel: Boolean = false


    //binnace key configuration. online and testnet
    var binanceSECRET_KEY_ONLINE: String = ""
    var binanceAPI_KEY_ONLINE: String = ""
    var binanceSECRET_KEY_TESTNET: String = ""
    var binance_API_KEY_TESTNET: String = ""


    //msg service configuration
    var wxCallbackURL: String = ""
    var DingdignCallbackURL: String = ""

    var telegraBOT_TOKEN: String = ""
    var telegraBOT_USDERNAME: String = ""
    var telegraMsgGroupId: String = ""

}