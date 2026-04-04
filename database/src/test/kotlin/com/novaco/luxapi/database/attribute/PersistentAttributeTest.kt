package com.novaco.luxapi.database.attribute

import com.novaco.luxapi.commons.service.ServiceManager
import com.novaco.luxapi.database.service.DatabaseService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * A simple mock DatabaseService to intercept async queries.
 */
class MockDatabaseService : DatabaseService {
    var asyncQueriesExecuted = 0

    override fun getConnection(): Connection {
        throw NotImplementedError("Not needed for this test.")
    }

    override fun executeAsync(query: (Connection) -> Unit): CompletableFuture<Void> {
        asyncQueriesExecuted++
        // Simulate immediate successful execution (bypassing the real connection)
        query(mockConnection())
        return CompletableFuture.completedFuture(null)
    }

    override fun close() {}

    private fun mockConnection(): Connection = org.mockito.Mockito.mock(Connection::class.java)
}

/**
 * A dummy attribute to test the abstract lifecycle.
 */
class DummyPersistentAttribute(uuid: UUID) : PersistentAttribute(uuid) {
    var loadTriggered = false
    var saveTriggered = false

    override fun loadData(service: DatabaseService) {
        loadTriggered = true
    }

    override fun saveData(service: DatabaseService) {
        saveTriggered = true
    }
}

class PersistentAttributeTest {

    private lateinit var mockService: MockDatabaseService
    private lateinit var attribute: DummyPersistentAttribute

    @BeforeEach
    fun setup() {
        mockService = MockDatabaseService()
        // Inject the mock service into the framework so PersistentAttribute can find it
        ServiceManager.register(DatabaseService::class.java, mockService)

        attribute = DummyPersistentAttribute(UUID.randomUUID())
    }

    @AfterEach
    fun teardown() {
        ServiceManager.clear()
    }

    @Test
    fun `test async load sets loaded state to true`() {
        assertFalse(attribute.isLoaded, "Should initially be unloaded.")

        attribute.loadAsync()

        assertTrue(attribute.loadTriggered, "loadData must be triggered.")
        assertTrue(attribute.isLoaded, "isLoaded state must be true after loading.")
        assertEquals(1, mockService.asyncQueriesExecuted, "One async query should be dispatched.")
    }

    @Test
    fun `test save is blocked if data is not loaded`() {
        attribute.saveAsync()

        assertFalse(attribute.saveTriggered, "Save should abort if isLoaded is false.")
        assertEquals(0, mockService.asyncQueriesExecuted, "No query should be dispatched.")
    }

    @Test
    fun `test async save executes if data is loaded`() {
        // Force the loaded state
        attribute.loadAsync()

        attribute.saveAsync()

        assertTrue(attribute.saveTriggered, "Save should trigger if isLoaded is true.")
        assertEquals(2, mockService.asyncQueriesExecuted, "Load and Save both dispatch async queries.")
    }
}