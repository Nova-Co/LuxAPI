package com.novaco.luxapi.fabric

import com.novaco.luxapi.commons.command.injector.InjectorRegistry
import com.novaco.luxapi.fabric.command.FabricCommandManager
import com.novaco.luxapi.fabric.player.FabricPlayerManager
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import org.slf4j.LoggerFactory

class LuxFabricInitializer : ModInitializer {

    companion object {
        const val MOD_ID = "luxapi"
        val logger = LoggerFactory.getLogger(MOD_ID)

        val commandManager = FabricCommandManager()
    }

    override fun onInitialize() {
        logger.info("Initializing LuxAPI for Fabric 1.21.1...")

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            commandManager.setDispatcher(dispatcher)
        }

        ServerLifecycleEvents.SERVER_STARTING.register { server ->
            val playerManager = FabricPlayerManager(server)

            InjectorRegistry.registerPlayerInjector(playerManager)

            logger.info("LuxAPI Player Injector registered successfully!")
        }
    }
}