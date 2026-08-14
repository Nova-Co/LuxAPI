package com.novaco.luxapi.cobblemon.hooks

import com.cobblemon.mod.common.api.events.battles.BattleVictoryEvent
import com.cobblemon.mod.common.api.events.pokemon.LevelUpEvent
import com.cobblemon.mod.common.api.events.pokemon.evolution.EvolutionEvent
import com.cobblemon.mod.common.api.events.pokemon.HatchEggEvent
import com.cobblemon.mod.common.pokemon.Pokemon
import com.novaco.luxapi.commons.player.LuxPlayer
import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class HookManagerTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun bootstrapMinecraft() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    private fun <T> hook(onTrigger: (LuxPlayer, T) -> Unit): Hook<T> = object : Hook<T> {
        override fun onTrigger(player: LuxPlayer, data: T) = onTrigger(player, data)
    }

    @Test
    fun `broadcastCatch fires only registered catch hooks with the right args`() {
        val player = mock<LuxPlayer>()
        val pokemon = mock<Pokemon>()
        var received: Pair<LuxPlayer, Pokemon>? = null

        HookManager.registerCatchHook(hook { p, data -> received = p to data })

        HookManager.broadcastCatch(player, pokemon)

        assertEquals(player, received?.first)
        assertEquals(pokemon, received?.second)
    }

    @Test
    fun `broadcastCatch does not fire defeat hooks`() {
        val player = mock<LuxPlayer>()
        val pokemon = mock<Pokemon>()
        var defeatFired = false

        HookManager.registerDefeatHook(hook { _, _ -> defeatFired = true })
        HookManager.broadcastCatch(player, pokemon)

        assertFalse(defeatFired)
    }

    @Test
    fun `broadcastDefeat fires registered defeat hooks`() {
        val player = mock<LuxPlayer>()
        val event = mock<BattleVictoryEvent>()
        var received: BattleVictoryEvent? = null

        HookManager.registerDefeatHook(hook { _, data -> received = data })
        HookManager.broadcastDefeat(player, event)

        assertEquals(event, received)
    }

    @Test
    fun `broadcastLevelUp fires registered level-up hooks`() {
        val player = mock<LuxPlayer>()
        val event = mock<LevelUpEvent>()
        var received: LevelUpEvent? = null

        HookManager.registerLevelUpHook(hook { _, data -> received = data })
        HookManager.broadcastLevelUp(player, event)

        assertEquals(event, received)
    }

    @Test
    fun `broadcastEvolution fires registered evolution hooks`() {
        val player = mock<LuxPlayer>()
        val event = mock<EvolutionEvent>()
        var received: EvolutionEvent? = null

        HookManager.registerEvolutionHook(hook { _, data -> received = data })
        HookManager.broadcastEvolution(player, event)

        assertEquals(event, received)
    }

    @Test
    fun `broadcastEggHatch fires registered egg-hatch hooks`() {
        val player = mock<LuxPlayer>()
        val event = mock<HatchEggEvent>()
        var received: HatchEggEvent? = null

        HookManager.registerEggHatchHook(hook { _, data -> received = data })
        HookManager.broadcastEggHatch(player, event)

        assertEquals(event, received)
    }

    @Test
    fun `multiple hooks of the same kind all fire`() {
        val player = mock<LuxPlayer>()
        val pokemon = mock<Pokemon>()
        var firstFired = false
        var secondFired = false

        HookManager.registerCatchHook(hook { _, _ -> firstFired = true })
        HookManager.registerCatchHook(hook { _, _ -> secondFired = true })
        HookManager.broadcastCatch(player, pokemon)

        assertTrue(firstFired)
        assertTrue(secondFired)
    }
}
