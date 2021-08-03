package com.pecpwee.tradebot.strategy.controller

import com.pecpwee.tradebot.strategy.AbsStrategy
import com.pecpwee.tradebot.telegram.command.replyStrContent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.telegram.telegrambots.extensions.bots.commandbot.commands.helpCommand.ManCommand
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender


/**
 * 这个类可能造成循环依赖注入倒置没法正常启动程序
 * 原因是strategy里依赖telegramService打印日志，同时初始化telegramService时又需要引用StrategyManagerCenterTelegramMan，这里又得初始化所有策略
 *
 */
@Component
class StrategyManagerCenterTelegramMan : ManCommand(
    "strategy_ops", "strategy {name} {start/stop} or strategy all {start/stop}", ""
) {

    @Autowired
    lateinit var strategyManagerCenterService: StrategyManagerCenterService


    override fun execute(absSender: AbsSender?, user: User?, chat: Chat?, arguments: Array<out String>?) {
        if (arguments == null || arguments.size < 2) {
            this.replyStrContent("error ${description}", absSender, user, chat)
            return
        }
        val strategyName = arguments[0]
        val opsName = arguments[1]
        var listToOps: List<AbsStrategy>
        if (strategyName.equals("all", ignoreCase = true)) {
            listToOps = strategyManagerCenterService.queryStrategies(null)
        } else {
            listToOps = strategyManagerCenterService.queryStrategies(strategyName)
        }
        if (opsName.equals("start", ignoreCase = true)) {
            strategyManagerCenterService.start(listToOps)
            this.replyStrContent("OK,we just start ${listToOps}", absSender, user, chat)
        } else {
            strategyManagerCenterService.stop(listToOps)
            this.replyStrContent("OK,we just stop ${listToOps}", absSender, user, chat)
        }
    }
}