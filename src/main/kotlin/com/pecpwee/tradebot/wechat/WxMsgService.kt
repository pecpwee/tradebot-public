package com.pecpwee.tradebot.wechat

import com.pecpwee.tradebot.configuration.TradebotApplicationConfig
import com.pecpwee.tradebot.utils.toDateStr
import com.yanceyzhang.commons.wx.chatbot.SendResult
import com.yanceyzhang.commons.wx.chatbot.WxChatbotClient
import com.yanceyzhang.commons.wx.chatbot.message.TextMessage
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service


@Service
class WxMsgService {

    @Autowired
    private lateinit var logger: Logger

    @Autowired
    lateinit var tradebotApplicationConfig: TradebotApplicationConfig


    val client = WxChatbotClient()


    fun sendMsg(msg: String, needNotifyMyself: Boolean = false) {
        if (tradebotApplicationConfig.wxCallbackURL.isNullOrEmpty()) {
            logger.debug("no weixin callback configured,not sent msg")
            return
        }
        val message = TextMessage("[${System.currentTimeMillis().toDateStr()}]$msg")
        val mentionedMobileList: MutableList<String> = ArrayList()
        message.mentionedMobileList = mentionedMobileList
        message.setIsAtAll(true) //@所有人
        val result: SendResult = WxChatbotClient.send(tradebotApplicationConfig.wxCallbackURL, message)
        System.out.println(result)
    }


}