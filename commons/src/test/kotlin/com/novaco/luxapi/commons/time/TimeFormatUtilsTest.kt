package com.novaco.luxapi.commons.time

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TimeFormatUtilsTest {

    @Test
    fun `test zero or negative time returns ready format`() {
        assertEquals("Ready", TimeFormatUtils.formatDuration(0L, null))
        assertEquals("Ready", TimeFormatUtils.formatDuration(-5000L, null))
    }

    @Test
    fun `test exact time formatting`() {
        // 1 Second = 1000ms
        assertEquals("1s", TimeFormatUtils.formatDuration(1000L, null))

        // 1 Minute, 5 Seconds = 65000ms
        assertEquals("1m 5s", TimeFormatUtils.formatDuration(65000L, null))

        // 1 Hour = 3600000ms
        assertEquals("1h", TimeFormatUtils.formatDuration(3600000L, null))

        // 1 Day, 2 Hours, 30 Minutes = (86400 + 7200 + 1800) * 1000
        val complexTime = (86400L + 7200L + 1800L) * 1000L
        assertEquals("1d 2h 30m", TimeFormatUtils.formatDuration(complexTime, null))
    }

    @Test
    fun `test time formatting without trailing units`() {
        // 2 Minutes exactly (should not show 0s)
        assertEquals("2m", TimeFormatUtils.formatDuration(120000L, null))
    }
}