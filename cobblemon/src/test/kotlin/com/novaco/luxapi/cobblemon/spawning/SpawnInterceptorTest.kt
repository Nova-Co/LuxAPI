package com.novaco.luxapi.cobblemon.spawning

import com.cobblemon.mod.common.api.events.CobblemonEvents
import com.cobblemon.mod.common.api.events.entity.SpawnEvent
import com.cobblemon.mod.common.api.reactive.ObservableSubscription
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.pokemon.Species
import net.minecraft.SharedConstants
import net.minecraft.core.BlockPos
import net.minecraft.server.Bootstrap
import net.minecraft.server.level.ServerLevel
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class SpawnInterceptorTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun bootstrapMinecraft() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    private var subscription: ObservableSubscription<SpawnEvent<PokemonEntity>>? = null

    @AfterEach
    fun unsubscribe() {
        subscription?.unsubscribe()
        subscription = null
    }

    private fun spawnEvent(species: String, x: Int, y: Int, z: Int): SpawnEvent<PokemonEntity> {
        val speciesObj = mock<Species>()
        whenever(speciesObj.name).thenReturn(species)
        val pokemon = mock<Pokemon>()
        whenever(pokemon.species).thenReturn(speciesObj)
        val entity = mock<PokemonEntity>()
        whenever(entity.pokemon).thenReturn(pokemon)

        val world = mock<ServerLevel>()
        val position = mock<SpawnablePosition>()
        whenever(position.world).thenReturn(world)
        whenever(position.position).thenReturn(BlockPos(x, y, z))

        return SpawnEvent(entity, position)
    }

    @Test
    fun `onPokemonSpawn delivers entity, world, position and species`() {
        val event = spawnEvent("Mewtwo", 1, 2, 3)
        val received = mutableListOf<SpawnInterceptEvent>()

        subscription = SpawnInterceptor.onPokemonSpawn { received.add(it) }
        // POKEMON_ENTITY_SPAWN is a TransformObservable piped from ENTITY_SPAWN (filters to
        // PokemonEntity spawns) — TransformObservable has no post() of its own, only the root
        // CancelableObservable does. Posting here is what a real Cobblemon spawn attempt does
        // under the hood.
        CobblemonEvents.ENTITY_SPAWN.post(event)

        assertEquals(1, received.size)
        assertEquals(event.entity, received[0].entity)
        assertEquals(event.spawnablePosition.world, received[0].world)
        assertEquals(BlockPos(1, 2, 3), received[0].position)
        assertEquals("Mewtwo", received[0].species)
    }

    @Test
    fun `cancel on the delivered event cancels the underlying spawn`() {
        val event = spawnEvent("Bulbasaur", 0, 0, 0)

        subscription = SpawnInterceptor.onPokemonSpawn { it.cancel() }
        // POKEMON_ENTITY_SPAWN is a TransformObservable piped from ENTITY_SPAWN (filters to
        // PokemonEntity spawns) — TransformObservable has no post() of its own, only the root
        // CancelableObservable does. Posting here is what a real Cobblemon spawn attempt does
        // under the hood.
        CobblemonEvents.ENTITY_SPAWN.post(event)

        assertTrue(event.isCanceled)
    }
}
