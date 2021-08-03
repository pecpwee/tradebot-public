package com.pecpwee.tradebot.telegram.command

import org.telegram.telegrambots.extensions.bots.commandbot.commands.helpCommand.ManCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender
import org.telegram.telegrambots.meta.exceptions.TelegramApiException


fun ManCommand.replyStrContent(
    reply: String,
    absSender: AbsSender?,
    user: User?,
    chat: Chat?
) {


    try {
        absSender!!.execute(
            SendMessage.builder().chatId(chat!!.id.toString()).text(reply)
                .parseMode("HTML").build()
        )
    } catch (e: TelegramApiException) {
        e.printStackTrace()
    }
}

