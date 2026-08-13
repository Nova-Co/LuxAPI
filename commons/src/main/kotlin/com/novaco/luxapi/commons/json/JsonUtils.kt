package com.novaco.luxapi.commons.json

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException

/**
 * Shared JSON serialization helpers built on the `Gson` instance already used internally
 * by the Discord module — a single configured [GSON] instance and null-safe accessors,
 * so devs don't need to bring/configure their own Gson instance for simple cases.
 */
object JsonUtils {

    val GSON: Gson = GsonBuilder().disableHtmlEscaping().create()

    /**
     * Serializes any object into a compact JSON string.
     */
    fun toJson(value: Any?): String = GSON.toJson(value)

    /**
     * Deserializes a JSON string into the requested type, or null if parsing fails.
     */
    inline fun <reified T> fromJson(json: String): T? {
        return try {
            GSON.fromJson(json, T::class.java)
        } catch (_: JsonSyntaxException) {
            null
        }
    }

    /**
     * Parses a raw JSON string into a [JsonObject], or null if it isn't a valid JSON object.
     */
    fun parseObject(json: String): JsonObject? {
        return try {
            JsonParser.parseString(json).takeIf { it.isJsonObject }?.asJsonObject
        } catch (_: JsonSyntaxException) {
            null
        }
    }

    /**
     * Parses a raw JSON string into a [JsonArray], or null if it isn't a valid JSON array.
     */
    fun parseArray(json: String): JsonArray? {
        return try {
            JsonParser.parseString(json).takeIf { it.isJsonArray }?.asJsonArray
        } catch (_: JsonSyntaxException) {
            null
        }
    }

    /**
     * Reads a string field, or [default] if it's absent, null, or not a primitive value.
     */
    fun JsonObject.getStringOrDefault(key: String, default: String = ""): String {
        return safeElement(key)?.takeIf { it.isJsonPrimitive }?.asString ?: default
    }

    /**
     * Reads an int field, or [default] if it's absent, null, or not a valid number.
     */
    fun JsonObject.getIntOrDefault(key: String, default: Int = 0): Int {
        return safeElement(key)?.takeIf { it.isJsonPrimitive }?.asInt ?: default
    }

    /**
     * Reads a double field, or [default] if it's absent, null, or not a valid number.
     */
    fun JsonObject.getDoubleOrDefault(key: String, default: Double = 0.0): Double {
        return safeElement(key)?.takeIf { it.isJsonPrimitive }?.asDouble ?: default
    }

    /**
     * Reads a boolean field, or [default] if it's absent, null, or not a valid boolean.
     */
    fun JsonObject.getBooleanOrDefault(key: String, default: Boolean = false): Boolean {
        return safeElement(key)?.takeIf { it.isJsonPrimitive }?.asBoolean ?: default
    }

    private fun JsonObject.safeElement(key: String): JsonElement? {
        val element = this.get(key) ?: return null
        return if (element.isJsonNull) null else element
    }
}
