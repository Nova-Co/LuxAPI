package com.novaco.luxapi.commons.player

import com.novaco.luxapi.commons.command.sender.CommandSender
import com.novaco.luxapi.commons.i18n.LanguageManager
import com.novaco.luxapi.commons.math.Vector3D
import com.novaco.luxapi.commons.metadata.PlayerMetadataManager
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
     * The client language code of the player (e.g., "en_us", "th_th").
     * Supplied by the platform-specific implementation.
     */
    val locale: String

    /**
     * The player's current position in the world.
     */
    val position: Vector3D

    /**
     * Sends a title and subtitle to the player's screen.
     */
    fun sendTitle(title: String, subtitle: String)

    /**
     * Disconnects the player from the server with a specific reason.
     */
    fun kick(reason: String)

    /**
     * Attaches temporary metadata to this player.
     * The data is automatically garbage collected upon logout.
     */
    fun setMetadata(key: String, value: Any) {
        PlayerMetadataManager.getContainer(this.uniqueId).set(key, value)
    }

    /**
     * Checks if this player has specific metadata.
     */
    fun hasMetadata(key: String): Boolean {
        return PlayerMetadataManager.getContainer(this.uniqueId).has(key)
    }

    /**
     * Removes specific metadata from this player.
     */
    fun removeMetadata(key: String) {
        PlayerMetadataManager.getContainer(this.uniqueId).remove(key)
    }

    /**
     * Retrieves metadata for this player, cast to a specific class.
     */
    fun <T> getMetadata(key: String, clazz: Class<T>): T? {
        return PlayerMetadataManager.getContainer(this.uniqueId).get(key, clazz)
    }
}

/**
 * Reified extension for cleaner metadata retrieval.
 * Usage: val status = player.getMeta<String>("status")
 */
inline fun <reified T : Any> LuxPlayer.getMeta(key: String): T? {
    return getMetadata(key, T::class.java)
}

/**
 * Extension function to send a localized message directly to the player.
 * * @param key The message key from the language JSON file.
 * @param params Optional local variables to inject into the message.
 */
fun LuxPlayer.sendTranslated(key: String, params: Map<String, String> = emptyMap()) {
    val translatedText = LanguageManager.getTranslation(this, key, params)
    this.sendMessage(translatedText)
}