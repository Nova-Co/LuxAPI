package com.novaco.luxapi.commons.scheduler

/**
 * Represents a task scheduled through the LuxAPI scheduler.
 * Provides control over the task lifecycle, allowing cancellation and status checks.
 */
interface LuxTask {

    /**
     * Gets the unique identifier of the task.
     */
    val id: Int

    /**
     * Cancels the task, preventing it from running again.
     */
    fun cancel()

    /**
     * Checks whether the task is currently active or scheduled to run.
     */
    val isCancelled: Boolean

    /**
     * Checks whether the task is running asynchronously.
     */
    val isAsync: Boolean
}