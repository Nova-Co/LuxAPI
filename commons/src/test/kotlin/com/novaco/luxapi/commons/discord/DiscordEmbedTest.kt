package com.novaco.luxapi.commons.discord

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DiscordEmbedTest {

    @Test
    fun `test embed builder constructs proper json object`() {
        val titleText = "Server Started"
        val descText = "LuxAPI has successfully initialized."
        val hexColor = 0x00FF00
        val fieldName = "Status"
        val fieldValue = "Online"

        // Construct the embed using the fluent API
        val embed = DiscordEmbed()
            .setTitle(titleText)
            .setDescription(descText)
            .setColor(hexColor)
            .addField(fieldName, fieldValue, true)
            .setThumbnail("http://example.com/thumb.png")
            .setFooter("Powered by LuxAPI")

        // Retrieve the generated JSON
        val json = embed.toJson()

        // Validate root properties
        assertEquals(titleText, json.get("title").asString, "Embed title must match.")
        assertEquals(descText, json.get("description").asString, "Embed description must match.")
        assertEquals(hexColor, json.get("color").asInt, "Embed color must match.")

        // Validate the fields array
        assertTrue(json.has("fields"), "Embed must contain a fields array.")
        val fieldsArray = json.getAsJsonArray("fields")
        assertEquals(1, fieldsArray.size(), "There should be exactly one field.")

        val firstField = fieldsArray[0].asJsonObject
        assertEquals(fieldName, firstField.get("name").asString, "Field name must match.")
        assertEquals(fieldValue, firstField.get("value").asString, "Field value must match.")
        assertTrue(firstField.get("inline").asBoolean, "Field should be set to inline.")

        // Validate nested objects (Thumbnail & Footer)
        assertTrue(json.has("thumbnail"), "Embed must contain a thumbnail object.")
        assertEquals("http://example.com/thumb.png", json.getAsJsonObject("thumbnail").get("url").asString)

        assertTrue(json.has("footer"), "Embed must contain a footer object.")
        assertEquals("Powered by LuxAPI", json.getAsJsonObject("footer").get("text").asString)
    }
}