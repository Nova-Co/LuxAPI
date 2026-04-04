package com.novaco.luxapi.database

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test

class LuxDatabaseTest {

    @Test
    fun `test database module initializes without exception`() {
        // Should securely register the manager to the EventBus without throwing errors
        assertDoesNotThrow {
            LuxDatabase.init()
        }
    }
}