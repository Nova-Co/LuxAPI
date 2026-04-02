package com.novaco.luxapi.commons.player

import com.novaco.luxapi.commons.command.sender.CommandSender
import java.util.UUID

/**
 * Represents an online player in the server, wrapping the platform-specific player object.
 */
interface LuxPlayer : CommandSender {

    override val uniqueId: UUID

    /**
     * The original platform-specific player object (e.g., ServerPlayer in Fabric/Forge).
     * We use [Any] here because the commons module doesn't know about Minecraft classes.
     */
    val parent: Any

    /**
     * Sends a title and subtitle to the player's screen.
     */
    fun sendTitle(title: String, subtitle: String)

    /**
     * Disconnects the player from the server with a specific reason.
     */
    fun kick(reason: String)
}