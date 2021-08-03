package com.pecpwee.tradebot.telegram

import com.pecpwee.tradebot.configuration.TradebotApplicationConfig
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.telegram.telegrambots.extensions.bots.commandbot.TelegramLongPollingCommandBot
import org.telegram.telegrambots.extensions.bots.commandbot.commands.IBotCommand
import org.telegram.telegrambots.extensions.bots.commandbot.commands.helpCommand.HelpCommand
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import javax.annotation.PostConstruct


@Component
class TelegramCenterBot : TelegramLongPollingCommandBot() {
    @Autowired
    private lateinit var logger: Logger

    @Autowired
    lateinit var tradebotApplicationConfig: TradebotApplicationConfig


    @Autowired
    private lateinit var commands: List<IBotCommand>


    @PostConstruct
    fun init() {
        logger.info("PWBot create")

        if (botToken.isNullOrEmpty() || botUsername.isNullOrEmpty()) {
            logger.debug("telegram not configured,not send msg")
            return
        }

        commands.forEach {
            this.register(
                it
            )
        }
        this.register(HelpCommand())

    }

    override fun getBotToken(): String {
        return tradebotApplicationConfig.telegraBOT_TOKEN
    }

    override fun getBotUsername(): String {
        return tradebotApplicationConfig.telegraBOT_USDERNAME
    }

    fun getTelegramMsgSendToGroupId(): String {
        return tradebotApplicationConfig.telegraMsgGroupId
    }

    override fun processNonCommandUpdate(update: Update?) {
        logger.debug(update)
    }


    fun notifyInfo(msg: String) {
        if (botToken.isNullOrEmpty() || botUsername.isNullOrEmpty()) {
            logger.debug("telegram not configured,not send msg")
            return
        }

        val telegramGroupId = getTelegramMsgSendToGroupId()
        if (telegramGroupId.isNullOrEmpty()) {
            logger.debug("telegram not configure the group id to send,not send msg")
            return
        }
        val snd = SendMessage()
        snd.chatId = telegramGroupId
        snd.text = msg
        try {
            execute(snd)
        } catch (e: TelegramApiException) {
            logger.error("Could not send message")
            logger.error(e)
        }
    }


}