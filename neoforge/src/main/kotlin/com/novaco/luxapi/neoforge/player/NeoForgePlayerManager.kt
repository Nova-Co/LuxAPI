package com.novaco.luxapi.neoforge.player

import com.novaco.luxapi.commons.player.LuxPlayer
import com.novaco.luxapi.commons.player.PlayerManager
import net.minecraft.server.MinecraftServer
import java.util.UUID

/**
 * Manages player retrieval and conversions specifically for the NeoForge platform.
 */
class NeoForgePlayerManager(private val server: MinecraftServer) : PlayerManager {

    override fun getPlayer(name: String): LuxPlayer? {
        val player = server.playerList.getPlayerByName(name) ?: return null
        return NeoForgeLuxPlayer(player)
    }

    override fun getPlayer(uuid: UUID): LuxPlayer? {
        val player = server.playerList.getPlayer(uuid) ?: return null
        return NeoForgeLuxPlayer(player)
    }

    override fun getOnlinePlayers(): List<LuxPlayer> {
        return server.playerList.players.map { NeoForgeLuxPlayer(it) }
    }
}