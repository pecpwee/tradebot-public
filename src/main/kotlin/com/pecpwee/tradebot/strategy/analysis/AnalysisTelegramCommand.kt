package com.pecpwee.tradebot.strategy.analysis

import com.pecpwee.tradebot.telegram.command.replyStrContent
import com.pecpwee.tradebot.utils.fromDateStrToTimestamp
import com.pecpwee.tradebot.utils.toPrettyJson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.telegram.telegrambots.extensions.bots.commandbot.commands.helpCommand.ManCommand
import org.telegram.telegrambots.meta.api.objects.Chat
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.bots.AbsSender
import java.time.Duration

@Component
class AnalysisTelegramCommand : ManCommand("analysis_decline_rank", "argument1:size,argument2:startTime,", "") {
    @Autowired
    lateinit var declineAndRecoverRank: DeclineAndRecoverRank
    override fun execute(absSender: AbsSender?, user: User?, chat: Chat?, arguments: Array<out String>?) {

        if (arguments == null) {
            return
        }
        var startTime: Long = System.currentTimeMillis() - Duration.ofDays(1).toMillis()
        var ranksize = 10
        if (arguments.size > 0) {
            ranksize = arguments[0].toInt()
        }
        if (arguments.size > 1) {
            startTime = arguments[1].fromDateStrToTimestamp()
        }


        val result = declineAndRecoverRank.getRanks(startTime = startTime)

        this.replyStrContent("startAnalysis please wait", absSender, user, chat)

        val delineRankName = result.declineRank.subList(0, ranksize).map {
            it.pairName.toString() + " ${it.percentDeclined}"
        }
        val recoverRankName = result.recoverRank.subList(0, ranksize).map {
            it.pairName.toString() + " ${it.percentLatestRecovery}"
        }

        this.replyStrContent("delineRank\n" + delineRankName.toPrettyJson(), absSender, user, chat)

        this.replyStrContent("recoverRank\n" + recoverRankName.toPrettyJson(), absSender, user, chat)

    }


}