package com.novaco.luxapi.commons.type

/**
 * Type-safe conversion helpers for the common case of turning loosely-typed input
 * (command arguments, config values, placeholder params) into a concrete type without
 * a raw `as` cast or a scattered try/catch at every call site.
 */
object TypeUtils {

    /**
     * Safely casts [value] to [T], returning null instead of throwing on a mismatch.
     */
    inline fun <reified T> safeCast(value: Any?): T? = value as? T

    /**
     * Parses [value] as an [Int], returning [default] if it's null or not a valid integer.
     */
    fun asInt(value: Any?, default: Int = 0): Int {
        return when (value) {
            is Int -> value
            is Number -> value.toInt()
            is String -> value.toIntOrNull() ?: default
            else -> default
        }
    }

    /**
     * Parses [value] as a [Long], returning [default] if it's null or not a valid long.
     */
    fun asLong(value: Any?, default: Long = 0L): Long {
        return when (value) {
            is Long -> value
            is Number -> value.toLong()
            is String -> value.toLongOrNull() ?: default
            else -> default
        }
    }

    /**
     * Parses [value] as a [Double], returning [default] if it's null or not a valid double.
     */
    fun asDouble(value: Any?, default: Double = 0.0): Double {
        return when (value) {
            is Double -> value
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull() ?: default
            else -> default
        }
    }

    /**
     * Parses [value] as a [Boolean]. Accepts native booleans and the strings
     * "true"/"false" (case-insensitive); anything else returns [default].
     */
    fun asBoolean(value: Any?, default: Boolean = false): Boolean {
        return when (value) {
            is Boolean -> value
            is String -> when (value.lowercase()) {
                "true" -> true
                "false" -> false
                else -> default
            }
            else -> default
        }
    }

    /**
     * Converts [value] to its [String] form, or [default] if [value] is null.
     */
    fun asString(value: Any?, default: String = ""): String {
        return value?.toString() ?: default
    }

    /**
     * Resolves [name] to an enum constant of [T] (case-insensitive), or [default] if
     * [name] is null or doesn't match any constant.
     */
    inline fun <reified T : Enum<T>> asEnum(name: String?, default: T): T {
        if (name == null) return default
        return enumValues<T>().firstOrNull { it.name.equals(name, ignoreCase = true) } ?: default
    }
}
