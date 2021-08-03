package com.pecpwee.tradebot.utils


import java.util.LinkedList

class LimitQueue<E>(// 队列长度
    val limit: Int
) {
    private val queue = LinkedList<E>()

    /**
     * 入列：当队列大小已满时，把队头的元素poll掉
     */
    @Synchronized
    fun offer(e: E) {
        if (queue.size >= limit) {
            queue.poll()
        }
        queue.offer(e)
    }

    @Synchronized
    fun getAll(): List<E> {
        return queue.clone() as List<E>
    }

    operator fun get(position: Int): E {
        return queue[position]
    }

    val last: E
        get() = queue.last
    val first: E
        get() = queue.first

    fun size(): Int {
        return queue.size
    }
}