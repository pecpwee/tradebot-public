package com.pecpwee.tradebot.telegram.command

import org.springframework.stereotype.Component
import org.telegram.telegrambots.extensions.bots.commandbot.commands.helpCommand.ManCommand
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender


@Component
class KillSelfTelegramCommand : ManCommand("killself", "/killself force", "") {


    override fun execute(absSender: AbsSender?, user: User?, chat: Chat?, arguments: Array<out String>?) {
        if (arguments == null || arguments.size < 1 || !arguments[0].equals("force")) {
            this.replyStrContent("no force argument.please input as followed:${description}", absSender, user, chat)
            return
        }

        this.replyStrContent("system will exit by telegram command", absSender, user, chat)

        System.exit(0)

    }
}