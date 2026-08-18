package com.novaco.luxapi.commons.config.type

import com.novaco.luxapi.commons.time.TimeFormatUtils
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * A config-serializable choice of duration formatting style, delegating the actual formatting
 * to [TimeFormatUtils] rather than reimplementing it.
 */
@ConfigSerializable
class TimeFormatConfig {

    enum class Style { SHORT, LONG, DIGITAL }

    var style: Style = Style.SHORT

    fun format(millis: Long): String = when (style) {
        Style.SHORT -> TimeFormatUtils.formatShort(millis)
        Style.LONG -> TimeFormatUtils.formatLong(millis)
        Style.DIGITAL -> TimeFormatUtils.formatDigital(millis)
    }

    fun format(duration: Duration): String = format(duration.toMillis())

    fun format(amount: Long, unit: TimeUnit): String = format(unit.toMillis(amount))

    companion object {
        fun of(style: Style): TimeFormatConfig = TimeFormatConfig().apply { this.style = style }
    }
}
