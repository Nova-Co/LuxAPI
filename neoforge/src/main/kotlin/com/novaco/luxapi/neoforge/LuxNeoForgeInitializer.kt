package com.novaco.luxapi.neoforge

import com.novaco.luxapi.commons.command.injector.InjectorRegistry
import com.novaco.luxapi.neoforge.command.NeoForgeCommandManager
import com.novaco.luxapi.neoforge.player.NeoForgePlayerManager
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.neoforge.event.server.ServerStartingEvent
import org.slf4j.LoggerFactory

@Mod(LuxNeoForgeInitializer.MOD_ID)
class LuxNeoForgeInitializer(modEventBus: IEventBus) {

    companion object {
        const val MOD_ID = "luxapi"
        val logger = LoggerFactory.getLogger(MOD_ID)

        val commandManager = NeoForgeCommandManager()
    }

    init {
        logger.info("Initializing LuxAPI for NeoForge 1.21.1...")
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands)
        NeoForge.EVENT_BUS.addListener(this::onServerStarting)
    }

    /**
     * ทำหน้าที่เหมือน CommandRegistrationCallback ใน Fabric
     */
    private fun onRegisterCommands(event: RegisterCommandsEvent) {
        commandManager.setDispatcher(event.dispatcher)
        logger.info("LuxAPI connected to NeoForge Command Dispatcher.")
    }

    /**
     * ทำหน้าที่เหมือน ServerLifecycleEvents.SERVER_STARTING ใน Fabric
     */
    private fun onServerStarting(event: ServerStartingEvent) {
        val server = event.server

        val playerManager = NeoForgePlayerManager(server)
        InjectorRegistry.registerPlayerInjector(playerManager)

        logger.info("LuxAPI Player Injector (NeoForge) registered successfully!")
    }
}