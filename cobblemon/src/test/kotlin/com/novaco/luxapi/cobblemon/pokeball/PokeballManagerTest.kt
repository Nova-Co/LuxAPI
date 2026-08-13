package com.novaco.luxapi.cobblemon.pokeball

import com.cobblemon.mod.common.api.pokeball.catching.CaptureContext
import com.cobblemon.mod.common.api.pokeball.catching.calculators.CaptureCalculator
import com.cobblemon.mod.common.entity.pokeball.EmptyPokeBallEntity
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
import net.minecraft.world.entity.LivingEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class PokeballManagerTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun bootstrapMinecraft() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    @Test
    fun `get resolves a built-in pokeball by name`() {
        assertEquals("cobblemon:great_ball", PokeballManager.get("great_ball")?.name.toString())
    }

    @Test
    fun `get returns null for an unknown pokeball name`() {
        assertNull(PokeballManager.get("not_a_real_lux_ball"))
    }

    @Test
    fun `all includes built-in balls`() {
        assertTrue(PokeballManager.all().any { it.name.toString() == "cobblemon:ultra_ball" })
    }

    @Test
    fun `registerCaptureCalculator adds a calculator that getCaptureCalculator can then resolve`() {
        val calculator = object : CaptureCalculator {
            override fun id() = "lux_test_calculator"
            override fun processCapture(
                thrower: LivingEntity,
                pokeBallEntity: EmptyPokeBallEntity,
                target: PokemonEntity
            ): CaptureContext = throw UnsupportedOperationException("not used in this test")
        }

        PokeballManager.registerCaptureCalculator(calculator)

        assertEquals(calculator, PokeballManager.getCaptureCalculator("lux_test_calculator"))
    }

    @Test
    fun `getCaptureCalculator returns null for an unknown id`() {
        assertNull(PokeballManager.getCaptureCalculator("not_a_real_lux_calculator"))
    }
}
