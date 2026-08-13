package com.novaco.luxapi.bukkit.scheduler

import com.novaco.luxapi.commons.scheduler.LuxScheduler
import com.novaco.luxapi.commons.scheduler.LuxTask
import org.bukkit.plugin.Plugin
import java.util.concurrent.ConcurrentHashMap

/**
 * Bukkit implementation of [LuxScheduler].
 * Delegates directly to the server's native [org.bukkit.scheduler.BukkitScheduler]
 * rather than reimplementing a tick loop — Bukkit already exposes exactly this contract.
 *
 * @param plugin The owning plugin instance, required by Bukkit's scheduler API.
 */
class BukkitLuxScheduler(private val plugin: Plugin) : LuxScheduler {

    private val activeTasks = ConcurrentHashMap<Int, BukkitLuxTask>()

    private fun track(task: BukkitLuxTask): LuxTask {
        activeTasks[task.id] = task
        return task
    }

    override fun run(runnable: Runnable): LuxTask {
        val task = plugin.server.scheduler.runTask(plugin, runnable)
        return track(BukkitLuxTask(task, isAsync = false))
    }

    override fun runAsync(runnable: Runnable): LuxTask {
        val task = plugin.server.scheduler.runTaskAsynchronously(plugin, runnable)
        return track(BukkitLuxTask(task, isAsync = true))
    }

    override fun runLater(delay: Long, runnable: Runnable): LuxTask {
        val task = plugin.server.scheduler.runTaskLater(plugin, runnable, delay)
        return track(BukkitLuxTask(task, isAsync = false))
    }

    override fun runLaterAsync(delay: Long, runnable: Runnable): LuxTask {
        val task = plugin.server.scheduler.runTaskLaterAsynchronously(plugin, runnable, delay)
        return track(BukkitLuxTask(task, isAsync = true))
    }

    override fun runRepeating(delay: Long, period: Long, runnable: Runnable): LuxTask {
        val task = plugin.server.scheduler.runTaskTimer(plugin, runnable, delay, period)
        return track(BukkitLuxTask(task, isAsync = false))
    }

    override fun runRepeatingAsync(delay: Long, period: Long, runnable: Runnable): LuxTask {
        val task = plugin.server.scheduler.runTaskTimerAsynchronously(plugin, runnable, delay, period)
        return track(BukkitLuxTask(task, isAsync = true))
    }

    override fun cancelAll() {
        plugin.server.scheduler.cancelTasks(plugin)
        activeTasks.values.forEach { it.cancel() }
        activeTasks.clear()
    }
}
