package com.novaco.luxapi.fabric

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
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.LivingEntity
import org.slf4j.LoggerFactory

/**
 * The main entry point and initializer for the Fabric platform.
 */
class LuxFabricInitializer : ModInitializer {

    companion object {
        const val MOD_ID = "luxapi"
        val logger = LoggerFactory.getLogger(MOD_ID)

        init {
            val fabricScheduler = FabricLuxScheduler()
            LuxAPI.schedulerProvider = { fabricScheduler }
            fabricScheduler.registerTickListener()
        }

        val commandManager = FabricCommandManager()

        var playerManager: FabricPlayerManager? = null
            private set
    }

    override fun onInitialize() {
        logger.info("Initializing LuxAPI for Fabric 1.21.1...")

        LuxAPI.init()
        LuxCobblemon.init()

        PlaceholderManager.register(DefaultPlayerProvider())

        EvolutionHookManager.initialize()

        LuxAPI.guiProvider = { FabricGuiBuilder() }
        LuxAPI.paginatedGuiProvider = { FabricPaginatedGuiBuilder() }

        FabricEventBridge.register()

        UseEntityCallback.EVENT.register { player, world, hand, target, hitResult ->
            if (!world.isClientSide && player is ServerPlayer) {
                if (NPCInteractionListener.onInteract(player, target)) {
                    return@register InteractionResult.SUCCESS
                }
            }
            InteractionResult.PASS
        }

        ServerLivingEntityEvents.ALLOW_DAMAGE.register { entity, source, amount ->
            val sourceEntity = source.entity as? LivingEntity
            BossDamageListener.processDamage(entity, sourceEntity, amount)
            true
        }

        CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
            commandManager.setDispatcher(dispatcher)
        }

        ServerLifecycleEvents.SERVER_STARTING.register { server ->
            LuxServerManager.init(server)

            val manager = FabricPlayerManager(server)
            playerManager = manager
            InjectorRegistry.registerPlayerInjector(manager)

            InjectorRegistry.register<ServerPlayer> { _, args, index ->
                if (args.size > index) server.playerList.getPlayerByName(args[index]) else null
            }

            TabRegistry.register(ServerPlayer::class.java, object : TabHandler {
                override fun getSuggestions(sender: CommandSender, args: Array<String>): List<String> {
                    return server.playerList.players.map { it.scoreboardName }
                }
            })

            logger.info("LuxAPI Player Injector (Fabric) registered successfully!")
        }

        ServerLifecycleEvents.SERVER_STOPPED.register { _ ->
            LuxServerManager.clear()
            playerManager = null
        }
    }
}