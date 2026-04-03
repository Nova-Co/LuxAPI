package com.novaco.luxapi.commons.discord

import com.google.gson.JsonArray
import com.google.gson.JsonObject

/**
 * A builder class for constructing rich Discord embeds.
 * Allows customization of titles, descriptions, colors, fields, and images.
 */
class DiscordEmbed {

    private val json = JsonObject()
    private val fields = JsonArray()

    init {
        json.add("fields", fields)
    }

    /**
     * Sets the title of the embed.
     *
     * @param title The text to display as the title.
     * @return This embed instance for chaining.
     */
    fun setTitle(title: String): DiscordEmbed = apply {
        json.addProperty("title", title)
    }

    /**
     * Sets the main description body of the embed.
     *
     * @param description The text to display in the body.
     * @return This embed instance for chaining.
     */
    fun setDescription(description: String): DiscordEmbed = apply {
        json.addProperty("description", description)
    }

    /**
     * Sets the color of the embed's side border.
     *
     * @param hexColor The hex color code (e.g., 0xFF0000 for red).
     * @return This embed instance for chaining.
     */
    fun setColor(hexColor: Int): DiscordEmbed = apply {
        json.addProperty("color", hexColor)
    }

    /**
     * Adds a custom field to the embed.
     *
     * @param name The title of the field.
     * @param value The content of the field.
     * @param inline Whether the field should be displayed inline with others.
     * @return This embed instance for chaining.
     */
    fun addField(name: String, value: String, inline: Boolean = false): DiscordEmbed = apply {
        val field = JsonObject()
        field.addProperty("name", name)
        field.addProperty("value", value)
        field.addProperty("inline", inline)
        fields.add(field)
    }

    /**
     * Sets the thumbnail image located in the top right corner of the embed.
     *
     * @param url The URL of the image.
     * @return This embed instance for chaining.
     */
    fun setThumbnail(url: String): DiscordEmbed = apply {
        val thumbnail = JsonObject()
        thumbnail.addProperty("url", url)
        json.add("thumbnail", thumbnail)
    }

    /**
     * Sets the footer text at the bottom of the embed.
     *
     * @param text The text to display in the footer.
     * @return This embed instance for chaining.
     */
    fun setFooter(text: String): DiscordEmbed = apply {
        val footer = JsonObject()
        footer.addProperty("text", text)
        json.add("footer", footer)
    }

    /**
     * Compiles the embed into a JsonObject ready for the webhook payload.
     *
     * @return The constructed JsonObject.
     */
    fun toJson(): JsonObject {
        return json
    }
}