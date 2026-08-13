package com.novaco.luxapi.bukkit

import com.novaco.luxapi.bukkit.event.BukkitEventBridge
import com.novaco.luxapi.bukkit.gui.BukkitGuiBuilder
import com.novaco.luxapi.bukkit.gui.BukkitGuiListener
import com.novaco.luxapi.bukkit.gui.BukkitPaginatedGuiBuilder
import com.novaco.luxapi.bukkit.player.BukkitPlayerManager
import com.novaco.luxapi.bukkit.scheduler.BukkitLuxScheduler
import com.novaco.luxapi.commons.LuxAPI
import org.bukkit.plugin.Plugin

/**
 * Convenience entry point wiring every Bukkit platform bridge (player, scheduler, GUI, event)
 * into LuxAPI's `commons` providers in one call — the Bukkit-side counterpart of what
 * `LuxFabricInitializer`/`LuxNeoForgeInitializer` do at mod-init time.
 *
 * The `bukkit` module ships no `JavaPlugin` of its own (it's a library, like `commons`'
 * `BukkitCommandManager` already is) — a consuming plugin calls [initialize] once from its
 * own `onEnable()`, passing itself as the [Plugin] Bukkit's APIs require.
 */
object LuxBukkitBridge {

    /**
     * Initializes all Bukkit platform bridges and registers them with LuxAPI's `commons` layer.
     *
     * @param plugin The consuming plugin instance.
     * @return The [BukkitPlayerManager] backing this bridge, for direct use by the caller.
     */
    fun initialize(plugin: Plugin): BukkitPlayerManager {
        LuxAPI.init()

        val playerManager = BukkitPlayerManager()

        val scheduler = BukkitLuxScheduler(plugin)
        LuxAPI.schedulerProvider = { scheduler }

        LuxAPI.guiProvider = { BukkitGuiBuilder() }
        LuxAPI.paginatedGuiProvider = { BukkitPaginatedGuiBuilder() }

        BukkitEventBridge(plugin, playerManager).register()
        BukkitGuiListener(plugin).register()

        return playerManager
    }
}
