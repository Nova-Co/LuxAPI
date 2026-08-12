package com.novaco.luxapi.cobblemon.spawning

import com.cobblemon.mod.common.api.spawning.detail.PokemonSpawnDetail
import com.cobblemon.mod.common.api.spawning.detail.SpawnDetail
import com.cobblemon.mod.common.api.spawning.position.SpawnablePosition
import com.novaco.luxapi.commons.math.Cuboid
import com.novaco.luxapi.commons.math.Vector3D
import net.minecraft.SharedConstants
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceKey
import net.minecraft.server.Bootstrap
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.UUID

class LuxAreaSpawningInfluenceTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun bootstrapMinecraft() {
            // Mocking net.minecraft.server.level.ServerLevel triggers its class init,
            // which touches IntProvider -> BuiltInRegistries and needs Minecraft's
            // Bootstrap to have run first, same as Phase 6's PokemonPropertyManagerTest.
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    private val registeredIds = mutableListOf<UUID>()

    @AfterEach
    fun cleanupAreas() {
        registeredIds.forEach { AreaSpawnManager.unregisterArea(it) }
        registeredIds.clear()
    }

    private fun register(
        dimension: ResourceKey<Level> = Level.OVERWORLD,
        weightMultiplier: Float = 1.0F,
        bannedSpecies: Set<String> = emptySet()
    ) {
        val id = AreaSpawnManager.registerArea(
            SpawnArea(
                dimension = dimension,
                region = Cuboid(Vector3D(0.0, 0.0, 0.0), Vector3D(10.0, 10.0, 10.0)),
                weightMultiplier = weightMultiplier,
                bannedSpecies = bannedSpecies
            )
        )
        registeredIds.add(id)
    }

    private fun spawnablePositionAt(x: Int, y: Int, z: Int, dimension: ResourceKey<Level> = Level.OVERWORLD): SpawnablePosition {
        val world = mock<ServerLevel>()
        whenever(world.dimension()).thenReturn(dimension)
        val spawnablePosition = mock<SpawnablePosition>()
        whenever(spawnablePosition.world).thenReturn(world)
        whenever(spawnablePosition.position).thenReturn(BlockPos(x, y, z))
        return spawnablePosition
    }

    private fun pokemonDetail(species: String): PokemonSpawnDetail {
        val detail = PokemonSpawnDetail()
        detail.pokemon.species = species
        return detail
    }

    @Test
    fun `affectWeight multiplies weight when inside a matching area`() {
        register(weightMultiplier = 2.0F)
        val spawnablePosition = spawnablePositionAt(5, 5, 5)
        val detail = pokemonDetail("bulbasaur")

        val result = LuxAreaSpawningInfluence.affectWeight(detail, spawnablePosition, 10.0F)

        assertEquals(20.0F, result)
    }

    @Test
    fun `affectWeight leaves weight unchanged outside any area`() {
        register(weightMultiplier = 2.0F)
        val spawnablePosition = spawnablePositionAt(50, 50, 50)
        val detail = pokemonDetail("bulbasaur")

        val result = LuxAreaSpawningInfluence.affectWeight(detail, spawnablePosition, 10.0F)

        assertEquals(10.0F, result)
    }

    @Test
    fun `affectWeight leaves weight unchanged in a different dimension`() {
        register(dimension = Level.NETHER, weightMultiplier = 2.0F)
        val spawnablePosition = spawnablePositionAt(5, 5, 5, dimension = Level.OVERWORLD)
        val detail = pokemonDetail("bulbasaur")

        val result = LuxAreaSpawningInfluence.affectWeight(detail, spawnablePosition, 10.0F)

        assertEquals(10.0F, result)
    }

    @Test
    fun `affectWeight stacks multipliers for overlapping areas`() {
        register(weightMultiplier = 2.0F)
        register(weightMultiplier = 3.0F)
        val spawnablePosition = spawnablePositionAt(5, 5, 5)
        val detail = pokemonDetail("bulbasaur")

        val result = LuxAreaSpawningInfluence.affectWeight(detail, spawnablePosition, 10.0F)

        assertEquals(60.0F, result)
    }

    @Test
    fun `affectSpawnable rejects a banned species case-insensitively`() {
        register(bannedSpecies = setOf("pikachu"))
        val spawnablePosition = spawnablePositionAt(5, 5, 5)
        val detail = pokemonDetail("PIKACHU")

        val result = LuxAreaSpawningInfluence.affectSpawnable(detail, spawnablePosition)

        assertFalse(result)
    }

    @Test
    fun `affectSpawnable allows a non-banned species`() {
        register(bannedSpecies = setOf("pikachu"))
        val spawnablePosition = spawnablePositionAt(5, 5, 5)
        val detail = pokemonDetail("bulbasaur")

        val result = LuxAreaSpawningInfluence.affectSpawnable(detail, spawnablePosition)

        assertTrue(result)
    }

    @Test
    fun `affectSpawnable never rejects non-PokemonSpawnDetail details`() {
        register(bannedSpecies = setOf("pikachu"))
        val spawnablePosition = spawnablePositionAt(5, 5, 5)
        val detail = mock<SpawnDetail>()

        val result = LuxAreaSpawningInfluence.affectSpawnable(detail, spawnablePosition)

        assertTrue(result)
    }
}
