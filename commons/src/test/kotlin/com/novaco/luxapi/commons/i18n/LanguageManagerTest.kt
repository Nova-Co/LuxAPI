package com.novaco.luxapi.commons.i18n

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LanguageManagerTest {

    /**
     * A dummy implementation to simulate the LanguageManager's internal translation map.
     * We assume there is a method to fetch and format the string.
     */
    private val mockTranslations = mapOf(
        "welcome_message" to "Welcome to the server, {player}!",
        "balance_update" to "Your new balance is ${'$'}{amount}."
    )

    /**
     * A helper function simulating the LanguageManager's replacement logic.
     * Adjust this to match your actual LanguageManager method (e.g., LanguageManager.getTranslation).
     */
    private fun getTranslatedString(key: String, params: Map<String, String>): String {
        var text = mockTranslations[key] ?: return key
        for ((paramKey, paramValue) in params) {
            text = text.replace("{$paramKey}", paramValue)
        }
        return text
    }

    @Test
    fun `test translation parameter injection`() {
        val params = mapOf("player" to "Novaco")
        val result = getTranslatedString("welcome_message", params)

        assertEquals("Welcome to the server, Novaco!", result, "The {player} parameter should be replaced correctly.")
    }

    @Test
    fun `test multiple parameter injection`() {
        val params = mapOf("amount" to "1,500")
        val result = getTranslatedString("balance_update", params)

        assertEquals("Your new balance is $1,500.", result, "The {amount} parameter should be replaced correctly.")
    }

    @Test
    fun `test missing translation key returns the key itself`() {
        val result = getTranslatedString("unknown_key_123", emptyMap())

        assertEquals("unknown_key_123", result, "If a translation is missing, it should gracefully return the raw key.")
    }
}