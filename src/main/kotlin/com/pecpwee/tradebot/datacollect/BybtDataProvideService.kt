package com.pecpwee.tradebot.datacollect

import com.pecpwee.tradebot.configuration.TradebotApplicationConfig
import okhttp3.OkHttpClient
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.annotation.PostConstruct


@Service
class BybtDataProvideService {

    @Autowired
    private lateinit var logger: Logger

    private lateinit var bybtGreyScaleRemoteRepo: BybtGreyScaleHoldInterfaces
    private lateinit var bybtFundingRate: FundingRateInterface

    @Autowired
    lateinit var mainApplicationConfig: TradebotApplicationConfig

    @PostConstruct
    fun init() {
        val client = OkHttpClient()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.bybt.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
        this.bybtGreyScaleRemoteRepo = retrofit.create(BybtGreyScaleHoldInterfaces::class.java)
        println(mainApplicationConfig)


        val fapiBybt = Retrofit.Builder()
            .baseUrl("https://fapi.bybt.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
        this.bybtFundingRate = fapiBybt.create(FundingRateInterface::class.java)

    }

    fun getFundingRateInfo(): BdpServicesInterfaceResponse? {
        val caller = bybtFundingRate.requestFundingRateList()
        try {
            val response = caller.execute()
            if (response.isSuccessful) {
                val data = response.body()
                return data
            }
        } catch (e: Throwable) {
            logger.error(e)
        }
        return null
    }

    fun getGreyScaleHoldInfo(greyRequestSymbol: GreyRequestSymbol): GreyScaleDataResult {


        val caller = bybtGreyScaleRemoteRepo.reuqestGreyScaleVolumn(greyRequestSymbol)
        try {
            val response = caller.execute()
            if (response.isSuccessful) {
                val respData = response.body()
                if (respData != null) {
                    return respData
                }
            }
        } catch (e: Throwable) {
            logger.error(e)
        }
        return GreyScaleDataResult()
    }

    //    @Scheduled(initialDelay = 1000, fixedDelay = 5000000)
    fun testit() {
        val result = getGreyScaleHoldInfo(GreyRequestSymbol.BTC)
        logger.info(result)
    }

    enum class GreyRequestSymbol(val strValue: String) {
        BTC("BTC"), ETH("ETH")
    }
}