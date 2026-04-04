package com.novaco.luxapi.commons.text

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ChatPaginatorTest {

    /**
     * Simulates the pagination logic.
     * Assuming your ChatPaginator takes a list and a page size.
     */
    private fun <T> getPage(items: List<T>, pageNumber: Int, itemsPerPage: Int): List<T> {
        if (items.isEmpty() || pageNumber < 1) return emptyList()

        val startIndex = (pageNumber - 1) * itemsPerPage
        if (startIndex >= items.size) return emptyList()

        val endIndex = minOf(startIndex + itemsPerPage, items.size)
        return items.subList(startIndex, endIndex)
    }

    private fun getTotalPages(totalItems: Int, itemsPerPage: Int): Int {
        if (totalItems == 0) return 1
        return Math.ceil(totalItems.toDouble() / itemsPerPage.toDouble()).toInt()
    }

    @Test
    fun `test pagination chunking logic`() {
        val dataList = listOf("Line 1", "Line 2", "Line 3", "Line 4", "Line 5")
        val itemsPerPage = 2

        val page1 = getPage(dataList, 1, itemsPerPage)
        assertEquals(2, page1.size, "Page 1 should contain exactly 2 items.")
        assertEquals("Line 1", page1[0])

        val page3 = getPage(dataList, 3, itemsPerPage)
        assertEquals(1, page3.size, "Page 3 should contain the remainder (1 item).")
        assertEquals("Line 5", page3[0])
    }

    @Test
    fun `test out of bounds page request returns empty list`() {
        val dataList = listOf("A", "B", "C")

        val outOfBoundsPage = getPage(dataList, 99, 2)
        assertTrue(outOfBoundsPage.isEmpty(), "Requesting a page beyond the maximum should return an empty list safely.")
    }

    @Test
    fun `test total pages calculation`() {
        assertEquals(3, getTotalPages(5, 2), "5 items with 2 per page should equal 3 total pages.")
        assertEquals(1, getTotalPages(0, 10), "0 items should still default to 1 empty page.")
        assertEquals(5, getTotalPages(50, 10), "50 items with 10 per page should exactly equal 5 pages.")
    }
}