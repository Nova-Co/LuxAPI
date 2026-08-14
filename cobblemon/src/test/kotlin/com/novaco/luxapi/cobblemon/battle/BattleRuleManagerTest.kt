package com.novaco.luxapi.cobblemon.battle

import com.cobblemon.mod.common.api.battles.model.PokemonBattle
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor
import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.battles.BattleStartedEvent
import com.cobblemon.mod.common.battles.ActiveBattlePokemon
import com.cobblemon.mod.common.battles.BattleSide
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.Species
import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class BattleRuleManagerTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun bootstrapMinecraft() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
            // register() is idempotent (guarded by isRegistered) — safe to call once here
            // for every test in this class.
            BattleRuleManager.register()
        }
    }

    @AfterEach
    fun resetRules() {
        // No bulk-clear exists on BattleRuleManager (by design — every field already has
        // a public mutator), so undo exactly what each test configured.
        BattleRuleManager.setLevelCap(null, null)
        BattleRuleManager.setCustomRule { null }
    }

    private fun battleEventWithPokemon(vararg pokemon: Pokemon): BattleStartedEvent.Pre {
        val battlePokemon = pokemon.map { BattlePokemon(it) }
        val actor = mock<BattleActor>()
        // ActiveBattlePokemon's constructor eagerly reads actor.battle — must be fully
        // constructed *before* stubbing actor.activePokemon, otherwise that mock
        // interaction happens mid-stub and Mockito throws UnfinishedStubbingException.
        val activePokemon = battlePokemon.map { ActiveBattlePokemon(actor, it) }.toMutableList()
        whenever(actor.activePokemon).thenReturn(activePokemon)

        val side1 = BattleSide(actor)
        val emptyActor = mock<BattleActor>()
        whenever(emptyActor.activePokemon).thenReturn(mutableListOf())
        val side2 = BattleSide(emptyActor)

        val battle = mock<PokemonBattle>()
        whenever(battle.side1).thenReturn(side1)
        whenever(battle.side2).thenReturn(side2)
        whenever(battle.actors).thenReturn(emptyList())

        return BattleStartedEvent.Pre(battle)
    }

    private fun pokemonOfSpecies(name: String, level: Int = 5): Pokemon {
        val species = mock<Species>()
        whenever(species.name).thenReturn(name)
        val pokemon = mock<Pokemon>()
        whenever(pokemon.species).thenReturn(species)
        whenever(pokemon.level).thenReturn(level)
        return pokemon
    }

    @Test
    fun `banned species rejects the battle and sets a reason`() {
        BattleRuleManager.banSpecies("mewtwo")
        val event = battleEventWithPokemon(pokemonOfSpecies("Mewtwo"))

        CobblemonEvents.BATTLE_STARTED_PRE.post(event)

        assertTrue(event.isCanceled)
        assertTrue(event.reason?.string?.contains("banned") == true)

        BattleRuleManager.unbanSpecies("mewtwo")
    }

    @Test
    fun `unbanned species is not rejected by the ban list`() {
        BattleRuleManager.banSpecies("mewtwo")
        BattleRuleManager.unbanSpecies("mewtwo")
        val event = battleEventWithPokemon(pokemonOfSpecies("Mewtwo"))

        CobblemonEvents.BATTLE_STARTED_PRE.post(event)

        assertFalse(event.isCanceled)
    }

    @Test
    fun `pokemon below the minimum level cap is rejected`() {
        BattleRuleManager.setLevelCap(min = 10)
        val event = battleEventWithPokemon(pokemonOfSpecies("Pidgey", level = 5))

        CobblemonEvents.BATTLE_STARTED_PRE.post(event)

        assertTrue(event.isCanceled)
    }

    @Test
    fun `pokemon above the maximum level cap is rejected`() {
        BattleRuleManager.setLevelCap(max = 50)
        val event = battleEventWithPokemon(pokemonOfSpecies("Pidgey", level = 80))

        CobblemonEvents.BATTLE_STARTED_PRE.post(event)

        assertTrue(event.isCanceled)
    }

    @Test
    fun `pokemon within the level cap is allowed`() {
        BattleRuleManager.setLevelCap(min = 1, max = 100)
        val event = battleEventWithPokemon(pokemonOfSpecies("Pidgey", level = 50))

        CobblemonEvents.BATTLE_STARTED_PRE.post(event)

        assertFalse(event.isCanceled)
    }

    @Test
    fun `custom rule rejection message is used as the cancel reason`() {
        BattleRuleManager.setCustomRule { "No battles allowed right now." }
        val event = battleEventWithPokemon(pokemonOfSpecies("Pidgey"))

        CobblemonEvents.BATTLE_STARTED_PRE.post(event)

        assertTrue(event.isCanceled)
        assertTrue(event.reason?.string == "No battles allowed right now.")
    }

    @Test
    fun `custom rule returning null allows the battle`() {
        BattleRuleManager.setCustomRule { null }
        val event = battleEventWithPokemon(pokemonOfSpecies("Pidgey"))

        CobblemonEvents.BATTLE_STARTED_PRE.post(event)

        assertFalse(event.isCanceled)
        assertNull(event.reason)
    }
}
