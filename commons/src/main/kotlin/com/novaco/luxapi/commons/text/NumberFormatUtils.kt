package com.novaco.luxapi.commons.text

import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

/**
 * A utility class designed to parse and format raw numerical values into human-readable strings.
 * Ideal for displaying economy balances, player statistics, and damage indicators.
 */
object NumberFormatUtils {

    private val COMMA_FORMAT = DecimalFormat("#,###.##")
    private val SUFFIX_FORMAT = DecimalFormat("#.##")
    private val SUFFIXES = arrayOf("", "K", "M", "B", "T", "Q")

    /**
     * Formats a raw number by inserting commas for standard readability.
     * Example: 1500000.5 -> "1,500,000.5"
     *
     * @param value The numerical value to format (Int, Double, Float, Long).
     * @return The formatted string with commas.
     */
    fun formatWithCommas(value: Number): String {
        return COMMA_FORMAT.format(value.toDouble())
    }

    /**
     * Formats a large number by truncating it and appending an alphabetical suffix.
     * Example: 1500000 -> "1.5M", 2500 -> "2.5K"
     *
     * @param value The numerical value to format.
     * @return The abbreviated string with the appropriate suffix.
     */
    fun formatWithSuffix(value: Number): String {
        val num = value.toDouble()

        if (num.isNaN() || num.isInfinite()) return num.toString()
        if (num < 0) return "-" + formatWithSuffix(-num)
        if (num < 1000) return formatWithCommas(num)

        val exp = (log10(num) / 3).toInt()
        val suffixIndex = exp.coerceAtMost(SUFFIXES.size - 1)
        val divisor = 10.0.pow(suffixIndex * 3)

        return SUFFIX_FORMAT.format(num / divisor) + SUFFIXES[suffixIndex]
    }
}

/**
 * Extension function to instantly format any Number type with commas.
 * Usage: val display = playerBalance.toCommas()
 */
fun Number.toCommas(): String = NumberFormatUtils.formatWithCommas(this)

/**
 * Extension function to instantly abbreviate any Number type with a suffix.
 * Usage: val display = damageOutput.toSuffix()
 */
fun Number.toSuffix(): String = NumberFormatUtils.formatWithSuffix(this)