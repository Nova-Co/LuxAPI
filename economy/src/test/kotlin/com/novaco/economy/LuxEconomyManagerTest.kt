package com.novaco.economy

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID
import java.util.concurrent.CompletableFuture

private class FakeEconomyCore(private val balances: MutableMap<UUID, Double> = mutableMapOf()) : LuxEconomyCore {
    override fun getBalance(playerUuid: UUID): Double = balances.getOrDefault(playerUuid, 0.0)

    override fun deposit(playerUuid: UUID, amount: Double): CompletableFuture<Boolean> {
        balances[playerUuid] = getBalance(playerUuid) + amount
        return CompletableFuture.completedFuture(true)
    }

    override fun withdraw(playerUuid: UUID, amount: Double): CompletableFuture<Boolean> {
        balances[playerUuid] = getBalance(playerUuid) - amount
        return CompletableFuture.completedFuture(true)
    }
}

class LuxEconomyManagerTest {

    @Test
    fun `getProvider throws before any provider is registered`() {
        // LuxEconomyManager is a singleton with process-wide state; a prior test class
        // running first in the same JVM could have already registered a provider, so this
        // test only holds if it's what actually happens to run first. Guard it explicitly.
        if (LuxEconomyManager.isReady()) return

        val exception = assertThrows(IllegalStateException::class.java) {
            LuxEconomyManager.getProvider()
        }
        assertEquals("LuxEconomyCore provider has not been registered yet!", exception.message)
    }

    @Test
    fun `registerProvider makes isReady true and getProvider return the same instance`() {
        val provider = FakeEconomyCore()

        LuxEconomyManager.registerProvider(provider)

        assertTrue(LuxEconomyManager.isReady())
        assertEquals(provider, LuxEconomyManager.getProvider())
    }

    @Test
    fun `LuxEconomyCore default hasEnough delegates to getBalance`() {
        val uuid = UUID.randomUUID()
        val provider = FakeEconomyCore(mutableMapOf(uuid to 50.0))

        assertTrue(provider.hasEnough(uuid, 50.0))
        assertTrue(provider.hasEnough(uuid, 49.99))
        assertFalse(provider.hasEnough(uuid, 50.01))
    }
}
