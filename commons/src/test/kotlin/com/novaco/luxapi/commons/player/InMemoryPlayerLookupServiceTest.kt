package com.novaco.luxapi.commons.player

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class InMemoryPlayerLookupServiceTest {

    private lateinit var service: InMemoryPlayerLookupService

    @BeforeEach
    fun setup() {
        service = InMemoryPlayerLookupService()
    }

    @Test
    fun `test unknown name resolves to null`() {
        assertNull(service.resolveUuid("Nobody"))
    }

    @Test
    fun `test unknown uuid resolves to null`() {
        assertNull(service.resolveName(UUID.randomUUID()))
    }

    @Test
    fun `test recorded pair resolves both directions`() {
        val uuid = UUID.randomUUID()
        service.record(uuid, "NovacoAdmin")

        assertEquals(uuid, service.resolveUuid("NovacoAdmin"))
        assertEquals("NovacoAdmin", service.resolveName(uuid))
    }

    @Test
    fun `test name resolution is case-insensitive`() {
        val uuid = UUID.randomUUID()
        service.record(uuid, "NovacoAdmin")

        assertEquals(uuid, service.resolveUuid("novacoadmin"))
        assertEquals(uuid, service.resolveUuid("NOVACOADMIN"))
    }

    @Test
    fun `test re-recording a name updates the mapped uuid`() {
        val firstUuid = UUID.randomUUID()
        val secondUuid = UUID.randomUUID()

        service.record(firstUuid, "SharedName")
        service.record(secondUuid, "SharedName")

        assertEquals(secondUuid, service.resolveUuid("SharedName"))
    }
}
