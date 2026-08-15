package com.novaco.luxapi.core.scoreboard

import net.minecraft.ChatFormatting
import net.minecraft.server.ServerScoreboard
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Team

/**
 * Utility for creating and configuring scoreboard teams (glow color, nametag visibility,
 * collision, friendly fire) independent of the sidebar display in [ScoreboardManager].
 */
object TeamUtils {

    /**
     * Returns the team registered under [name], creating it if it doesn't exist yet.
     */
    fun getOrCreateTeam(scoreboard: ServerScoreboard, name: String): PlayerTeam {
        return scoreboard.getPlayerTeam(name) ?: scoreboard.addPlayerTeam(name)
    }

    /**
     * Removes a team by name. No-ops if it doesn't exist.
     */
    fun removeTeam(scoreboard: ServerScoreboard, name: String) {
        scoreboard.getPlayerTeam(name)?.let { scoreboard.removePlayerTeam(it) }
    }

    /**
     * Adds [playerName] to [team], moving them out of any other team they're on.
     */
    fun addMember(scoreboard: ServerScoreboard, team: PlayerTeam, playerName: String) {
        scoreboard.addPlayerToTeam(playerName, team)
    }

    /**
     * Configures color (also used for glowing outline color), nametag visibility, and
     * collision rule in one call. Any null parameter leaves that setting unchanged.
     */
    fun configure(
        team: PlayerTeam,
        color: ChatFormatting? = null,
        nameTagVisibility: Team.Visibility? = null,
        collisionRule: Team.CollisionRule? = null,
        allowFriendlyFire: Boolean? = null
    ) {
        color?.let { team.color = it }
        nameTagVisibility?.let { team.nameTagVisibility = it }
        collisionRule?.let { team.collisionRule = it }
        allowFriendlyFire?.let { team.isAllowFriendlyFire = it }
    }
}
