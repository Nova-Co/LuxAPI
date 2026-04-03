package com.novaco.luxapi.cobblemon.hooks

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.novaco.luxapi.commons.LuxAPI
import com.novaco.luxapi.commons.player.PlayerManager

/**
 * Internal bridge that listens to raw CobblemonEvents and redirects them
 * to the simplified LuxAPI Hook system.
 */
object PokemonHooks {

    fun initialize() {
        val playerManager = LuxAPI.getService<PlayerManager>()

        CobblemonEvents.POKEMON_CAPTURED.subscribe { event ->
            val serverPlayer = event.player
            val caughtPokemon = event.pokemon

            val playerManager = LuxAPI.getService<PlayerManager>()
            val luxPlayer = playerManager?.getPlayer(serverPlayer.uuid)

            if (luxPlayer != null) {
                HookManager.broadcastCatch(luxPlayer, caughtPokemon)
            }
        }

        CobblemonEvents.BATTLE_VICTORY.subscribe { event ->
            val winners = event.winners
                .filterIsInstance<com.cobblemon.mod.common.battles.actor.PlayerBattleActor>()
                .mapNotNull { it.entity }

            val playerManager = LuxAPI.getService<PlayerManager>()

            for (serverPlayer in winners) {
                val luxPlayer = playerManager?.getPlayer(serverPlayer.uuid)

                if (luxPlayer != null) {
                    HookManager.broadcastDefeat(luxPlayer, event)
                }
            }
        }

        CobblemonEvents.LEVEL_UP_EVENT.subscribe { event ->
            val pokemon = event.pokemon
            val ownerUUID = pokemon.getOwnerUUID()

            if (ownerUUID != null) {
                val playerManager = LuxAPI.getService<PlayerManager>()
                val luxPlayer = playerManager?.getPlayer(ownerUUID)

                if (luxPlayer != null) {
                    HookManager.broadcastLevelUp(luxPlayer, event)
                }
            }
        }

        CobblemonEvents.EVOLUTION_COMPLETE.subscribe { event ->
            val ownerUUID = event.pokemon.getOwnerUUID()
            if (ownerUUID != null) {
                val luxPlayer = playerManager?.getPlayer(ownerUUID)
                if (luxPlayer != null) {
                    HookManager.broadcastEvolution(luxPlayer, event)
                }
            }
        }

        CobblemonEvents.HATCH_EGG_POST.subscribe { event ->
            val ownerUUID = event.pokemon.getOwnerUUID()
            if (ownerUUID != null) {
                val luxPlayer = playerManager?.getPlayer(ownerUUID)
                if (luxPlayer != null) {
                    HookManager.broadcastEggHatch(luxPlayer, event)
                }
            }
        }
    }
}