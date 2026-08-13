package com.novaco.luxapi.cobblemon.move

import com.cobblemon.mod.common.api.moves.MoveSet
import com.cobblemon.mod.common.api.moves.MoveTemplate
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class MoveManagerTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun bootstrapMinecraft() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    // Moves' own registry is populated only via Cobblemon's Showdown resource-loading
    // pipeline (not available in a unit test), and Moves.register is private, so the
    // by-name lookups below are exercised against an empty registry (see the
    // not-found tests) while the by-template overloads are exercised with a
    // MoveTemplate constructed directly, matching MoveManager's own registry-free
    // fallback design.

    @Test
    fun `get returns null for an unknown move name in an unpopulated registry`() {
        assertNull(MoveManager.get("not_a_real_lux_move"))
    }

    @Test
    fun `learnMove by template places the move in the moveset`() {
        val pokemon = mock<Pokemon>()
        val moveSet = MoveSet()
        whenever(pokemon.moveSet).thenReturn(moveSet)
        val template = MoveTemplate.dummy("luxtestmove")

        val result = MoveManager.learnMove(pokemon, template)

        assertTrue(result)
        assertTrue(moveSet.getMoves().any { it.template == template })
    }

    @Test
    fun `learnMove by template rejects a duplicate move`() {
        val pokemon = mock<Pokemon>()
        val moveSet = MoveSet()
        whenever(pokemon.moveSet).thenReturn(moveSet)
        val template = MoveTemplate.dummy("luxtestmove")
        moveSet.add(template.create())

        val result = MoveManager.learnMove(pokemon, template)

        assertFalse(result)
    }

    @Test
    fun `learnMove by unknown name is a no-op false`() {
        val pokemon = mock<Pokemon>()

        val result = MoveManager.learnMove(pokemon, "not_a_real_lux_move")

        assertFalse(result)
    }

    @Test
    fun `unlearnMove by template removes it from the moveset`() {
        val pokemon = mock<Pokemon>()
        val moveSet = MoveSet()
        val template = MoveTemplate.dummy("luxtestmove")
        moveSet.add(template.create())
        whenever(pokemon.moveSet).thenReturn(moveSet)
        whenever(pokemon.unlearnMove(template)).then {
            moveSet.setMove(0, null)
            Unit
        }

        val result = MoveManager.unlearnMove(pokemon, template)

        assertTrue(result)
        assertTrue(moveSet.getMoves().isEmpty())
    }

    @Test
    fun `setMove by template rejects an out-of-range slot`() {
        val pokemon = mock<Pokemon>()
        val template = MoveTemplate.dummy("luxtestmove")

        val result = MoveManager.setMove(pokemon, 4, template)

        assertFalse(result)
    }

    @Test
    fun `setMove by template places the move at the given slot`() {
        val pokemon = mock<Pokemon>()
        val moveSet = MoveSet()
        whenever(pokemon.moveSet).thenReturn(moveSet)
        val template = MoveTemplate.dummy("luxtestmove")

        val result = MoveManager.setMove(pokemon, 1, template)

        assertTrue(result)
        assertEquals(template, moveSet[1]?.template)
    }
}
