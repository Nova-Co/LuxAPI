package com.novaco.luxapi.cobblemon.battle

import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.battles.ActiveBattlePokemon
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.pokemon.Pokemon
import com.novaco.luxapi.commons.LuxAPI
import com.novaco.luxapi.commons.scheduler.LuxScheduler
import com.novaco.luxapi.commons.scheduler.LuxTask
import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.UUID

private class FakeLuxTask(override val id: Int = 1, override val isAsync: Boolean = false) : LuxTask {
    var cancelled = false
        private set
    override val isCancelled: Boolean get() = cancelled
    override fun cancel() { cancelled = true }
}

private class CapturingScheduler : LuxScheduler {
    var repeatingAction: (() -> Unit)? = null
    val task = FakeLuxTask()

    override fun run(runnable: Runnable): LuxTask = throw NotImplementedError()
    override fun runAsync(runnable: Runnable): LuxTask = throw NotImplementedError()
    override fun runLater(delay: Long, runnable: Runnable): LuxTask = throw NotImplementedError()
    override fun runLaterAsync(delay: Long, runnable: Runnable): LuxTask = throw NotImplementedError()
    override fun runRepeating(delay: Long, period: Long, runnable: Runnable): LuxTask {
        repeatingAction = { runnable.run() }
        return task
    }
    override fun runRepeatingAsync(delay: Long, period: Long, runnable: Runnable): LuxTask = throw NotImplementedError()
    override fun cancelAll() {}

    fun tick() = repeatingAction?.invoke()
}

class BattleInterceptorTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun bootstrapMinecraft() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    private lateinit var scheduler: CapturingScheduler

    @BeforeEach
    fun installFakeScheduler() {
        scheduler = CapturingScheduler()
        LuxAPI.schedulerProvider = { scheduler }
    }

    @AfterEach
    fun restoreScheduler() {
        LuxAPI.schedulerProvider = {
            throw IllegalStateException("LuxAPI Scheduler Provider has not been initialized!")
        }
    }

    private fun pokemonWithHealth(current: Int, max: Int): Pokemon {
        val pokemon = mock<Pokemon>()
        whenever(pokemon.uuid).thenReturn(UUID.randomUUID())
        whenever(pokemon.currentHealth).thenReturn(current)
        whenever(pokemon.maxHealth).thenReturn(max)
        return pokemon
    }

    private fun battleWith(vararg activePokemon: ActiveBattlePokemon): PokemonBattle {
        val battle = mock<PokemonBattle>()
        whenever(battle.battleId).thenReturn(UUID.randomUUID())
        whenever(battle.activePokemon).thenReturn(activePokemon.toList())
        return battle
    }

    @Test
    fun `action fires once health ratio drops to or below the threshold`() {
        val pokemon = pokemonWithHealth(current = 100, max = 100)
        val battlePokemon = BattlePokemon(pokemon)
        val battle = battleWith(ActiveBattlePokemon(mock(), battlePokemon))
        var fireCount = 0

        BattleInterceptor.onHealthThreshold(battle, battlePokemon, 0.5F) { _, _ -> fireCount++ }

        whenever(pokemon.currentHealth).thenReturn(100)
        scheduler.tick()
        assertEquals(0, fireCount, "Should not fire above the threshold.")

        whenever(pokemon.currentHealth).thenReturn(40)
        scheduler.tick()
        assertEquals(1, fireCount, "Should fire once the ratio drops to/below 0.5.")

        whenever(pokemon.currentHealth).thenReturn(10)
        scheduler.tick()
        assertEquals(1, fireCount, "Must not re-fire after already triggering once.")

        BattleInterceptor.unregister(battle)
    }

    @Test
    fun `unregister cancels the poll task`() {
        val pokemon = pokemonWithHealth(current = 100, max = 100)
        val battlePokemon = BattlePokemon(pokemon)
        val battle = battleWith(ActiveBattlePokemon(mock(), battlePokemon))

        BattleInterceptor.onHealthThreshold(battle, battlePokemon, 0.5F) { _, _ -> }
        BattleInterceptor.unregister(battle)

        assertTrue(scheduler.task.isCancelled)
    }

    @Test
    fun `stopBattle calls stop on the underlying battle`() {
        val battle = mock<PokemonBattle>()

        BattleInterceptor.stopBattle(battle)

        org.mockito.kotlin.verify(battle).stop()
    }
}
