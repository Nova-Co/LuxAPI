package com.novaco.luxapi.commons.text

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NumberFormatUtilsTest {

    @Test
    fun `test comma formatting for large numbers`() {
        assertEquals("1,000", NumberFormatUtils.formatWithCommas(1000))
        assertEquals("1,000,000", NumberFormatUtils.formatWithCommas(1000000))
        assertEquals("999", NumberFormatUtils.formatWithCommas(999))
    }

    @Test
    fun `test compact formatting`() {
        assertEquals("1K", NumberFormatUtils.formatWithSuffix(1000))
        assertEquals("1.5K", NumberFormatUtils.formatWithSuffix(1500))
        assertEquals("2.5M", NumberFormatUtils.formatWithSuffix(2500000))
    }
}