package com.pecpwee.tradebot.telegram.command

import com.pecpwee.tradebot.strategy.controller.StrategyMonitorService
import com.pecpwee.tradebot.utils.toPrettyJson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.telegram.telegrambots.extensions.bots.commandbot.commands.helpCommand.ManCommand
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender

@Component
class StrategyRunStatusCommand : ManCommand("strategy_status", "view all strategy run status", "") {


    @Autowired
    lateinit var strategyMonitorService: StrategyMonitorService


    override fun execute(absSender: AbsSender?, user: User?, chat: Chat?, arguments: Array<out String>?) {

        val status = strategyMonitorService.getRunningStatus().toPrettyJson()
        this.replyStrContent(status, absSender, user, chat)
    }

}