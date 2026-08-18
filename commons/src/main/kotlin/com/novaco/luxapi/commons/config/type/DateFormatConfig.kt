package com.novaco.luxapi.commons.config.type

import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date

/**
 * A config-serializable date/time format pattern. Builds a fresh [SimpleDateFormat] per call
 * instead of caching one — [pattern] can be overwritten in place by [com.novaco.luxapi.commons.config.LuxConfig.reload],
 * so a cached formatter would silently go stale after a reload.
 */
@ConfigSerializable
class DateFormatConfig {

    var pattern: String = "dd/MM/yyyy HH:mm:ss"

    fun format(instant: Instant): String = format(Date.from(instant))

    fun format(date: Date): String = SimpleDateFormat(pattern).format(date)

    fun format(epochMillis: Long): String = format(Date(epochMillis))

    companion object {
        fun of(pattern: String): DateFormatConfig = DateFormatConfig().apply { this.pattern = pattern }
    }
}
