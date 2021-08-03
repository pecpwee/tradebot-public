package com.pecpwee.tradebot.controller

import com.pecpwee.tradebot.datacollect.GreyScaleDataResult
import com.pecpwee.tradebot.datacollect.BybtDataProvideService
import com.pecpwee.tradebot.pojo.CommonResponse
import com.pecpwee.tradebot.vpvr.VPVRCalculatorService
import com.pecpwee.tradebot.strategy.controller.StrategyMonitorService
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RequestMapping("/tradebot/v1/")
@RestController
class MainController {
    @Autowired
    private lateinit var logger: Logger


    @Autowired
    private lateinit var vpvrService: VPVRCalculatorService

    @Autowired
    private lateinit var greyScaleService: BybtDataProvideService

    @Autowired
    private lateinit var strategyMonitorService: StrategyMonitorService


    data class SystemAllStatus(val strategiesStatus: List<StrategyMonitorService.StrategyStatus>)

//    //http://127.0.0.1:8080/tradebot/v1/vpvr/ETHUSDT?startTime=1604163661000&endTime=1604646061000
//    @GetMapping("vpvr/{tradepair}")
//    @ResponseBody
//    @CrossOrigin
//    fun calculateVpvr(
//            @PathVariable("tradepair") tradePair: String,
//            @RequestParam(name = "startTime") startTime: Long,
//            @RequestParam(name = "endTime") endTime: Long
//    ): VPVRCalculatorService.PriceVolResult {
//
//        return vpvrService.calculateVpvr(tradePair, KlineInterval.m1, startTime, endTime)
////        return CommonResponse(statusCode = 200, message = "hello world");
//    }


    //127.0.0.1:8080/tradebot/v1/greyscale/BTC
    @GetMapping("greyscale/{coinSymbol}")
    @ResponseBody
    @CrossOrigin
    fun calculateVpvr(
            @PathVariable("coinSymbol") coinSymbol: String
    ): GreyScaleDataResult {
        val greyCoinSymbol = BybtDataProvideService.GreyRequestSymbol.valueOf(coinSymbol)
        val greyResult = greyScaleService.getGreyScaleHoldInfo(greyCoinSymbol)

        return greyResult
    }

    fun test(): CommonResponse {
        val result = ""
        return CommonResponse(statusCode = 200, message = result)
    }


}