package com.novaco.luxapi.cobblemon.battle

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.novaco.luxapi.commons.player.LuxPlayer
import net.minecraft.server.level.ServerPlayer

/**
 * A global utility for managing active battles and spectators.
 * Essential for creating tournament systems, arenas, and status checks.
 */
object BattleManager {

    /**
     * Retrieves the current active battle that the specified player is participating in.
     *
     * @param player The target player.
     * @return The PokemonBattle instance, or null if the player is not currently in a battle.
     */
    fun getActiveBattle(player: LuxPlayer): PokemonBattle? {
        val serverPlayer = player.parent as ServerPlayer
        return Cobblemon.battleRegistry.getBattleByParticipatingPlayer(serverPlayer)
    }

    /**
     * Evaluates whether the specified player is currently engaged in a Pokemon battle.
     *
     * @param player The target player.
     * @return True if the player is battling, false otherwise.
     */
    fun isInBattle(player: LuxPlayer): Boolean {
        return getActiveBattle(player) != null
    }

    /**
     * Forces a player to spectate another player's ongoing battle.
     *
     * @param spectator The player who will watch the battle.
     * @param target The player who is currently battling.
     * @return True if successfully joined as a spectator, false if the target is not in a battle.
     */
    fun forceSpectate(spectator: LuxPlayer, target: LuxPlayer): Boolean {
        val targetBattle = getActiveBattle(target) ?: return false
        val spectatorPlayer = spectator.parent as ServerPlayer

        targetBattle.spectators.add(spectatorPlayer.uuid)
        return true
    }
}