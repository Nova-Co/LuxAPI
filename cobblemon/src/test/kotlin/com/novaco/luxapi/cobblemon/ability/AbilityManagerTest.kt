package com.novaco.luxapi.cobblemon.ability

import com.cobblemon.mod.common.api.abilities.Abilities
import com.cobblemon.mod.common.api.abilities.Ability
import com.cobblemon.mod.common.api.abilities.AbilityTemplate
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class AbilityManagerTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun bootstrapMinecraft() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    @Test
    fun `register adds a new ability template that get can then resolve`() {
        val template = AbilityManager.register("lux_test_ability_register")

        assertEquals(template, AbilityManager.get("lux_test_ability_register"))
        assertTrue(AbilityManager.all().contains(template))
    }

    @Test
    fun `get returns null for an unknown ability name`() {
        assertNull(AbilityManager.get("not_a_real_lux_ability"))
    }

    @Test
    fun `setAbility assigns a known ability and reports success`() {
        val template = Abilities.register(AbilityTemplate("lux_test_ability_set"))
        val pokemon = mock<Pokemon>()
        val assigned = template.create(forced = true)
        whenever(pokemon.updateAbility(any())).thenReturn(assigned)

        val result = AbilityManager.setAbility(pokemon, "lux_test_ability_set", forced = true)

        assertTrue(result)
        verify(pokemon).updateAbility(any<Ability>())
    }

    @Test
    fun `setAbility rejects an unknown ability name without touching the Pokemon`() {
        val pokemon = mock<Pokemon>()

        val result = AbilityManager.setAbility(pokemon, "not_a_real_lux_ability")

        assertFalse(result)
        verify(pokemon, never()).updateAbility(any())
    }
}
