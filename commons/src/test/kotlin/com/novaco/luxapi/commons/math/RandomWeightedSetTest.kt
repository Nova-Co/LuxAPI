package com.novaco.luxapi.commons.math

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class RandomWeightedSetTest {

    @Test
    fun `test weight accumulation and random retrieval`() {
        val lootSet = RandomWeightedSet<String>()

        lootSet.add("Common Sword", 70.0)
        lootSet.add("Rare Shield", 25.0)
        lootSet.add("Legendary Bow", 5.0)

        // The total weight should be exactly the sum of all inputs
        assertEquals(100.0, lootSet.totalWeight, "Total weight should calculate correctly")

        // Retrieve a random item
        val droppedItem = lootSet.getRandom()

        assertNotNull(droppedItem, "Random item should never be null if the set is not empty")
        assertTrue(
            droppedItem in listOf("Common Sword", "Rare Shield", "Legendary Bow"),
            "Random item must be one of the registered elements"
        )
    }

    @Test
    fun `test empty set behavior`() {
        val emptySet = RandomWeightedSet<String>()

        assertNull(emptySet.getRandom(), "Empty set should safely return null")
        assertEquals(0.0, emptySet.totalWeight, "Empty set should have zero weight")
    }
}