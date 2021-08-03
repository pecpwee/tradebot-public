package com.pecpwee.tradebot.telegram

import com.pecpwee.tradebot.queue.DelayMessage
import com.pecpwee.tradebot.queue.DelayedMessageQueueService
import com.pecpwee.tradebot.wechat.DingTalkMsgService
import com.pecpwee.tradebot.wechat.WxMsgService
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Service
class TelegramInfoServiceImpl : TelegramInfoService {

    @Autowired
    lateinit var wxService: WxMsgService

    @Autowired
    private lateinit var telegramBot: TelegramCenterBot

    @Autowired
    private lateinit var dingTalkMsgService: DingTalkMsgService

    @Autowired
    private lateinit var logger: Logger


    @Autowired
    private lateinit var delayedMessageQueueService: DelayedMessageQueueService

    @PostConstruct
    fun init() {
        notifyInfo("Hello, i'm tradebot,I am fine")
    }

    @PreDestroy
    fun beforeDestroy() {
        notifyInfo("Bye, i'm tradebot,I am dying QAQ")
    }

    override fun notifyInfo(msg: String) {
        delayedMessageQueueService.putTask(DelayMessage(0) {
            runSafely {
                dingTalkMsgService.sendMsg(msg)
            }
            runSafely {
                wxService.sendMsg(msg)
            }
            runSafely {
                telegramBot.notifyInfo(msg)
            }
            logger.info("notified msg: ${msg}")
        })

    }

    private fun runSafely(block: () -> Unit) {
        try {
            block()
        } catch (t: Throwable) {
            logger.debug(t)
        }


    }
}

