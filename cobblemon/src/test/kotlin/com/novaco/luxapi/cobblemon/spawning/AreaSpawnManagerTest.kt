package com.novaco.luxapi.cobblemon.spawning

import com.novaco.luxapi.commons.math.Cuboid
import com.novaco.luxapi.commons.math.Vector3D
import net.minecraft.world.level.Level
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AreaSpawnManagerTest {

    private fun area(dimension: net.minecraft.resources.ResourceKey<Level> = Level.OVERWORLD) = SpawnArea(
        dimension = dimension,
        region = Cuboid(Vector3D(0.0, 0.0, 0.0), Vector3D(10.0, 10.0, 10.0)),
        weightMultiplier = 2.0F,
        bannedSpecies = setOf("pikachu")
    )

    @Test
    fun `registerArea then getAreas returns it for the matching dimension`() {
        val a = area()

        val id = AreaSpawnManager.registerArea(a)

        assertEquals(a.id, id)
        assertTrue(AreaSpawnManager.getAreas(Level.OVERWORLD).any { it.id == id })
    }

    @Test
    fun `getAreas does not return areas registered for a different dimension`() {
        val a = area(dimension = Level.NETHER)
        AreaSpawnManager.registerArea(a)

        val result = AreaSpawnManager.getAreas(Level.OVERWORLD)

        assertTrue(result.none { it.id == a.id })
    }

    @Test
    fun `unregisterArea removes it from future getAreas calls`() {
        val a = area()
        AreaSpawnManager.registerArea(a)

        AreaSpawnManager.unregisterArea(a.id)

        assertTrue(AreaSpawnManager.getAreas(Level.OVERWORLD).none { it.id == a.id })
    }

    @Test
    fun `unregisterArea on an unknown id is a silent no-op`() {
        AreaSpawnManager.unregisterArea(java.util.UUID.randomUUID())
    }
}
