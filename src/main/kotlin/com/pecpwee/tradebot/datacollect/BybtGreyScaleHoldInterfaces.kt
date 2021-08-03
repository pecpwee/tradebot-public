package com.pecpwee.tradebot.datacollect

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface BybtGreyScaleHoldInterfaces {
    @GET("api/api/grayscaleOpenInterest/history")
    @Headers(
        value = [
            "Accept: application/json",
            "referer: https://www.bybt.com/zh/Grayscale"
        ]
    )
    fun reuqestGreyScaleVolumn(@Query("symbol") symbol: BybtDataProvideService.GreyRequestSymbol): Call<GreyScaleDataResult>
}

interface FundingRateInterface {
    @GET("api/fundingRate/v2/home")
    @Headers(
        value = [
            "Accept: application/json",
            "referer: https://www.bybt.com/zh/Grayscale"
        ]
    )
    fun requestFundingRateList(): Call<BdpServicesInterfaceResponse>
}

data class BdpServicesInterfaceResponse (
    val code: String,
    val msg: String,
    val data: List<Datum>,
    val success: Boolean
)
data class Datum (
    val uMarginList: List<MarginList>,
    val symbol: String,
    val cMarginList: List<MarginList>,
    val uIndexPrice: Double? = null,
    val uPrice: Double? = null,
    val cPrice: Double? = null,
    val symbolLogo: String,
    val cIndexPrice: Double? = null,
    val status: Long
)

data class MarginList (
    val rate: Double? = null,
    val nextFundingTime: Long? = null,
    val exchangeName: String,
    val exchangeLogo: String,
    val status: Long,
    val predictedRate: Double? = null,
    val sort: Long? = null,
    val allSort: Long? = null
)


data class GreyScaleDataResult(
    val code: String = "",
    val msg: String = "",
    val data: ValidData = ValidData(),
    val success: Boolean = false
)

data class ValidData(
    val opList: List<Long> = arrayListOf(),
    val dateList: List<Long> = arrayListOf(),
    val priceList: List<Double> = arrayListOf()
)