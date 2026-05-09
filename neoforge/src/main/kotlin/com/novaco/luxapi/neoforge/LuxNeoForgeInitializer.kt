package com.novaco.luxapi.neoforge

import com.novaco.luxapi.cobblemon.LuxCobblemon
import com.novaco.luxapi.cobblemon.boss.aggro.BossDamageListener
import com.novaco.luxapi.cobblemon.evolution.EvolutionHookManager
import com.novaco.luxapi.cobblemon.npc.event.NPCInteractionListener
import com.novaco.luxapi.commons.LuxAPI
import com.novaco.luxapi.commons.chat.placeholder.DefaultPlayerProvider
import com.novaco.luxapi.commons.chat.placeholder.PlaceholderManager
import com.novaco.luxapi.commons.command.injector.InjectorRegistry
import com.novaco.luxapi.core.server.LuxServerManager
import com.novaco.luxapi.neoforge.command.NeoForgeCommandManager
import com.novaco.luxapi.neoforge.event.NeoForgeEventBridge
import com.novaco.luxapi.neoforge.gui.NeoForgeGuiBuilder
import com.novaco.luxapi.neoforge.gui.NeoForgePaginatedGuiBuilder
import com.novaco.luxapi.neoforge.player.NeoForgePlayerManager
import com.novaco.luxapi.neoforge.scheduler.NeoForgeLuxScheduler
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity
import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
import net.neoforged.fml.common.Mod
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent
import net.neoforged.neoforge.event.server.ServerStartingEvent
import net.neoforged.neoforge.event.server.ServerStoppedEvent
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

        PlaceholderManager.register(DefaultPlayerProvider())

        EvolutionHookManager.initialize()

        LuxAPI.guiProvider = { NeoForgeGuiBuilder() }
        LuxAPI.paginatedGuiProvider = { NeoForgePaginatedGuiBuilder() }

        val neoForgeScheduler = NeoForgeLuxScheduler()
        LuxAPI.schedulerProvider = { neoForgeScheduler }
        neoForgeScheduler.register()

        NeoForgeEventBridge.register()
        NeoForge.EVENT_BUS.register(this)

        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands)
        NeoForge.EVENT_BUS.addListener(this::onServerStarting)
        NeoForge.EVENT_BUS.addListener(this::onServerStopped)
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

        LuxServerManager.init(server)

        val playerManager = NeoForgePlayerManager(server)
        InjectorRegistry.registerPlayerInjector(playerManager)

        logger.info("LuxAPI Player Injector (NeoForge) registered successfully!")
    }

    private fun onServerStopped(event: ServerStoppedEvent) {
        LuxServerManager.clear()
    }

    /**
     * Catches entity damage events and forwards them to the Common Boss API
     * to manage boss aggro, minion targeting, and scoreboards.
     */
    @SubscribeEvent
    fun onEntityDamaged(event: LivingDamageEvent.Post) {
        val entity = event.entity
        val sourceEntity = event.source.entity as? LivingEntity
        val amount = event.newDamage

        BossDamageListener.processDamage(entity, sourceEntity, amount)
    }

    /**
     * Catches entity interact events
     */
    @SubscribeEvent
    fun onEntityInteract(event: PlayerInteractEvent.EntityInteract) {
        val player = event.entity as? ServerPlayer ?: return
        val target = event.target

        if (NPCInteractionListener.onInteract(player, target)) {
            event.isCanceled = true // Prevent default behavior if we handled it
        }
    }
}