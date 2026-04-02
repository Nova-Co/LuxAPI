package com.novaco.luxapi.commons.event.player

import com.novaco.luxapi.commons.event.LuxEvent
import com.novaco.luxapi.commons.player.LuxPlayer

/**
 * Triggered when a player sends a chat message.
 * Supports cancellation and modification of the message or format.
 */
class PlayerChatEvent(
    val player: LuxPlayer,
    var message: String,
    var format: String = "<%player_name%> %message%",
    val recipients: MutableSet<LuxPlayer>
) : LuxEvent {

    var isCancelled: Boolean = false

    /**
     * Convenience method to update the message after placeholder replacement.
     */
    fun getRenderedMessage(): String {
        return format.replace("%message%", message)
    }
}