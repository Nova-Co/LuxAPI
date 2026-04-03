package com.novaco.luxapi.cobblemon.battle

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.novaco.luxapi.commons.player.LuxPlayer

/**
 * Extension functions to streamline battle initiations and status checks.
 * Allows developers to write clean, single-line battle commands.
 */

/**
 * Instantly initiates a standard 1v1 battle against a wild Pokémon entity.
 *
 * @param wildEntity The target wild Pokémon.
 */
fun LuxPlayer.forceBattleWild(wildEntity: PokemonEntity) {
    BattleBuilder(this).startAgainstWild(wildEntity)
}

/**
 * Instantly initiates a standard 1v1 PvP battle against another player.
 *
 * @param opponent The targeted opponent player.
 */
fun LuxPlayer.forceBattlePlayer(opponent: LuxPlayer) {
    BattleBuilder(this).startAgainstPlayer(opponent)
}

/**
 * Property extension to quickly check if the player is currently in a battle.
 * Usage: if (player.isBattling) { ... }
 */
val LuxPlayer.isBattling: Boolean
    get() = BattleManager.isInBattle(this)