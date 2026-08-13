package com.novaco.luxapi.bukkit.player

import com.novaco.luxapi.commons.player.LuxPlayer
import com.novaco.luxapi.commons.player.PlayerManager
import org.bukkit.Bukkit
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Bukkit-specific implementation of [PlayerManager].
 * Caches [BukkitLuxPlayer] wrappers per online session, keyed by UUID.
 */
class BukkitPlayerManager : PlayerManager {

    private val cachedPlayers = ConcurrentHashMap<UUID, LuxPlayer>()

    /**
     * Wraps and caches a native [org.bukkit.entity.Player] on login.
     */
    fun login(player: org.bukkit.entity.Player): LuxPlayer {
        val luxPlayer = BukkitLuxPlayer(player)
        cachedPlayers[player.uniqueId] = luxPlayer
        return luxPlayer
    }

    /**
     * Evicts a player from the cache on disconnect.
     */
    fun logout(uuid: UUID) {
        cachedPlayers.remove(uuid)
    }

    override fun getPlayer(name: String): LuxPlayer? {
        val active = cachedPlayers.values.firstOrNull { it.name.equals(name, ignoreCase = true) }
        if (active != null) return active

        val nativePlayer = Bukkit.getPlayerExact(name) ?: return null
        return login(nativePlayer)
    }

    override fun getPlayer(uuid: UUID): LuxPlayer? {
        val active = cachedPlayers[uuid]
        if (active != null) return active

        val nativePlayer = Bukkit.getPlayer(uuid) ?: return null
        return login(nativePlayer)
    }

    override fun getOnlinePlayers(): List<LuxPlayer> {
        return Bukkit.getOnlinePlayers().map { getPlayer(it.uniqueId) ?: login(it) }
    }
}
