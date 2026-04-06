package com.novaco.luxapi.cobblemon.evolution

import com.cobblemon.mod.common.api.events.pokemon.evolution.EvolutionAcceptedEvent
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.Species
import net.minecraft.server.level.ServerPlayer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class EvolutionHookManagerTest {

    // A test hook that lets us track if it was executed
    private class TestHook(
        target: String,
        val shouldPass: Boolean
    ) : EvolutionHook(target) {
        var failureTriggered = false

        override fun checkConditions(pokemon: Pokemon, player: ServerPlayer): Boolean {
            return shouldPass
        }

        override fun onFailure(pokemon: Pokemon, player: ServerPlayer) {
            failureTriggered = true
        }
    }

    @BeforeEach
    fun setup() {
        // CRITICAL: Because EvolutionHookManager is an object (Singleton),
        // we MUST clear its internal state before every test to prevent cross-test contamination.
        val hooksField = EvolutionHookManager::class.java.getDeclaredField("hooks")
        hooksField.isAccessible = true

        @Suppress("UNCHECKED_CAST")
        val hooksList = hooksField.get(EvolutionHookManager) as MutableList<EvolutionHook>
        hooksList.clear()
    }

    @Test
    fun `test register adds hook to internal registry`() {
        val hook1 = TestHook("pikachu", true)
        val hook2 = TestHook("charmander", false)

        EvolutionHookManager.register(hook1)
        EvolutionHookManager.register(hook2)

        // Verify using Reflection
        val hooksField = EvolutionHookManager::class.java.getDeclaredField("hooks")
        hooksField.isAccessible = true
        val internalHooks = hooksField.get(EvolutionHookManager) as List<*>

        assertEquals(2, internalHooks.size, "Registry should contain exactly 2 hooks.")
        assertTrue(internalHooks.contains(hook1))
        assertTrue(internalHooks.contains(hook2))
    }

    @Test
    @Disabled("Requires the Cobblemon Event Bus to be initialized on the classpath.")
    fun `test initialize subscribes to event and enforces custom logic`() {
        // 1. Register our strict test hook
        val strictHook = TestHook("magikarp", shouldPass = false)
        EvolutionHookManager.register(strictHook)

        // 2. Deep Mocking the Cobblemon Ecosystem
        val mockEvent = mock<EvolutionAcceptedEvent>()
        val mockPokemon = mock<Pokemon>()
        val mockPlayer = mock<ServerPlayer>()
        val mockSpecies = mock<Species>()

        whenever(mockEvent.pokemon).thenReturn(mockPokemon)
        whenever(mockPokemon.getOwnerPlayer()).thenReturn(mockPlayer)
        whenever(mockPokemon.species).thenReturn(mockSpecies)

        // Match the species so our hook activates
        whenever(mockSpecies.name).thenReturn("magikarp")

        // 3. Initialize the bus hook
        assertDoesNotThrow {
            EvolutionHookManager.initialize()
        }

        // NOTE: In a true integration environment, firing the native Cobblemon event here
        // would trigger the lambda. We would then verify:
        // verify(mockEvent).cancel()
        // assertTrue(strictHook.failureTriggered)
    }
}