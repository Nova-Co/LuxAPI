package com.novaco.luxapi.commons.economy

import com.novaco.luxapi.commons.math.Vector3D
import com.novaco.luxapi.commons.player.LuxPlayer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

/**
 * A dummy implementation of LuxPlayer used exclusively to pass into the EconomyService methods.
 * It fulfills all the abstract properties and methods required by the LuxPlayer interface.
 */
class DummyEconomyPlayer(override val uniqueId: UUID) : LuxPlayer {

    // --- CommandSender Properties & Methods ---
    override val name: String = "TestInvestor"
    override fun sendMessage(message: String) {}
    override fun hasPermission(permission: String): Boolean = true
    override fun isPlayer(): Boolean = true

    // --- LuxPlayer Properties & Methods ---
    override val parent: Any = Any() // Just a dummy object to satisfy the 'Any' requirement
    override val locale: String = "en_us"
    override val position: Vector3D = Vector3D(0.0, 0.0, 0.0)

    override fun sendTitle(title: String, subtitle: String) {
        // Dummy implementation: Do nothing during tests
    }

    override fun kick(reason: String) {
        // Dummy implementation: Do nothing during tests
    }

    // Note: The metadata methods (setMetadata, getMetadata, etc.) do not need to be
    // overridden here because you brilliantly provided default implementations inside the interface!
}

/**
 * A mock implementation of the EconomyService interface for testing purposes.
 */
class MockEconomyService : EconomyService {
    private val balances = mutableMapOf<UUID, Double>()

    override fun getBalance(player: LuxPlayer): Double {
        return balances.getOrDefault(player.uniqueId, 0.0)
    }

    override fun hasEnough(player: LuxPlayer, amount: Double): Boolean {
        return getBalance(player) >= amount
    }

    override fun deposit(uuid: UUID, amount: Double): Boolean {
        if (amount < 0) return false
        balances[uuid] = balances.getOrDefault(uuid, 0.0) + amount
        return true
    }

    override fun withdraw(player: LuxPlayer, amount: Double): Boolean {
        if (amount < 0 || !hasEnough(player, amount)) return false
        balances[player.uniqueId] = getBalance(player) - amount
        return true
    }
}

class EconomyServiceTest {

    private lateinit var economy: EconomyService
    private lateinit var testPlayer: DummyEconomyPlayer

    @BeforeEach
    fun setup() {
        economy = MockEconomyService()
        testPlayer = DummyEconomyPlayer(UUID.randomUUID())
    }

    @Test
    fun `test initial balance is zero`() {
        assertEquals(0.0, economy.getBalance(testPlayer), "Default balance should be 0.0")
    }

    @Test
    fun `test deposit functionality using UUID`() {
        val success = economy.deposit(testPlayer.uniqueId, 500.0)

        assertTrue(success, "Deposit should be successful")
        assertEquals(500.0, economy.getBalance(testPlayer), "Balance should reflect the deposited amount")
    }

    @Test
    fun `test withdraw functionality and bounds checking`() {
        economy.deposit(testPlayer.uniqueId, 1000.0)

        val withdrawSuccess = economy.withdraw(testPlayer, 400.0)
        assertTrue(withdrawSuccess, "Withdrawal should succeed when having sufficient funds")
        assertEquals(600.0, economy.getBalance(testPlayer), "Balance should be deducted correctly")

        val overdrawSuccess = economy.withdraw(testPlayer, 1000.0)
        assertFalse(overdrawSuccess, "Withdrawal should fail when attempting to overdraw")
        assertEquals(600.0, economy.getBalance(testPlayer), "Balance should remain unchanged after a failed withdrawal")
    }

    @Test
    fun `test hasEnough verification`() {
        economy.deposit(testPlayer.uniqueId, 250.0)

        assertTrue(economy.hasEnough(testPlayer, 200.0), "hasEnough should return true if balance is greater than the amount")
        assertTrue(economy.hasEnough(testPlayer, 250.0), "hasEnough should return true if balance is exactly equal to the amount")
        assertFalse(economy.hasEnough(testPlayer, 300.0), "hasEnough should return false if balance is lower than the amount")
    }
}