package com.novaco.luxapi.commons.init

import org.slf4j.LoggerFactory

/**
 * Runs a batch of discovered [InitializationTask]s, isolating failures the same way
 * [com.novaco.luxapi.commons.event.EventBus.fire] does for listeners — one task
 * throwing doesn't stop the rest from running.
 */
object InitializationRunner {

    private val logger = LoggerFactory.getLogger(InitializationRunner::class.java)

    /**
     * Runs every task in [tasks] in order, catching and logging any exception per-task.
     *
     * @return The number of tasks that ran without throwing.
     */
    fun runAll(tasks: Collection<InitializationTask>): Int {
        var succeeded = 0
        for (task in tasks) {
            try {
                task.run()
                succeeded++
            } catch (e: RuntimeException) {
                logger.error("InitializationTask '{}' threw during startup", task::class.simpleName, e)
            }
        }
        return succeeded
    }
}
