package com.novaco.luxapi.neoforge.player

import com.novaco.luxapi.commons.player.LuxPlayer
import com.novaco.luxapi.commons.player.PlayerManager
import kotlinx.coroutines.*
import net.minecraft.server.MinecraftServer
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * NeoForge-specific implementation of the PlayerManager.
 * Manages player sessions and caches LuxPlayer instances dynamically using a thread-safe registry.
 *
 * @param server The active MinecraftServer instance.
 */
class NeoForgePlayerManager(private val server: MinecraftServer) : PlayerManager {

    private val cachedPlayers = ConcurrentHashMap<UUID, LuxPlayer>()
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    /**
     * Caches and wraps a native ServerPlayer instance.
     *
     * @param player The native ServerPlayer object.
     * @return The cached or newly constructed LuxPlayer instance.
     */
    fun login(player: net.minecraft.server.level.ServerPlayer): LuxPlayer {
        val luxPlayer = NeoForgeLuxPlayer(player)
        cachedPlayers[player.uuid] = luxPlayer
        return luxPlayer
    }

    /**
     * Schedules a delayed eviction of a player from the registry to allow metadata systems to safely persist.
     *
     * @param uuid The unique identifier of the disconnecting player.
     */
    fun logout(uuid: UUID) {
        scope.launch {
            delay(2000)
            cachedPlayers.remove(uuid)
        }
    }

    /**
     * Synchronizes a player's underlying native session, typically during respawns.
     *
     * @param player The updated native ServerPlayer object.
     */
    fun updatePlayerInstance(player: net.minecraft.server.level.ServerPlayer) {
        val cached = cachedPlayers[player.uuid] as? NeoForgeLuxPlayer
        cached?.updateInstance(player)
    }

    override fun getPlayer(name: String): LuxPlayer? {
        val active = cachedPlayers.values.firstOrNull { it.name.equals(name, ignoreCase = true) }
        if (active != null) return active

        val nativePlayer = server.playerList.getPlayerByName(name) ?: return null
        return login(nativePlayer)
    }

    override fun getPlayer(uuid: UUID): LuxPlayer? {
        val active = cachedPlayers[uuid]
        if (active != null) return active

        val nativePlayer = server.playerList.getPlayer(uuid) ?: return null
        return login(nativePlayer)
    }

    override fun getOnlinePlayers(): List<LuxPlayer> {
        return server.playerList.players.map { getPlayer(it.uuid) ?: NeoForgeLuxPlayer(it) }
    }
}