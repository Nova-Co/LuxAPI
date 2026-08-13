package com.novaco.luxapi.bukkit.scheduler

import com.novaco.luxapi.commons.scheduler.LuxTask
import org.bukkit.scheduler.BukkitTask

/**
 * Bukkit implementation of a scheduled task, wrapping a native [BukkitTask].
 */
class BukkitLuxTask(
    private val bukkitTask: BukkitTask,
    override val isAsync: Boolean
) : LuxTask {

    override val id: Int
        get() = bukkitTask.taskId

    @Volatile
    private var cancelled: Boolean = false

    override val isCancelled: Boolean
        get() = cancelled || bukkitTask.isCancelled

    override fun cancel() {
        cancelled = true
        bukkitTask.cancel()
    }
}
