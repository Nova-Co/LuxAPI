package com.novaco.luxapi.commons.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

interface IDummyService
class DummyServiceImpl : IDummyService

class ServiceManagerTest {

    @BeforeEach
    fun setup() {
        ServiceManager.clear()
    }

    @Test
    fun `test successful service registration and retrieval`() {
        val myService = DummyServiceImpl()

        ServiceManager.register(IDummyService::class.java, myService)

        val retrieved = ServiceManager.get(IDummyService::class.java)

        assertNotNull(retrieved, "Retrieved service should not be null")
        assertEquals(myService, retrieved, "The retrieved instance must be exactly the same object")
    }

    @Test
    fun `test retrieval of unregistered service returns null`() {
        val missingService = ServiceManager.get(String::class.java)

        assertNull(missingService, "Unregistered service should return null safely")
    }
}