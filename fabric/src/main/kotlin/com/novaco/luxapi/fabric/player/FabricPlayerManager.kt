package com.novaco.luxapi.fabric.player

import com.novaco.luxapi.commons.player.LuxPlayer
import com.novaco.luxapi.commons.player.PlayerManager
import net.minecraft.server.MinecraftServer
import java.util.UUID

/**
 * Fabric implementation to retrieve online players from the MinecraftServer.
 */
class FabricPlayerManager(private val server: MinecraftServer) : PlayerManager {

    override fun getPlayer(name: String): LuxPlayer? {
        val player = server.playerList.getPlayerByName(name) ?: return null
        return FabricLuxPlayer(player)
    }

    override fun getPlayer(uuid: UUID): LuxPlayer? {
        val player = server.playerList.getPlayer(uuid) ?: return null
        return FabricLuxPlayer(player)
    }

    override fun getOnlinePlayers(): List<LuxPlayer> {
        return server.playerList.players.map { FabricLuxPlayer(it) }
    }
}