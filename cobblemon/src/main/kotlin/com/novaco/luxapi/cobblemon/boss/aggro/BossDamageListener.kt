package com.novaco.luxapi.cobblemon.boss.aggro

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.novaco.luxapi.cobblemon.boss.minion.BossMinionManager
import com.novaco.luxapi.core.scoreboard.ScoreboardManager
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.Projectile

/**
 * Automates the tracking of damage dealt to boss entities and dynamically
 * updates the aggro tables and visual scoreboards.
 * * Note: This is a Common API. Platform-specific event listeners (Fabric/NeoForge)
 * must call [processDamage] when a LivingDamage event occurs.
 */
object BossDamageListener {

    /**
     * The entry point for platform-specific events to push damage data into the Boss API.
     */
    fun processDamage(entity: LivingEntity, sourceEntity: LivingEntity?, amount: Float) {
        if (entity !is PokemonEntity) return
        if (!entity.tags.contains("lux_is_boss") && !entity.tags.contains("lux_is_world_boss")) return

        val attacker = resolveAttacker(sourceEntity) ?: return

        recordDamage(entity, attacker, amount.toDouble())
    }

    /**
     * The core processor that intercepts damage, updates the internal aggro table,
     * and forces a real-time refresh of the visual scoreboard.
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
     * Refreshes the top 5 damagers on the dynamic virtual scoreboard.
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
     * Utility to resolve the true attacker from a damage source, handling projectiles
     * and owner-pet relationships.
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