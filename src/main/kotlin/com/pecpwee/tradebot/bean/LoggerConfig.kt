package com.pecpwee.tradebot.bean

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

@Configuration
class LoggerConfig {
    @Bean
    fun logger4JConfig(): Logger {
        val logger = LogManager.getLogger()
        return logger
    }

}