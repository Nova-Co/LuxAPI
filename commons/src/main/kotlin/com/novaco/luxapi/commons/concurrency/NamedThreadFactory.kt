package com.novaco.luxapi.commons.concurrency

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 * A [ThreadFactory] that names every thread it creates `"$prefix-N"` (N incrementing
 * from 1), instead of the JVM's generic "pool-1-thread-1" default — makes threads from
 * a specific subsystem identifiable in a thread dump or profiler.
 *
 * @param prefix The name prefix applied to every thread this factory creates.
 * @param daemon Whether created threads are daemon threads (won't block JVM shutdown).
 *   Defaults to true — background work like this shouldn't hold the server process open.
 */
class NamedThreadFactory(
    private val prefix: String,
    private val daemon: Boolean = true
) : ThreadFactory {

    private val counter = AtomicInteger(1)

    override fun newThread(r: Runnable): Thread {
        val thread = Thread(r, "$prefix-${counter.getAndIncrement()}")
        thread.isDaemon = daemon
        return thread
    }
}
