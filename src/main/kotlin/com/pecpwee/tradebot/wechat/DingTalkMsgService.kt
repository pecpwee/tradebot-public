package com.pecpwee.tradebot.wechat

import com.dingtalk.api.DefaultDingTalkClient
import com.dingtalk.api.DingTalkClient
import com.dingtalk.api.request.OapiRobotSendRequest
import com.dingtalk.api.request.OapiRobotSendRequest.At
import com.pecpwee.tradebot.configuration.TradebotApplicationConfig
import com.pecpwee.tradebot.utils.toDateStr
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*


/***
 *  https://developers.dingtalk.com/document/app/custom-robot-access?spm=ding_open_doc.document.0.0.7f875e59tnNf3i#topic-2026027
 *
 *
 */
@Service
class DingTalkMsgService {


    @Autowired
    lateinit var tradebotApplicationConfig: TradebotApplicationConfig

    @Autowired
    lateinit var logger: Logger

    // TODO: 2021/6/4
    fun init() {
        sendMsg("")
    }

    fun sendMsg(msg: String) {
        if (tradebotApplicationConfig.DingdignCallbackURL.isNullOrEmpty()) {
            logger.debug("no dingding callback configured,not sent msg")
            return
        }
        val client: DingTalkClient =
            DefaultDingTalkClient(tradebotApplicationConfig.DingdignCallbackURL)
        val request = OapiRobotSendRequest()
        request.msgtype = "text"
        val text = OapiRobotSendRequest.Text()
        text.content = "${msg} ${System.currentTimeMillis().toDateStr()} detbits"
        request.setText(text)
        val at = At()
        at.atMobiles = Arrays.asList("18410299730")
// isAtAll类型如果不为Boolean，请升级至最新SDK
// isAtAll类型如果不为Boolean，请升级至最新SDK
        at.isAtAll = true
        at.atUserIds = Arrays.asList("109929", "32099")
        request.setAt(at)

//        request.msgtype = "link"
//
//        request.msgtype = "markdown"
//        val markdown = OapiRobotSendRequest.Markdown()
//        markdown.title = "杭州天气"
//        markdown.text = """
//            #### 杭州天气 @156xxxx8827
//            > 9度，西北风1级，空气良89，相对温度73%
//
//            > ![screenshot](https://gw.alicdn.com/tfs/TB1ut3xxbsrBKNjSZFpXXcXhFXa-846-786.png)
//            > ###### 10点20分发布 [天气](http://www.thinkpage.cn/)
//
//            """.trimIndent()
//        request.setMarkdown(markdown)
        val response = client.execute(request)

//        println(response)

    }
}