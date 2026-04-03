package com.novaco.luxapi.commons.discord

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CompletableFuture

/**
 * Handles the construction and asynchronous execution of Discord Webhook payloads.
 * Ensures that HTTP requests do not block the main server thread.
 *
 * @param webhookUrl The destination URL provided by Discord.
 */
class DiscordWebHook(private val webhookUrl: String) {

    private var content: String? = null
    private var username: String? = null
    private var avatarUrl: String? = null
    private val embeds = mutableListOf<DiscordEmbed>()

    /**
     * Sets the raw text message to be sent alongside the webhook.
     *
     * @param content The text message.
     * @return This webhook instance for chaining.
     */
    fun setContent(content: String): DiscordWebHook = apply {
        this.content = content
    }

    /**
     * Overrides the default username of the webhook bot.
     *
     * @param username The custom username to display.
     * @return This webhook instance for chaining.
     */
    fun setUsername(username: String): DiscordWebHook = apply {
        this.username = username
    }

    /**
     * Overrides the default avatar image of the webhook bot.
     *
     * @param avatarUrl The URL of the custom avatar image.
     * @return This webhook instance for chaining.
     */
    fun setAvatarUrl(avatarUrl: String): DiscordWebHook = apply {
        this.avatarUrl = avatarUrl
    }

    /**
     * Attaches a rich embed to the webhook payload.
     * Discord allows a maximum of 10 embeds per message.
     *
     * @param embed The constructed DiscordEmbed.
     * @return This webhook instance for chaining.
     */
    fun addEmbed(embed: DiscordEmbed): DiscordWebHook = apply {
        this.embeds.add(embed)
    }

    /**
     * Executes the HTTP POST request to Discord asynchronously.
     * Prints an error to the console if the connection or payload fails.
     */
    fun executeAsync() {
        CompletableFuture.runAsync {
            try {
                val url = URL(webhookUrl)
                val connection = url.openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("User-Agent", "LuxAPI-Webhook-Client")
                connection.doOutput = true

                val payload = buildJsonPayload()

                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(payload.toString())
                    writer.flush()
                }

                val responseCode = connection.responseCode
                if (responseCode < 200 || responseCode >= 300) {
                    println("[LuxAPI] Failed to send Discord Webhook! Response Code: $responseCode")
                }

                connection.disconnect()
            } catch (e: Exception) {
                println("[LuxAPI] Exception occurred while sending Discord Webhook:")
                e.printStackTrace()
            }
        }
    }

    /**
     * Compiles all properties and embeds into a single JSON payload.
     *
     * @return The final JsonObject ready for transmission.
     */
    private fun buildJsonPayload(): JsonObject {
        val payload = JsonObject()

        content?.let { payload.addProperty("content", it) }
        username?.let { payload.addProperty("username", it) }
        avatarUrl?.let { payload.addProperty("avatar_url", it) }

        if (embeds.isNotEmpty()) {
            val embedArray = JsonArray()
            embeds.forEach { embedArray.add(it.toJson()) }
            payload.add("embeds", embedArray)
        }

        return payload
    }
}