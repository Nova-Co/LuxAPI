package com.novaco.luxapi.commons.command.injector.impl

import com.novaco.luxapi.commons.command.exception.CommandParseException
import com.novaco.luxapi.commons.player.InMemoryPlayerLookupService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class OfflinePlayerInjectorTest {

    private lateinit var playerManager: MockPlayerManager
    private lateinit var lookupService: InMemoryPlayerLookupService
    private lateinit var injector: OfflinePlayerInjector
    private lateinit var dummySender: DummyCommandSender

    @BeforeEach
    fun setup() {
        playerManager = MockPlayerManager()
        lookupService = InMemoryPlayerLookupService()
        injector = OfflinePlayerInjector(playerManager, lookupService)
        dummySender = DummyCommandSender()
    }

    @Test
    fun `test resolves an online player`() {
        val targetUuid = UUID.randomUUID()
        playerManager.addPlayer(DummyCommandPlayer(targetUuid, "NovacoAdmin"))

        val injected = injector.instantiate(dummySender, arrayOf("NovacoAdmin"), 0)

        assertEquals(targetUuid, injected.uniqueId)
        assertEquals("NovacoAdmin", injected.name)
    }

    @Test
    fun `test online resolution records the pair in the lookup service`() {
        val targetUuid = UUID.randomUUID()
        playerManager.addPlayer(DummyCommandPlayer(targetUuid, "NovacoAdmin"))

        injector.instantiate(dummySender, arrayOf("NovacoAdmin"), 0)

        assertEquals(targetUuid, lookupService.resolveUuid("NovacoAdmin"))
    }

    @Test
    fun `test resolves a previously-seen offline player from the lookup service`() {
        val targetUuid = UUID.randomUUID()
        lookupService.record(targetUuid, "OfflineFriend")

        val injected = injector.instantiate(dummySender, arrayOf("OfflineFriend"), 0)

        assertEquals(targetUuid, injected.uniqueId)
        assertEquals("OfflineFriend", injected.name)
    }

    @Test
    fun `test fails for a player never seen before`() {
        val exception = assertThrows(CommandParseException::class.java) {
            injector.instantiate(dummySender, arrayOf("NeverJoined"), 0)
        }

        assertTrue(exception.message!!.contains("never joined"), "Should throw specific error for unknown players.")
    }

    @Test
    fun `test fails when argument is missing`() {
        assertThrows(CommandParseException::class.java) {
            injector.instantiate(dummySender, emptyArray(), 0)
        }
    }

    @Test
    fun `test suggestions only cover online players, not lookup-service-only entries`() {
        playerManager.addPlayer(DummyCommandPlayer(UUID.randomUUID(), "NovacoAdmin"))
        lookupService.record(UUID.randomUUID(), "NovacoOffline")

        val suggestions = injector.getSuggestions(dummySender, arrayOf("nov"), 0)

        assertEquals(listOf("NovacoAdmin"), suggestions)
    }
}
