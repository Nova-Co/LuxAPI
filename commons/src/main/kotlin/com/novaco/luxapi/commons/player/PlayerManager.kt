package com.novaco.luxapi.commons.player

import java.util.UUID

/**
 * Responsible for managing and retrieving online players.
 */
interface PlayerManager {

    /**
     * Attempts to find an online player by their exact username.
     * * @param name The username of the player.
     * @return The [LuxPlayer] instance, or null if the player is offline or doesn't exist.
     */
    fun getPlayer(name: String): LuxPlayer?

    /**
     * Attempts to find an online player by their UUID.
     */
    fun getPlayer(uuid: UUID): LuxPlayer?

    /**
     * Returns a list of all currently online players.
     */
    fun getOnlinePlayers(): List<LuxPlayer>
}