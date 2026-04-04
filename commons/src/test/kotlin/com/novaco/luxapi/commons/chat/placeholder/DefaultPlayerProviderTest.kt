package com.novaco.luxapi.commons.chat.placeholder

import com.novaco.luxapi.commons.math.Vector3D
import com.novaco.luxapi.commons.player.LuxPlayer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

/**
 * A dummy LuxPlayer implementation to provide mock data.
 */
class DummyPlaceholderPlayer(override val uniqueId: UUID, override val name: String) : LuxPlayer {
    override val parent: Any = Any()
    override val locale: String = "en_us"
    override val position: Vector3D = Vector3D(0.0, 0.0, 0.0)

    override fun sendMessage(message: String) {}
    override fun hasPermission(permission: String): Boolean = true
    override fun isPlayer(): Boolean = true
    override fun sendTitle(title: String, subtitle: String) {}
    override fun kick(reason: String) {}
}

class DefaultPlayerProviderTest {

    private lateinit var provider: DefaultPlayerProvider
    private lateinit var dummyPlayer: DummyPlaceholderPlayer

    @BeforeEach
    fun setup() {
        provider = DefaultPlayerProvider()
        dummyPlayer = DummyPlaceholderPlayer(UUID.randomUUID(), "NovacoAdmin")
    }

    @Test
    fun `test valid player placeholder resolutions`() {
        assertEquals("player", provider.identifier(), "Identifier must strictly be 'player'.")

        val nameResult = provider.onPlaceholderRequest(dummyPlayer, "name")
        assertEquals("NovacoAdmin", nameResult, "Should correctly resolve %player_name%.")

        val uuidResult = provider.onPlaceholderRequest(dummyPlayer, "uuid")
        assertEquals(dummyPlayer.uniqueId.toString(), uuidResult, "Should correctly resolve %player_uuid%.")
    }

    @Test
    fun `test invalid parameter returns null`() {
        val invalidResult = provider.onPlaceholderRequest(dummyPlayer, "unknown_stat")
        assertNull(invalidResult, "Unknown parameters should return null to remain unmodified.")
    }

    @Test
    fun `test null player context defaults to Guest`() {
        // Simulating the console or a system broadcast evaluating a placeholder
        val consoleResult = provider.onPlaceholderRequest(null, "name")
        assertEquals("Guest", consoleResult, "Null players should gracefully default to 'Guest'.")
    }
}