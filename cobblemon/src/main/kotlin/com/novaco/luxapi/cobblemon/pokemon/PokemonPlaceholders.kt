package com.novaco.luxapi.cobblemon.placeholder

import com.cobblemon.mod.common.pokemon.Pokemon
import com.novaco.luxapi.cobblemon.pokemon.getIVPercentage

/**
 * A central registry for handling dynamic text replacements (Placeholders) related to Pokémon.
 * This system allows developers to register custom placeholders and parse strings seamlessly.
 *
 * It acts as a lightweight alternative to external placeholder APIs, specifically tailored
 * for Cobblemon data representation in UI, chat, or items.
 *
 */
object PokemonPlaceholderRegistry {

    /**
     * A map storing the registered placeholders.
     * Key: The placeholder string (e.g., "%pokemon_name%")
     * Value: A function that takes a [Pokemon] instance and returns the replacement [String].
     */
    private val placeholders: MutableMap<String, (Pokemon) -> String> = mutableMapOf()

    init {
        registerDefaultPlaceholders()
    }

    /**
     * Registers a new placeholder to the system.
     * If the placeholder already exists, it will be overwritten.
     *
     * @param placeholder The exact string to be replaced (e.g., "%custom_stat%").
     * @param transformer The logic defining how to extract data from the [Pokemon].
     */
    fun register(placeholder: String, transformer: (Pokemon) -> String) {
        placeholders[placeholder] = transformer
    }

    /**
     * Parses a string and replaces all registered placeholders with the actual Pokémon data.
     *
     * @param text The raw string containing placeholders.
     * @param pokemon The [Pokemon] instance to extract data from.
     * @return The formatted string with all placeholders replaced.
     */
    fun parse(text: String, pokemon: Pokemon): String {
        var result = text
        placeholders.forEach { (placeholder, transformer) ->
            if (result.contains(placeholder)) {
                result = result.replace(placeholder, transformer(pokemon))
            }
        }
        return result
    }

    /**
     * Registers the built-in, default placeholders used commonly across the API.
     */
    private fun registerDefaultPlaceholders() {
        register("%pokemon_name%") { it.species.name.replaceFirstChar { char -> char.uppercase() } }
        register("%pokemon_level%") { it.level.toString() }

        register("%pokemon_shiny%") { if (it.shiny) "★" else "" }
        register("%pokemon_shiny_text%") { if (it.shiny) "Shiny" else "Normal" }

        register("%pokemon_gender%") {
            when (it.gender.name.uppercase()) {
                "MALE" -> "♂"
                "FEMALE" -> "♀"
                else -> "⚲"
            }
        }

        register("%pokemon_nature%") { it.nature.name.path.replaceFirstChar { char -> char.uppercase() } }
        register("%pokemon_ability%") { it.ability.name.replaceFirstChar { char -> char.uppercase() } }
        register("%pokemon_iv_percentage%") { String.format("%.2f", it.getIVPercentage()) }
    }
}