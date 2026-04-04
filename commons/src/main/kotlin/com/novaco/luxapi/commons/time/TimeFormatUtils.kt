package com.novaco.luxapi.commons.time

import com.novaco.luxapi.commons.i18n.LanguageManager
import com.novaco.luxapi.commons.player.LuxPlayer
import java.util.concurrent.TimeUnit

/**
 * A utility class designed to convert raw milliseconds into human-readable time strings.
 * Provides various formatting styles for different UX requirements (e.g., chat vs. scoreboards).
 */
object TimeFormatUtils {

    /**
     * Formats milliseconds into a short, concise string (e.g., "1d 2h 30m 15s").
     * Empty units (like 0 days or 0 hours) are automatically omitted for cleaner output.
     *
     * @param millis The time duration in milliseconds.
     * @return The formatted short time string.
     */
    fun formatShort(millis: Long): String {
        if (millis <= 0) return "0s"

        val days = TimeUnit.MILLISECONDS.toDays(millis)
        val hours = TimeUnit.MILLISECONDS.toHours(millis) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60

        val builder = java.lang.StringBuilder()
        if (days > 0) builder.append("${days}d ")
        if (hours > 0) builder.append("${hours}h ")
        if (minutes > 0) builder.append("${minutes}m ")
        if (seconds > 0 || builder.isEmpty()) builder.append("${seconds}s")

        return builder.toString().trim()
    }

    /**
     * Formats milliseconds into a fully expanded, descriptive string
     * (e.g., "1 day, 2 hours, 30 minutes, 15 seconds").
     *
     * @param millis The time duration in milliseconds.
     * @return The formatted long time string.
     */
    fun formatLong(millis: Long): String {
        if (millis <= 0) return "0 seconds"

        val days = TimeUnit.MILLISECONDS.toDays(millis)
        val hours = TimeUnit.MILLISECONDS.toHours(millis) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60

        val parts = mutableListOf<String>()
        if (days > 0) parts.add("$days day${if (days > 1) "s" else ""}")
        if (hours > 0) parts.add("$hours hour${if (hours > 1) "s" else ""}")
        if (minutes > 0) parts.add("$minutes minute${if (minutes > 1) "s" else ""}")
        if (seconds > 0 || parts.isEmpty()) parts.add("$seconds second${if (seconds > 1) "s" else ""}")

        return parts.joinToString(", ")
    }

    /**
     * Formats milliseconds into a digital clock format (e.g., "01:30:15").
     * Ideal for scoreboards, action bars, or active countdowns.
     *
     * @param millis The time duration in milliseconds.
     * @return The formatted digital time string (HH:MM:SS or MM:SS).
     */
    fun formatDigital(millis: Long): String {
        if (millis <= 0) return "00:00"

        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millis) % 60

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    /**
     * Converts milliseconds into a formatted string (e.g., "1h 30m" or "1 ชม. 30 นาที").
     *
     * @param millis The time in milliseconds.
     * @param player The target player used to determine the correct language locale.
     * @return A formatted string representation.
     */
    fun formatDuration(millis: Long, player: LuxPlayer? = null): String {
        if (millis <= 0) {
            return LanguageManager.getTranslation(player, "time.format.ready", "Ready")
        }

        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        val hours = (millis / (1000 * 60 * 60)) % 24
        val days = millis / (1000 * 60 * 60 * 24)

        val strDay = LanguageManager.getTranslation(player, "time.format.day", "d")
        val strHour = LanguageManager.getTranslation(player, "time.format.hour", "h")
        val strMinute = LanguageManager.getTranslation(player, "time.format.minute", "m")
        val strSecond = LanguageManager.getTranslation(player, "time.format.second", "s")

        val builder = StringBuilder()
        if (days > 0) builder.append("$days$strDay ")
        if (hours > 0) builder.append("$hours$strHour ")
        if (minutes > 0) builder.append("$minutes$strMinute ")
        if (seconds > 0 || builder.isEmpty()) builder.append("$seconds$strSecond")

        return builder.toString().trim()
    }
}

/**
 * Extension function to convert a Long (representing milliseconds) directly into a short time string.
 */
fun Long.toShortTime(): String = TimeFormatUtils.formatShort(this)

/**
 * Extension function to convert a Long (representing milliseconds) directly into an expanded time string.
 */
fun Long.toLongTime(): String = TimeFormatUtils.formatLong(this)

/**
 * Extension function to convert a Long (representing milliseconds) directly into a digital clock string.
 */
fun Long.toDigitalTime(): String = TimeFormatUtils.formatDigital(this)