package com.novaco.luxapi.commons.i18n

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.novaco.luxapi.commons.chat.placeholder.PlaceholderManager
import com.novaco.luxapi.commons.player.LuxPlayer
import java.io.File

/**
 * Centralized registry for Internationalization (i18n).
 * Loads JSON language files and provides context-aware translations with fallback support.
 */
object LanguageManager {

    private val gson = Gson()

    // Structure: Map<LanguageCode, Map<MessageKey, TranslatedText>>
    private val translations = mutableMapOf<String, Map<String, String>>()

    private var defaultLanguage = "en_us"

    /**
     * Scans the target directory for .json files and loads them into memory.
     * * @param langFolder The directory containing language files (e.g., "plugins/LuxCore/lang/")
     * @param defaultLang The fallback language code to use when a translation is missing.
     */
    fun loadLanguages(langFolder: File, defaultLang: String = "en_us") {
        this.defaultLanguage = defaultLang.lowercase()
        translations.clear()

        if (!langFolder.exists()) {
            langFolder.mkdirs()
            return
        }

        langFolder.listFiles { file -> file.extension == "json" }?.forEach { file ->
            val langCode = file.nameWithoutExtension.lowercase()
            try {
                val type = object : TypeToken<Map<String, String>>() {}.type
                val map: Map<String, String> = gson.fromJson(file.readText(Charsets.UTF_8), type)
                translations[langCode] = map
            } catch (e: Exception) {
                println("[LuxAPI] Failed to load language file: ${file.name}")
                e.printStackTrace()
            }
        }
    }

    /**
     * Retrieves a translated string based on the player's client language.
     * Automatically processes global placeholders and local parameters.
     *
     * @param player The target player (used to determine locale and placeholders).
     * @param key The message key defined in the JSON file.
     * @param params Local variables to replace (e.g., mapOf("pokemon" to "Pikachu")).
     * @return The fully formatted and translated string.
     */
    fun getTranslation(player: LuxPlayer?, key: String, params: Map<String, String> = emptyMap()): String {
        // 1. Determine the target locale (fallback to default if player is null)
        val targetLocale = player?.locale?.lowercase() ?: defaultLanguage

        // 2. Attempt to fetch the translation, fallback to default language, then fallback to key
        var rawMessage = translations[targetLocale]?.get(key)
            ?: translations[defaultLanguage]?.get(key)
            ?: key

        // 3. Replace local temporary parameters (e.g., {pokemon})
        params.forEach { (paramKey, paramValue) ->
            rawMessage = rawMessage.replace("{$paramKey}", paramValue)
        }

        // 4. Process global placeholders (e.g., %player_name%) using our existing manager
        return PlaceholderManager.replace(player, rawMessage)
    }
}