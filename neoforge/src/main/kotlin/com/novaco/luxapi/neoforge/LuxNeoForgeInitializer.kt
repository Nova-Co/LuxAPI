package com.novaco.luxapi.neoforge

import com.novaco.luxapi.cobblemon.LuxCobblemon
import com.novaco.luxapi.commons.LuxAPI
import com.novaco.luxapi.commons.command.injector.InjectorRegistry
import com.novaco.luxapi.neoforge.command.NeoForgeCommandManager
import com.novaco.luxapi.neoforge.event.NeoForgeEventBridge
import com.novaco.luxapi.neoforge.gui.NeoForgeGuiBuilder
import com.novaco.luxapi.neoforge.gui.NeoForgePaginatedGuiBuilder
import com.novaco.luxapi.neoforge.player.NeoForgePlayerManager
import com.novaco.luxapi.neoforge.scheduler.NeoForgeLuxScheduler
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.neoforge.event.server.ServerStartingEvent
import org.slf4j.LoggerFactory

/**
 * The main entry point and initializer for the NeoForge platform.
 */
@Mod(LuxNeoForgeInitializer.MOD_ID)
class LuxNeoForgeInitializer(modEventBus: IEventBus) {

    companion object {
        const val MOD_ID = "luxapi"
        val logger = LoggerFactory.getLogger(MOD_ID)

        val commandManager = NeoForgeCommandManager()
    }

    init {
        logger.info("Initializing LuxAPI for NeoForge 1.21.1...")
        LuxAPI.init()
        LuxCobblemon.init()
        LuxAPI.guiProvider = { NeoForgeGuiBuilder() }
        LuxAPI.paginatedGuiProvider = { NeoForgePaginatedGuiBuilder() }

        val neoForgeScheduler = NeoForgeLuxScheduler()
        LuxAPI.schedulerProvider = { neoForgeScheduler }
        neoForgeScheduler.register()

        NeoForgeEventBridge.register()

        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands)
        NeoForge.EVENT_BUS.addListener(this::onServerStarting)
    }

    /**
     * Handles the command registration event when the server dispatcher is ready.
     */
    private fun onRegisterCommands(event: RegisterCommandsEvent) {
        commandManager.setDispatcher(event.dispatcher)
        logger.info("LuxAPI connected to NeoForge Command Dispatcher.")
    }

    /**
     * Handles the server starting event to initialize the player manager and injectors.
     */
    private fun onServerStarting(event: ServerStartingEvent) {
        val server = event.server

        val playerManager = NeoForgePlayerManager(server)
        InjectorRegistry.registerPlayerInjector(playerManager)

        logger.info("LuxAPI Player Injector (NeoForge) registered successfully!")
    }
}