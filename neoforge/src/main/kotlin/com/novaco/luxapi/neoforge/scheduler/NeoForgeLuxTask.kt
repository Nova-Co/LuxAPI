package com.novaco.luxapi.neoforge.scheduler

import com.novaco.luxapi.commons.scheduler.LuxTask

/**
 * NeoForge implementation of a scheduled task.
 * Holds the execution state and allows for cancellation.
 */
class NeoForgeLuxTask(
    override val id: Int,
    override val isAsync: Boolean
) : LuxTask {

    @Volatile
    override var isCancelled: Boolean = false
        private set

    /**
     * Flags this task as cancelled. The scheduler will remove it on the next tick.
     */
    override fun cancel() {
        this.isCancelled = true
    }
}