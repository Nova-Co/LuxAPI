package com.novaco.luxapi.commons.scheduler

/**
 * The core scheduling engine for cross-platform tasks.
 * Supports synchronous (main thread) and asynchronous execution.
 */
interface LuxScheduler {

    /**
     * Runs a task immediately on the main server thread.
     */
    fun run(runnable: Runnable): LuxTask

    /**
     * Runs a task immediately on an asynchronous thread pool.
     */
    fun runAsync(runnable: Runnable): LuxTask

    /**
     * Schedules a task to run on the main thread after a specific delay.
     * @param delay The delay in Minecraft ticks (20 ticks = 1 second).
     */
    fun runLater(delay: Long, runnable: Runnable): LuxTask

    /**
     * Schedules a task to run asynchronously after a specific delay.
     */
    fun runLaterAsync(delay: Long, runnable: Runnable): LuxTask

    /**
     * Schedules a repeating task on the main thread.
     * @param delay Initial delay before the first run.
     * @param period Time between subsequent executions.
     */
    fun runRepeating(delay: Long, period: Long, runnable: Runnable): LuxTask

    /**
     * Schedules a repeating task on an asynchronous thread pool.
     */
    fun runRepeatingAsync(delay: Long, period: Long, runnable: Runnable): LuxTask

    /**
     * Cancels all tasks currently managed by this scheduler.
     */
    fun cancelAll()
}