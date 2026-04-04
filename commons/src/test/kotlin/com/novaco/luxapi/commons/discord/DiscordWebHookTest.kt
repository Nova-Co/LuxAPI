package com.novaco.luxapi.commons.discord

import com.google.gson.JsonObject
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class DiscordWebHookTest {

    @Test
    fun `test webhook payload generation without executing network request`() {
        val webhook = DiscordWebHook("http://dummy-url.local")
            .setContent("Hello World")
            .setUsername("LuxBot")
            .setAvatarUrl("http://example.com/avatar.png")
            .addEmbed(DiscordEmbed().setTitle("Test Embed"))

        // Use reflection to access and test the private buildJsonPayload method
        val buildMethod = DiscordWebHook::class.java.getDeclaredMethod("buildJsonPayload")
        buildMethod.isAccessible = true

        val payload = buildMethod.invoke(webhook) as JsonObject

        // Validate base payload properties
        assertEquals("Hello World", payload.get("content").asString, "Content must match.")
        assertEquals("LuxBot", payload.get("username").asString, "Username must match.")
        assertEquals("http://example.com/avatar.png", payload.get("avatar_url").asString, "Avatar URL must match.")

        // Validate embed injection
        assertTrue(payload.has("embeds"), "Payload must contain an embeds array.")
        val embedsArray = payload.getAsJsonArray("embeds")
        assertEquals(1, embedsArray.size(), "There should be exactly one embed attached.")
        assertEquals("Test Embed", embedsArray[0].asJsonObject.get("title").asString, "Embed title inside the final payload must match.")
    }
}