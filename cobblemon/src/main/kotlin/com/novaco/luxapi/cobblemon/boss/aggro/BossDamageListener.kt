package com.novaco.luxapi.cobblemon.boss.aggro

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.novaco.luxapi.cobblemon.boss.minion.BossMinionManager
import com.novaco.luxapi.core.scoreboard.ScoreboardManager
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.Projectile

/**
 * Listens for damage events, specifically tracking damage dealt to boss entities.
 * It updates aggro tables and associated visual elements like scoreboards in real-time.
 * Note: This is a common API. Platform-specific event listeners (e.g., Fabric/NeoForge)
 * must call the [processDamage] method when a LivingDamage event occurs.
 */
object BossDamageListener {

    /**
     * Processes incoming damage events. This is the main entry point for platform-specific listeners.
     * It filters for damage dealt to boss entities and identifies the responsible attacker.
     *
     * @param entity The entity receiving damage.
     * @param sourceEntity The entity that is the source of the damage.
     * @param amount The amount of damage dealt.
     */
    fun processDamage(entity: LivingEntity, sourceEntity: LivingEntity?, amount: Float) {
        if (entity !is PokemonEntity) return
        if (!entity.tags.contains("lux_is_boss") && !entity.tags.contains("lux_is_world_boss")) return

        val attacker = resolveAttacker(sourceEntity) ?: return

        recordDamage(entity, attacker, amount.toDouble())
    }

    /**
     * Records the damage dealt by an attacker to a boss, updating the aggro system and scoreboard.
     * It also triggers a re-evaluation of minion targets based on the new aggro levels.
     *
     * @param bossEntity The boss Pokemon entity that received damage.
     * @param attacker The player who dealt the damage.
     * @param damageAmount The amount of damage dealt.
     */
    private fun recordDamage(bossEntity: PokemonEntity, attacker: ServerPlayer, damageAmount: Double) {
        // Update internal aggro database
        BossAggroManager.addAggro(bossEntity, attacker, damageAmount)

        // Trigger real-time scoreboard update
        updateScoreboard(bossEntity)

        // (Optional) Force minions to re-evaluate their targets if aggro shifts significantly
        BossMinionManager.updateMinionTargets(bossEntity)
    }

    /**
     * Updates the dynamic scoreboard to reflect the current top damagers.
     * It fetches the latest aggro data and displays the top 5 players.
     *
     * @param bossEntity The boss Pokemon entity associated with the scoreboard.
     */
    private fun updateScoreboard(bossEntity: PokemonEntity) {
        val server = bossEntity.server ?: return
        val raidBoard = ScoreboardManager.getScoreboard("raid_${bossEntity.uuid}") ?: return

        val topDamagers = BossAggroManager.getTopDamagers(bossEntity)

        // Display top 5 DPS on the sidebar (Index 5 is highest, 1 is lowest in typical ranking)
        topDamagers.take(5).forEachIndexed { index, pair ->
            val player = server.playerList.getPlayer(pair.first)
            val playerName = player?.name?.string ?: "Unknown"
            val damage = pair.second.toInt()

            raidBoard.setLine(5 - index, "§e${index + 1}. $playerName - $damage DMG") { uuid ->
                server.playerList.getPlayer(uuid)
            }
        }
    }

    /**
     * Resolves the actual player attacker from a damage source.
     * This handles cases where the damage comes from a projectile or a player's Pokemon.
     *
     * @param sourceEntity The source of the damage (e.g., player, projectile, Pokemon).
     * @return The ServerPlayer who is the ultimate source of the damage, or null if not determinable.
     */
    private fun resolveAttacker(sourceEntity: LivingEntity?): ServerPlayer? {
        if (sourceEntity is ServerPlayer) return sourceEntity

        if (sourceEntity is Projectile) {
            val owner = sourceEntity.owner
            if (owner is ServerPlayer) return owner
        }

        if (sourceEntity is PokemonEntity) {
            return sourceEntity.pokemon.getOwnerPlayer()
        }

        return null
    }
}