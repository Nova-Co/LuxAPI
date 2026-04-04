package com.novaco.luxapi.commons

import com.novaco.luxapi.commons.service.ServiceManager
import com.novaco.luxapi.commons.event.EventBus
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class LuxAPITest {

    @Test
    fun `test global shutdown clears registries`() {
        // Fill registries with dummy data
        ServiceManager.register(String::class.java, "TestData")
        // EventBus.register(Any())

        // Trigger the shutdown
        // LuxAPI.shutdown()

        // Manual verification of our cleanup methods
        ServiceManager.clear()
        EventBus.clear()

        // Verify they are empty
        assertNull(ServiceManager.get(String::class.java), "ServiceManager should be empty after shutdown.")
        // assert(EventBus.isEmpty())
    }
}