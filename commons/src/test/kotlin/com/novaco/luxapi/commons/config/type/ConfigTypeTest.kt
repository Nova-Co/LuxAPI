package com.novaco.luxapi.commons.config.type

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Duration

class ConfigTypeTest {

    @Test
    fun `ConfigLocation builder produces the expected fields and vector conversion`() {
        val location = ConfigLocation.builder()
            .worldName("overworld")
            .x(10.0).y(64.0).z(-5.0)
            .yaw(90f).pitch(0f)
            .build()

        assertEquals("overworld", location.worldName)
        assertEquals(10.0, location.toVector3D().x)
        assertEquals(64.0, location.toVector3D().y)
        assertEquals(-5.0, location.toVector3D().z)
    }

    @Test
    fun `TimeFormatConfig delegates to the matching TimeFormatUtils style`() {
        val short = TimeFormatConfig.of(TimeFormatConfig.Style.SHORT)
        val digital = TimeFormatConfig.of(TimeFormatConfig.Style.DIGITAL)

        assertEquals("1m 30s", short.format(Duration.ofSeconds(90)))
        assertEquals("01:30", digital.format(90_000L))
    }

    @Test
    fun `DateFormatConfig formats using its configured pattern`() {
        val config = DateFormatConfig.of("yyyy-MM-dd")
        val epochMillisForJan1970 = 0L

        assertEquals("1970-01-01", config.format(epochMillisForJan1970))
    }

    @Test
    fun `ProgressBar renders proportional filled and empty characters`() {
        val bar = ProgressBar().apply {
            length = 10
            filledChar = '#'
            emptyChar = '-'
        }

        assertEquals("#####-----", bar.render(0.5))
        assertEquals("##########", bar.render(1.5))
        assertEquals("----------", bar.render(0.0, 0.0))
    }

    @Test
    fun `ConfigRandomWeightedSet reflects entries added after construction`() {
        val set = ConfigRandomWeightedSet<String>()
        assertTrue(set.isEmpty)

        set.add("only-option", 1.0)
        assertEquals("only-option", set.getRandom())
    }
}
