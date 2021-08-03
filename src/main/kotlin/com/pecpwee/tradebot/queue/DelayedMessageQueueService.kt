package com.pecpwee.tradebot.queue

import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.lang.Exception
import java.lang.InterruptedException
import java.util.concurrent.*
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Service
class DelayedMessageQueueService {
    private val mDelayQueue: BlockingQueue<DelayMessage> = DelayQueue()
    private val threadPoolExecutor = newFixedThreadPool(1)
    private var needExit = false

    @Autowired
    private lateinit var logger: Logger

    @PostConstruct
    fun init() {
        threadPoolExecutor.submit({ runLooper() })
    }

    @PreDestroy
    fun destroy() {
        mDelayQueue.put(DelayMessage(100, Runnable {}))
        threadPoolExecutor.shutdown()
    }

    fun putTask(delayMessage: DelayMessage) {
        try {
            mDelayQueue.put(delayMessage)
        } catch (e: InterruptedException) {
            logger.error(e)
        }
    }

    fun removeTask(delayMessage: DelayMessage){
        mDelayQueue.remove(delayMessage)
    }


    fun scheduledRunLoop(delayMessage: DelayMessage) {

    }


    fun runLooper() {
        while (true) {
            try {
                //从延迟队列中取值,如果没有对象过期则取到null
                val delayedMsg = mDelayQueue.take()
                if (delayedMsg == null) {
                    return
                }
                val runnable = delayedMsg.runnable
                runnable.run()
            } catch (e: Exception) {
                logger.error(e)
            }
        }
    }


    companion object {
        fun newFixedThreadPool(nThreads: Int): ExecutorService {
            return ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, LinkedBlockingQueue())
        }
    }

}