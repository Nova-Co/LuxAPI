package com.novaco.luxapi.cobblemon.pasture

import com.cobblemon.mod.common.block.entity.PokemonPastureBlockEntity
import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.SharedConstants
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.Bootstrap
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class PastureManagerTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun bootstrapMinecraft() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    private fun tethering(playerId: UUID, pokemonId: UUID): PokemonPastureBlockEntity.Tethering {
        val pos = BlockPos.ZERO
        return PokemonPastureBlockEntity.Tethering(
            minRoamPos = pos,
            maxRoamPos = pos,
            playerId = playerId,
            playerName = "Ash",
            tetheringId = UUID.randomUUID(),
            pokemonId = pokemonId,
            pcId = UUID.randomUUID(),
            entityId = 1,
            pasturePos = pos
        )
    }

    @Test
    fun `getPastureAt resolves a pasture block entity at the position`() {
        val pasture = mock<PokemonPastureBlockEntity>()
        val world = mock<ServerLevel>()
        val pos = BlockPos(1, 2, 3)
        whenever(world.getBlockEntity(pos)).thenReturn(pasture)

        assertEquals(pasture, PastureManager.getPastureAt(world, pos))
    }

    @Test
    fun `getPastureAt returns null when the block entity isn't a pasture`() {
        val world = mock<ServerLevel>()
        val pos = BlockPos(1, 2, 3)
        whenever(world.getBlockEntity(pos)).thenReturn(null)

        assertNull(PastureManager.getPastureAt(world, pos))
    }

    @Test
    fun `tetherPokemon delegates to the pasture's tether method`() {
        val pasture = mock<PokemonPastureBlockEntity>()
        val player = mock<ServerPlayer>()
        val pokemon = mock<Pokemon>()
        whenever(pasture.tether(player, pokemon, Direction.NORTH)).thenReturn(true)

        assertTrue(PastureManager.tetherPokemon(pasture, player, pokemon, Direction.NORTH))
    }

    @Test
    fun `releasePokemon returns true and releases when the pokemon was tethered`() {
        val pasture = mock<PokemonPastureBlockEntity>()
        val pokemonId = UUID.randomUUID()
        whenever(pasture.tetheredPokemon).thenReturn(mutableListOf(tethering(UUID.randomUUID(), pokemonId)))

        val result = PastureManager.releasePokemon(pasture, pokemonId)

        assertTrue(result)
        verify(pasture).releasePokemon(pokemonId)
    }

    @Test
    fun `releasePokemon returns false when the pokemon was not tethered`() {
        val pasture = mock<PokemonPastureBlockEntity>()
        whenever(pasture.tetheredPokemon).thenReturn(mutableListOf())

        val result = PastureManager.releasePokemon(pasture, UUID.randomUUID())

        assertFalse(result)
        verify(pasture).releasePokemon(any())
    }

    @Test
    fun `releaseAllPokemon delegates and returns released ids`() {
        val pasture = mock<PokemonPastureBlockEntity>()
        val playerId = UUID.randomUUID()
        val released = listOf(UUID.randomUUID(), UUID.randomUUID())
        whenever(pasture.releaseAllPokemon(playerId)).thenReturn(released)

        assertEquals(released, PastureManager.releaseAllPokemon(pasture, playerId))
    }

    @Test
    fun `getTetheredPokemon maps every tethering to a PastureTethering`() {
        val pasture = mock<PokemonPastureBlockEntity>()
        val playerId = UUID.randomUUID()
        val pokemonId = UUID.randomUUID()
        val entry = tethering(playerId, pokemonId)
        whenever(pasture.tetheredPokemon).thenReturn(mutableListOf(entry))

        val result = PastureManager.getTetheredPokemon(pasture)

        assertEquals(1, result.size)
        assertEquals(pokemonId, result[0].pokemonId)
        assertEquals(playerId, result[0].ownerId)
        assertEquals("Ash", result[0].ownerName)
        assertEquals(entry.pcId, result[0].pcId)
        assertEquals(1, result[0].entityId)
    }

    @Test
    fun `getTetheredPokemon filtered by player only returns that player's tetherings`() {
        val pasture = mock<PokemonPastureBlockEntity>()
        val playerA = UUID.randomUUID()
        val playerB = UUID.randomUUID()
        whenever(pasture.tetheredPokemon).thenReturn(
            mutableListOf(tethering(playerA, UUID.randomUUID()), tethering(playerB, UUID.randomUUID()))
        )

        val result = PastureManager.getTetheredPokemon(pasture, playerA)

        assertEquals(1, result.size)
        assertEquals(playerA, result[0].ownerId)
    }
}
