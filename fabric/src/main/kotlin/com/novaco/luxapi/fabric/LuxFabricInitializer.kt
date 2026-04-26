package com.novaco.luxapi.fabric

import com.novaco.luxapi.cobblemon.LuxCobblemon
import com.novaco.luxapi.cobblemon.boss.aggro.BossDamageListener
import com.novaco.luxapi.cobblemon.evolution.EvolutionHookManager
import com.novaco.luxapi.commons.LuxAPI
import com.novaco.luxapi.commons.chat.placeholder.DefaultPlayerProvider
import com.novaco.luxapi.commons.chat.placeholder.PlaceholderManager
import com.novaco.luxapi.commons.command.injector.InjectorRegistry
import com.novaco.luxapi.fabric.command.FabricCommandManager
import com.novaco.luxapi.fabric.event.FabricEventBridge
import com.novaco.luxapi.fabric.gui.FabricGuiBuilder
import com.novaco.luxapi.fabric.gui.FabricPaginatedGuiBuilder
import com.novaco.luxapi.fabric.player.FabricPlayerManager
import com.novaco.luxapi.fabric.scheduler.FabricLuxScheduler
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.world.entity.LivingEntity
import org.slf4j.LoggerFactory

class LuxFabricInitializer : ModInitializer {

    companion object {
        const val MOD_ID = "luxapi"
        val logger = LoggerFactory.getLogger(MOD_ID)

        val commandManager = FabricCommandManager()
    }

    override fun onInitialize() {
        logger.info("Initializing LuxAPI for Fabric 1.21.1...")

        LuxAPI.init()
        LuxCobblemon.init()

        PlaceholderManager.register(DefaultPlayerProvider())

        EvolutionHookManager.initialize()

        LuxAPI.guiProvider = { FabricGuiBuilder() }
        LuxAPI.paginatedGuiProvider = { FabricPaginatedGuiBuilder() }

        val fabricScheduler = FabricLuxScheduler()
        LuxAPI.schedulerProvider = { fabricScheduler }
        fabricScheduler.registerTickListener()

        FabricEventBridge.register()

        ServerLivingEntityEvents.ALLOW_DAMAGE.register { entity, source, amount ->
            val sourceEntity = source.entity as? LivingEntity
            BossDamageListener.processDamage(entity, sourceEntity, amount)
            true
        }

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            commandManager.setDispatcher(dispatcher)
        }

        ServerLifecycleEvents.SERVER_STARTING.register { server ->
            val playerManager = FabricPlayerManager(server)
            InjectorRegistry.registerPlayerInjector(playerManager)
            logger.info("LuxAPI Player Injector (Fabric) registered successfully!")
        }
    }
}