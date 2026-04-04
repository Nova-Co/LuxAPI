package com.novaco.luxapi.commons.event.player

import com.novaco.luxapi.commons.math.Vector3D
import com.novaco.luxapi.commons.player.LuxPlayer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.UUID

/**
 * A dummy player strictly for testing the chat event.
 */
class DummyChatPlayer(override val name: String) : LuxPlayer {
    override val uniqueId: UUID = UUID.randomUUID()
    override val parent: Any = Any()
    override val locale: String = "en_us"
    override val position: Vector3D = Vector3D(0.0, 0.0, 0.0)

    override fun sendMessage(message: String) {}
    override fun hasPermission(permission: String): Boolean = true
    override fun isPlayer(): Boolean = true
    override fun sendTitle(title: String, subtitle: String) {}
    override fun kick(reason: String) {}
}

class PlayerChatEventTest {

    @Test
    fun `test message rendering replaces message placeholder`() {
        val player = DummyChatPlayer("Novaco")
        val rawMessage = "Hello server!"
        val format = "<%player_name%> %message%"

        val event = PlayerChatEvent(
            player = player,
            message = rawMessage,
            format = format,
            recipients = mutableSetOf()
        )

        val rendered = event.getRenderedMessage()

        // Note: The %player_name% placeholder is handled separately by PlaceholderManager.
        // The getRenderedMessage() specifically targets %message%.
        assertEquals("<%player_name%> Hello server!", rendered, "The %message% tag should be correctly replaced by the raw message.")
    }

    @Test
    fun `test event cancellation state`() {
        val event = PlayerChatEvent(DummyChatPlayer("Tester"), "Test", "<%player_name%> %message%", mutableSetOf())

        assertFalse(event.isCancelled, "Chat events should default to not cancelled.")

        event.isCancelled = true
        assertTrue(event.isCancelled, "Chat event cancellation state should be mutable.")
    }
}