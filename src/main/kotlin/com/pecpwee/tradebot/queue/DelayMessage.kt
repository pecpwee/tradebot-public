package com.pecpwee.tradebot.queue

import java.lang.Runnable
import java.util.concurrent.*

class DelayMessage(val delay: Long, val runnable: Runnable) : Delayed {

    private val expire: Long //到期时间

    init {
        expire = System.currentTimeMillis() + delay
    }

    /**
     * 剩余时间=到期时间-当前时间
     * 剩余时间是0的时候才会被消费者取出来
     */
    override fun getDelay(unit: TimeUnit): Long {
        return unit.convert(expire - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
    }

    /**
     * 优先队列里面优先级规则
     */
    override fun compareTo(o: Delayed): Int {
        return (getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS)).toInt()
    }

}