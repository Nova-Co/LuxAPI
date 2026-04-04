package com.novaco.luxapi.cobblemon.listener

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.pokemon.PokemonCapturedEvent
import com.cobblemon.mod.common.battles.actor.PlayerBattleActor
import com.novaco.luxapi.cobblemon.listener.CobblemonEventHandler.register
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
        registerCaptureEvent()
        registerBattleVictoryEvent()
    }

    /**
     * Listens for whenever a player successfully captures a wild Pokémon.
     * Useful for updating Pokédex quests or broadcasting rare catches.
     */
    private fun registerCaptureEvent() {
        CobblemonEvents.POKEMON_CAPTURED.subscribe { event: PokemonCapturedEvent ->
            val player = event.player
            val pokemon = event.pokemon

            if (pokemon.shiny) {
                val message = Component.literal("§6★ §e${player.name.string} just caught a Shiny ${pokemon.species.name.replaceFirstChar { it.uppercase() }}! §6★")
                player.server?.playerList?.broadcastSystemMessage(message, false)
            }

            // TODO: Hook into database to update player capture statistics
            // DatabaseManager.addCaptureStat(player.uuid, pokemon.species.name)
        }
    }

    /**
     * Listens for the conclusion of a battle where the player is victorious.
     * This is the perfect injection point for competitive ranking systems like NoxLeague.
     */
    private fun registerBattleVictoryEvent() {
        CobblemonEvents.BATTLE_VICTORY.subscribe { event ->
            val winners = event.winners
                .filterIsInstance<PlayerBattleActor>()
                .mapNotNull { it.entity }

            for (serverPlayer in winners) {
                val playerName = serverPlayer.gameProfile.name

                serverPlayer.sendSystemMessage(Component.literal("§aBattle Won! +10 League Points."))
            }
        }
    }
}