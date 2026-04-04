package com.novaco.luxapi.database.attribute

import com.novaco.luxapi.commons.event.player.PlayerJoinEvent
import com.novaco.luxapi.commons.event.player.PlayerQuitEvent
import com.novaco.luxapi.commons.math.Vector3D
import com.novaco.luxapi.commons.player.LuxPlayer
import com.novaco.luxapi.commons.service.ServiceManager
import com.novaco.luxapi.database.service.DatabaseService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

/**
 * Dummy player for simulating network joins and quits.
 */
class DummyDatabasePlayer(override val uniqueId: UUID) : LuxPlayer {
    override val name: String = "DbTester"
    override val parent: Any = Any()
    override val locale: String = "en_us"
    override val position: Vector3D = Vector3D(0.0, 0.0, 0.0)
    override fun sendMessage(message: String) {}
    override fun hasPermission(permission: String): Boolean = true
    override fun isPlayer(): Boolean = true
    override fun sendTitle(title: String, subtitle: String) {}
    override fun kick(reason: String) {}
}

class AttributeManagerTest {

    private lateinit var targetPlayer: DummyDatabasePlayer

    @BeforeEach
    fun setup() {
        // Prevent null pointers during attribute initialization
        ServiceManager.register(DatabaseService::class.java, MockDatabaseService())

        targetPlayer = DummyDatabasePlayer(UUID.randomUUID())
        AttributeManager.registerAttribute(DummyPersistentAttribute::class.java)
    }

    @AfterEach
    fun teardown() {
        ServiceManager.clear()
        // Ensure cleanup between tests so concurrent maps don't leak
        AttributeManager.onPlayerQuit(PlayerQuitEvent(targetPlayer))
    }

    @Test
    fun `test player join event initializes and loads attributes`() {
        // Simulate Player joining
        val joinEvent = PlayerJoinEvent(targetPlayer)
        AttributeManager.onPlayerJoin(joinEvent)

        // Verify retrieval via direct call
        val retrieved = AttributeManager.getAttribute(targetPlayer, DummyPersistentAttribute::class.java)
        assertNotNull(retrieved, "Attribute should be initialized and stored upon joining.")
        assertEquals(targetPlayer.uniqueId, retrieved?.uuid, "Attribute UUID must match the player.")

        // Verify retrieval via extension function
        val extensionRetrieved = targetPlayer.getAttribute<DummyPersistentAttribute>()
        assertNotNull(extensionRetrieved, "Extension function should correctly map to the manager.")
    }

    @Test
    fun `test player quit event clears attributes from memory`() {
        // Setup state
        AttributeManager.onPlayerJoin(PlayerJoinEvent(targetPlayer))
        assertNotNull(AttributeManager.getAttribute(targetPlayer, DummyPersistentAttribute::class.java))

        // Simulate Player quitting
        val quitEvent = PlayerQuitEvent(targetPlayer)
        AttributeManager.onPlayerQuit(quitEvent)

        // Verify it was purged from the active map
        val retrievedAfterQuit = AttributeManager.getAttribute(targetPlayer, DummyPersistentAttribute::class.java)
        assertNull(retrievedAfterQuit, "Attribute should be removed from memory to prevent leaks when a player quits.")
    }
}