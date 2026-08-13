package com.novaco.luxapi.commons.command.sender

import java.util.UUID

/**
 * Represents the entity executing the command.
 * This can be a Player, Console, or Command Block.
 */
interface CommandSender {

    /**
     * The name of the sender (e.g., Player's username or "Server").
     */
    val name: String

    /**
     * The UUID of the sender.
     * Will be null if the sender is the Console.
     */
    val uniqueId: UUID?

    /**
     * Sends a simple text message to this sender.
     *
     * @param message The message to send.
     */
    fun sendMessage(message: String)

    /**
     * Checks if the sender has a specific permission node.
     *
     * Implementations MUST fail closed: if a real answer cannot be resolved
     * (no permission backend wired, an internal error, an unimplemented
     * platform path), return false. Never hardcode or default to true —
     * that turns every permission-gated command into an open door. See the
     * CRITICAL section of TODO.md for the incident this guards against.
     *
     * @param permission The permission node (e.g., "luxapi.command.admin").
     * @return true if the sender has permission, false otherwise.
     */
    fun hasPermission(permission: String): Boolean

    /**
     * Checks if this sender is a player.
     */
    fun isPlayer(): Boolean = uniqueId != null
}