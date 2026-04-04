package com.novaco.luxapi.commons.math

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CuboidTest {

    @Test
    fun `test point containment inside cuboid`() {
        val minPoint = Vector3D(0.0, 0.0, 0.0)
        val maxPoint = Vector3D(10.0, 10.0, 10.0)
        val cuboid = Cuboid(minPoint, maxPoint)

        val insidePoint = Vector3D(5.0, 5.0, 5.0)
        val edgePoint = Vector3D(10.0, 0.0, 10.0)
        val outsidePoint = Vector3D(15.0, 5.0, 5.0)

        assertTrue(cuboid.contains(insidePoint), "Point should be inside the cuboid boundaries")
        assertTrue(cuboid.contains(edgePoint), "Point exactly on the edge should be considered inside")
        assertFalse(cuboid.contains(outsidePoint), "Point outside the coordinates should return false")
    }

    @Test
    fun `test cuboid volume calculation`() {
        val minPoint = Vector3D(0.0, 0.0, 0.0)
        val maxPoint = Vector3D(10.0, 5.0, 2.0) // 10 * 5 * 2 = 100
        val cuboid = Cuboid(minPoint, maxPoint)

        assertEquals(100.0, cuboid.getVolume(), "Volume should be length * width * height")
    }
}