package com.novaco.luxapi.commons.math

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class Vector3DTest {

    @Test
    fun `test vector initialization`() {
        val vector = Vector3D(10.5, 20.0, -5.5)

        assertEquals(10.5, vector.x, "X coordinate should match")
        assertEquals(20.0, vector.y, "Y coordinate should match")
        assertEquals(-5.5, vector.z, "Z coordinate should match")
    }

    @Test
    fun `test vector distance calculation`() {
        val v1 = Vector3D(0.0, 0.0, 0.0)
        val v2 = Vector3D(3.0, 4.0, 0.0)

        val distance = v1.distance(v2)
        assertEquals(5.0, distance, "Distance should be exactly 5.0")

        val distanceSq = v1.distanceSquared(v2)
        assertEquals(25.0, distanceSq, "Squared distance should be exactly 25.0")
    }

    @Test
    fun `test vector addition`() {
        val v1 = Vector3D(1.0, 2.0, 3.0)
        val v2 = Vector3D(4.0, 5.0, 6.0)

        val result = v1.add(v2)

        assertEquals(5.0, result.x)
        assertEquals(7.0, result.y)
        assertEquals(9.0, result.z)
    }
}