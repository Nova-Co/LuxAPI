package com.novaco.luxapi.commons.gui

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Simulating the complex math required for Paginated GUIs.
 */
class MockPaginatedGui(
    private val allItems: List<String>,
    private val itemsPerPage: Int
) {
    fun getTotalPages(): Int {
        if (allItems.isEmpty()) return 1
        return Math.ceil(allItems.size.toDouble() / itemsPerPage).toInt()
    }

    fun getItemsOnPage(page: Int): List<String> {
        val totalPages = getTotalPages()
        if (page < 1 || page > totalPages) return emptyList()

        val startIndex = (page - 1) * itemsPerPage
        val endIndex = minOf(startIndex + itemsPerPage, allItems.size)
        return allItems.subList(startIndex, endIndex)
    }
}

class PaginatedGuiBuilderTest {

    @Test
    fun `test paginated gui page chunking and bounds`() {
        // Create 25 mock items. If we have 10 items per page, we expect 3 pages.
        // Page 1: 10 items | Page 2: 10 items | Page 3: 5 items
        val mockData = List(25) { "Item_$it" }
        val paginatedGui = MockPaginatedGui(mockData, 10)

        // Test Total Pages
        assertEquals(3, paginatedGui.getTotalPages(), "25 items at 10 per page should result in exactly 3 pages.")

        // Test Page 1 Extraction
        val page1 = paginatedGui.getItemsOnPage(1)
        assertEquals(10, page1.size, "Page 1 should be full (10 items).")
        assertEquals("Item_0", page1.first())

        // Test Page 3 Extraction (The remainder page)
        val page3 = paginatedGui.getItemsOnPage(3)
        assertEquals(5, page3.size, "Page 3 should only contain the remaining 5 items.")
        assertEquals("Item_24", page3.last())

        // Test Out of Bounds Page
        assertTrue(paginatedGui.getItemsOnPage(4).isEmpty(), "Requesting page 4 should safely return an empty list.")
    }

    @Test
    fun `test empty dataset gracefully defaults to one empty page`() {
        val paginatedGui = MockPaginatedGui(emptyList(), 21)

        assertEquals(1, paginatedGui.getTotalPages(), "An empty dataset should still generate 1 blank page for the UI.")
        assertTrue(paginatedGui.getItemsOnPage(1).isEmpty(), "Page 1 should be empty.")
    }
}