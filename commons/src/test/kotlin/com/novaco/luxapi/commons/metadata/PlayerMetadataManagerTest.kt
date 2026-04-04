package com.novaco.luxapi.commons.metadata

import com.novaco.luxapi.commons.event.player.PlayerQuitEvent
import com.novaco.luxapi.commons.math.Vector3D
import com.novaco.luxapi.commons.player.LuxPlayer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.UUID

/**
 * Dummy player for testing metadata cleanup.
 */
class DummyMetaPlayer(override val uniqueId: UUID) : LuxPlayer {
    override val name: String = "MetaTester"
    override val parent: Any = Any()
    override val locale: String = "en_us"
    override val position: Vector3D = Vector3D(0.0, 0.0, 0.0)
    override fun sendMessage(message: String) {}
    override fun hasPermission(permission: String): Boolean = true
    override fun isPlayer(): Boolean = true
    override fun sendTitle(title: String, subtitle: String) {}
    override fun kick(reason: String) {}
}

class PlayerMetadataManagerTest {

    @Test
    fun `test container creation and isolation`() {
        val uuid1 = UUID.randomUUID()
        val uuid2 = UUID.randomUUID()

        val container1 = PlayerMetadataManager.getContainer(uuid1)
        val container2 = PlayerMetadataManager.getContainer(uuid2)

        container1.set("score", 100)

        // Container 1 should have the data, Container 2 should be empty
        assertEquals(100, container1.get("score", Int::class.javaObjectType))
        assertFalse(container2.has("score"), "Containers for different UUIDs must be strictly isolated.")
    }

    @Test
    fun `test container is destroyed on player quit event`() {
        val player = DummyMetaPlayer(UUID.randomUUID())

        // Create container and add data
        val container = PlayerMetadataManager.getContainer(player.uniqueId)
        container.set("temp_status", "in_combat")

        // Verify data exists
        assertTrue(PlayerMetadataManager.getContainer(player.uniqueId).has("temp_status"))

        // Simulate PlayerQuitEvent
        val quitEvent = PlayerQuitEvent(player)
        PlayerMetadataManager.onPlayerQuit(quitEvent)

        // Calling getContainer again should generate a brand new, empty container
        val newContainer = PlayerMetadataManager.getContainer(player.uniqueId)
        assertFalse(newContainer.has("temp_status"), "The old container should have been deleted to prevent memory leaks.")
    }
}