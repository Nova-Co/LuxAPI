package com.novaco.luxapi.cobblemon.hooks

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.novaco.luxapi.commons.LuxAPI
import com.novaco.luxapi.commons.player.PlayerManager

/**
 * An internal bridge that listens to raw Cobblemon events and translates them
 * into the simplified LuxAPI hook system. This object is responsible for
 * subscribing to the underlying game events and broadcasting them through the [HookManager].
 */
object PokemonHooks {

    /**
     * Initializes all the necessary event listeners. This method should be called
     * once during the server startup phase to ensure all hooks are active.
     */
    fun initialize() {
        val playerManager = LuxAPI.getService<PlayerManager>()

        // Listen for when a Pokémon is captured.
        CobblemonEvents.POKEMON_CAPTURED.subscribe { event ->
            val serverPlayer = event.player
            val caughtPokemon = event.pokemon

            val playerManager = LuxAPI.getService<PlayerManager>()
            val luxPlayer = playerManager?.getPlayer(serverPlayer.uuid)

            if (luxPlayer != null) {
                HookManager.broadcastCatch(luxPlayer, caughtPokemon)
            }
        }

        // Listen for when a battle concludes with a victory.
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

        // Listen for when a Pokémon levels up.
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

        // Listen for when a Pokémon completes its evolution.
        CobblemonEvents.EVOLUTION_COMPLETE.subscribe { event ->
            val ownerUUID = event.pokemon.getOwnerUUID()
            if (ownerUUID != null) {
                val luxPlayer = playerManager?.getPlayer(ownerUUID)
                if (luxPlayer != null) {
                    HookManager.broadcastEvolution(luxPlayer, event)
                }
            }
        }

        // Listen for when a Pokémon egg hatches.
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