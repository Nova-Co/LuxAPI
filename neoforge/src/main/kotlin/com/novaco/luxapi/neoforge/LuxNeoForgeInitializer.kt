package com.novaco.luxapi.neoforge

import com.novaco.luxapi.cobblemon.LuxCobblemon
import com.novaco.luxapi.cobblemon.boss.aggro.BossDamageListener
import com.novaco.luxapi.cobblemon.evolution.EvolutionHookManager
import com.novaco.luxapi.cobblemon.npc.event.NPCInteractionListener
import com.novaco.luxapi.commons.LuxAPI
import com.novaco.luxapi.commons.chat.placeholder.DefaultPlayerProvider
import com.novaco.luxapi.commons.chat.placeholder.PlaceholderManager
import com.novaco.luxapi.commons.command.injector.InjectorRegistry
import com.novaco.luxapi.commons.command.sender.CommandSender
import com.novaco.luxapi.commons.command.tab.TabHandler
import com.novaco.luxapi.commons.command.tab.TabRegistry
import com.novaco.luxapi.core.server.LuxServerManager
import com.novaco.luxapi.neoforge.command.NeoForgeCommandManager
import com.novaco.luxapi.neoforge.event.NeoForgeEventBridge
import com.novaco.luxapi.neoforge.init.NeoForgeInitScanner
import com.novaco.luxapi.neoforge.gui.NeoForgeGuiBuilder
import com.novaco.luxapi.neoforge.gui.NeoForgePaginatedGuiBuilder
import com.novaco.luxapi.neoforge.player.NeoForgePlayerManager
import com.novaco.luxapi.commons.player.InMemoryPlayerLookupService
import com.novaco.luxapi.commons.player.PlayerLookupService
import com.novaco.luxapi.neoforge.scheduler.NeoForgeLuxScheduler
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResult
import net.neoforged.bus.api.IEventBus
import net.neoforged.bus.api.SubscribeEvent
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
class LuxNeoForgeInitializer(modEventBus: IEventBus) {

    companion object {
        const val MOD_ID = "luxapi"
        val logger = LoggerFactory.getLogger(MOD_ID)

        init {
            val neoForgeScheduler = NeoForgeLuxScheduler()
            LuxAPI.schedulerProvider = { neoForgeScheduler }
            neoForgeScheduler.register()
        }

        val commandManager = NeoForgeCommandManager()

        var playerManager: NeoForgePlayerManager? = null
            private set

        val playerLookupService: PlayerLookupService = InMemoryPlayerLookupService()
    }

    init {
        logger.info("Initializing LuxAPI for NeoForge 1.21.1...")

        LuxAPI.init()
        LuxCobblemon.init()

        PlaceholderManager.register(DefaultPlayerProvider())

        EvolutionHookManager.initialize()

        LuxAPI.guiProvider = { NeoForgeGuiBuilder() }
        LuxAPI.paginatedGuiProvider = { NeoForgePaginatedGuiBuilder() }

        NeoForgeEventBridge.register()

        val initializedCount = NeoForgeInitScanner.scanAndRun()
        logger.info("LuxAPI ran $initializedCount auto-discovered init task(s) across all loaded mods.")

        NeoForge.EVENT_BUS.register(this)

        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands)
        NeoForge.EVENT_BUS.addListener(this::onServerStarting)
        NeoForge.EVENT_BUS.addListener(this::onServerStopped)
    }

    private fun onRegisterCommands(event: RegisterCommandsEvent) {
        commandManager.setDispatcher(event.dispatcher)
        logger.info("LuxAPI connected to NeoForge Command Dispatcher.")
    }

    private fun onServerStarting(event: ServerStartingEvent) {
        val server = event.server

        LuxServerManager.init(server)

        val manager = NeoForgePlayerManager(server)
        playerManager = manager
        InjectorRegistry.registerPlayerInjector(manager, playerLookupService)
        InjectorRegistry.registerOfflinePlayerInjector(manager, playerLookupService)

        InjectorRegistry.register<ServerPlayer> { _, args, index ->
            if (args.size > index) server.playerList.getPlayerByName(args[index]) else null
        }

        TabRegistry.register(ServerPlayer::class.java, object : TabHandler {
            override fun getSuggestions(sender: CommandSender, args: Array<String>): List<String> {
                return server.playerList.players.map { it.scoreboardName }
            }
        })

        logger.info("LuxAPI Player Injector (NeoForge) registered successfully!")
    }

    private fun onServerStopped(event: ServerStoppedEvent) {
        LuxServerManager.clear()
        playerManager = null
    }

    @SubscribeEvent
    fun onEntityDamaged(event: LivingDamageEvent.Post) {
        val entity = event.entity
        val sourceEntity = event.source.directEntity
        val amount = event.newDamage

        BossDamageListener.processDamage(entity, sourceEntity, amount)
    }

    @SubscribeEvent
    fun onEntityInteract(event: PlayerInteractEvent.EntityInteract) {
        val player = event.entity as? ServerPlayer ?: return
        val target = event.target

        if (NPCInteractionListener.onInteract(player, target)) {
            event.isCanceled = true
        }
    }
}