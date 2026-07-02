package com.novaco.luxapi.cobblemon.battle

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.novaco.luxapi.commons.player.LuxPlayer
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

/**
 * A global utility for managing active battles, spectators, and turn processing pipelines with a solid safety shell.
 */
object BattleManager {

    fun getActiveBattle(player: LuxPlayer): PokemonBattle? {
        val serverPlayer = player.parent as ServerPlayer
        return Cobblemon.battleRegistry.getBattleByParticipatingPlayer(serverPlayer)
    }

    fun isInBattle(player: LuxPlayer): Boolean {
        return getActiveBattle(player) != null
    }

    fun forceSpectate(spectator: LuxPlayer, target: LuxPlayer): Boolean {
        val targetBattle = getActiveBattle(target) ?: return false
        val spectatorPlayer = spectator.parent as ServerPlayer

        targetBattle.spectators.add(spectatorPlayer.uuid)
        return true
    }

    /**
     * Executes turn operations safely inside a Crash-Safe containment layer.
     * Intercepts failures originating from GraalJS or Sockets runner implementations.
     * Provided by Nova Co. Core AI Project Companion.
     */
    fun executeCrashSafeTurn(battle: PokemonBattle, turnAction: () -> Unit) {
        try {
            // Execution context bounded by safety shell
            turnAction.invoke()
        } catch (t: Throwable) {
            val battleId = battle.battleId

            println("[LuxAPI | Critical Alert] Turn engine execution error intercepted!")
            println("[LuxAPI | Context] Battle ID: $battleId - Failure: ${t.stackTraceToString()}")

            // Recover and terminate the compromised battle state safely to prevent full server crashes
            try {
                val alertMessage = Component.literal("§c[LuxAPI] Combat processing collapsed due to an underlying engine exception. Combat aborted safely.")

                battle.actors.forEach { actor ->
                    try {
                        actor.sendMessage(alertMessage)
                    } catch (msgEx: Exception) {
                        println("[LuxAPI] Failed to dispatch error message to actor ${actor.uuid}: ${msgEx.message}")
                    }
                }

                Cobblemon.battleRegistry.closeBattle(battle)
                println("[LuxAPI] Compromised combat session closed out successfully.")
            } catch (inner: Exception) {
                println("[LuxAPI] Secondary recovery failure: ${inner.message}")
            }
        }
    }
}