package com.novaco.luxapi.cobblemon.listener

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor
import com.novaco.luxapi.cobblemon.hooks.HookManager
import com.novaco.luxapi.cobblemon.listener.CobblemonEventHandler.register
import com.novaco.luxapi.commons.LuxAPI
import com.novaco.luxapi.commons.player.PlayerManager
import net.minecraft.network.chat.Component

/**
 * The central event handler for intercepting Cobblemon-specific actions.
 * This object listens to the native Cobblemon Event Bus and provides hooks
 * for custom server mechanics (e.g., Quest tracking, NoxLeague rankings).
 *
 * It must be initialized during the mod/plugin's startup phase by calling [register].
 *
 */
object CobblemonEventHandler {

    /**
     * Registers all custom event listeners to the Cobblemon Event Bus.
     * Call this method once inside your main mod initializer.
     */
    fun register() {
        registerCapturePipeline()
        registerBattleVictoryPipeline()
        registerLifecyclePipelines()
    }

    /**
     * Handles everything related to Pokémon capture events.
     */
    private fun registerCapturePipeline() {
        CobblemonEvents.POKEMON_CAPTURED.subscribe { event: PokemonCapturedEvent ->
            val player = event.player
            val pokemon = event.pokemon

            // 1. Core Native Logic (Shiny broadcasts)
            if (pokemon.shiny) {
                val message = Component.literal("§6★ §e${player.name.string} just caught a Shiny ${pokemon.species.name.replaceFirstChar { it.uppercase() }}! §6★")
                player.server?.playerList?.broadcastSystemMessage(message, false)
            }

            // 2. Dispatch to the Cross-Platform Hook system
            val playerManager = LuxAPI.getService<PlayerManager>()
            val luxPlayer = playerManager?.getPlayer(player.uuid)
            if (luxPlayer != null) {
                HookManager.broadcastCatch(luxPlayer, pokemon)
            }
        }
    }

    /**
     * Handles everything related to battle end and ranking calculations.
     */
    private fun registerBattleVictoryPipeline() {
        CobblemonEvents.BATTLE_VICTORY.subscribe { event ->
            val winners = event.winners
                .filterIsInstance<PlayerBattleActor>()
                .mapNotNull { it.entity }

            val playerManager = LuxAPI.getService<PlayerManager>()

            for (serverPlayer in winners) {
                // 1. Core Native Messaging
                serverPlayer.sendSystemMessage(Component.literal("§aBattle Won! +10 League Points."))

                // 2. Dispatch to the Cross-Platform Hook system
                val luxPlayer = playerManager?.getPlayer(serverPlayer.uuid)
                if (luxPlayer != null) {
                    HookManager.broadcastDefeat(luxPlayer, event)
                }
            }
        }
    }

    /**
     * Handles standard development lifecycles for internal translation.
     */
    private fun registerLifecyclePipelines() {
        // Level Up Pipeline
        CobblemonEvents.LEVEL_UP_EVENT.subscribe { event ->
            val ownerUUID = event.pokemon.getOwnerUUID() ?: return@subscribe
            val luxPlayer = LuxAPI.getService<PlayerManager>()?.getPlayer(ownerUUID)
            if (luxPlayer != null) {
                HookManager.broadcastLevelUp(luxPlayer, event)
            }
        }

        // Evolution Pipeline
        CobblemonEvents.EVOLUTION_COMPLETE.subscribe { event ->
            val ownerUUID = event.pokemon.getOwnerUUID() ?: return@subscribe
            val luxPlayer = LuxAPI.getService<PlayerManager>()?.getPlayer(ownerUUID)
            if (luxPlayer != null) {
                HookManager.broadcastEvolution(luxPlayer, event)
            }
        }

        // Hatching Pipeline
        CobblemonEvents.HATCH_EGG_POST.subscribe { event ->
            val ownerUUID = event.pokemon.getOwnerUUID() ?: return@subscribe
            val luxPlayer = LuxAPI.getService<PlayerManager>()?.getPlayer(ownerUUID)
            if (luxPlayer != null) {
                HookManager.broadcastEggHatch(luxPlayer, event)
            }
        }
    }
}