package com.pecpwee.tradebot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableConfigurationProperties
@ConfigurationPropertiesScan
class TradebotApplication

fun main(args: Array<String>) {
    //TELEGRAM
    SpringApplicationBuilder(TradebotApplication::class.java)
        .headless(false)//为了显示回测GUI
        .properties("spring.config.name=application,secret")//读取配置文件
        .run(*args)
    // runApplication<TradebotApplication>(*args)
}
