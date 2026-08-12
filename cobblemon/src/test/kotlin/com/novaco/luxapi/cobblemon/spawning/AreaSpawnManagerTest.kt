package com.novaco.luxapi.cobblemon.spawning

import com.novaco.luxapi.commons.math.Cuboid
import com.novaco.luxapi.commons.math.Vector3D
import net.minecraft.world.level.Level
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class AreaSpawnManagerTest {

    // AreaSpawnManager is a shared singleton across the whole test JVM — leftover
    // areas here would otherwise pollute LuxAreaSpawningInfluenceTest's assertions
    // when the full suite runs together.
    private val registeredIds = mutableListOf<UUID>()

    @AfterEach
    fun cleanupAreas() {
        registeredIds.forEach { AreaSpawnManager.unregisterArea(it) }
        registeredIds.clear()
    }

    private fun area(dimension: net.minecraft.resources.ResourceKey<Level> = Level.OVERWORLD) = SpawnArea(
        dimension = dimension,
        region = Cuboid(Vector3D(0.0, 0.0, 0.0), Vector3D(10.0, 10.0, 10.0)),
        weightMultiplier = 2.0F,
        bannedSpecies = setOf("pikachu")
    )

    private fun registerAndTrack(a: SpawnArea): UUID {
        val id = AreaSpawnManager.registerArea(a)
        registeredIds.add(id)
        return id
    }

    @Test
    fun `registerArea then getAreas returns it for the matching dimension`() {
        val a = area()

        val id = registerAndTrack(a)

        assertEquals(a.id, id)
        assertTrue(AreaSpawnManager.getAreas(Level.OVERWORLD).any { it.id == id })
    }

    @Test
    fun `getAreas does not return areas registered for a different dimension`() {
        val a = area(dimension = Level.NETHER)
        registerAndTrack(a)

        val result = AreaSpawnManager.getAreas(Level.OVERWORLD)

        assertTrue(result.none { it.id == a.id })
    }

    @Test
    fun `unregisterArea removes it from future getAreas calls`() {
        val a = area()
        registerAndTrack(a)

        AreaSpawnManager.unregisterArea(a.id)

        assertTrue(AreaSpawnManager.getAreas(Level.OVERWORLD).none { it.id == a.id })
    }

    @Test
    fun `unregisterArea on an unknown id is a silent no-op`() {
        AreaSpawnManager.unregisterArea(java.util.UUID.randomUUID())
    }
}
