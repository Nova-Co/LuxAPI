package com.novaco.luxapi.cobblemon.mark

import com.cobblemon.mod.common.api.mark.Mark
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.SharedConstants
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.Bootstrap
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class MarkManagerTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun bootstrapMinecraft() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }

        private fun testMark(name: String = "lux_test_mark") = Mark(
            ResourceLocation.fromNamespaceAndPath("lux", name),
            name,
            "$name.desc",
            null,
            null,
            ResourceLocation.fromNamespaceAndPath("lux", "textures/mark/$name.png"),
            null,
            null,
            0F,
            null
        )
    }

    // Marks is a JSON-loaded registry with no public register(), so get() is exercised
    // against its empty unit-test state; giveMark/removeMark/setActiveMark are
    // exercised against a directly-constructed Mark, matching MoveManagerTest's
    // registry-free fallback pattern.

    @Test
    fun `get returns null for an unknown identifier in an unpopulated registry`() {
        assertNull(MarkManager.get(ResourceLocation.fromNamespaceAndPath("lux", "not_a_real_mark")))
    }

    @Test
    fun `giveMark reports success once the Pokemon holds the mark`() {
        val pokemon = mock<Pokemon>()
        val marks = mutableSetOf<Mark>()
        val mark = testMark()
        whenever(pokemon.marks).thenReturn(marks)
        doAnswer { marks.add(mark) }.whenever(pokemon).exchangeMark(mark, true)

        val result = MarkManager.giveMark(pokemon, mark)

        assertTrue(result)
        assertTrue(marks.contains(mark))
    }

    @Test
    fun `removeMark reports success once the Pokemon no longer holds the mark`() {
        val pokemon = mock<Pokemon>()
        val mark = testMark()
        val marks = mutableSetOf(mark)
        whenever(pokemon.marks).thenReturn(marks)
        doAnswer { marks.remove(mark) }.whenever(pokemon).exchangeMark(mark, false)

        val result = MarkManager.removeMark(pokemon, mark)

        assertTrue(result)
        assertFalse(marks.contains(mark))
    }

    @Test
    fun `setActiveMark assigns the given mark`() {
        val pokemon = mock<Pokemon>()
        val mark = testMark()

        MarkManager.setActiveMark(pokemon, mark)

        verify(pokemon).activeMark = mark
    }
}
