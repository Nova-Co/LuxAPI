package com.novaco.luxapi.cobblemon.placeholder

import com.cobblemon.mod.common.pokemon.Pokemon
import net.minecraft.network.chat.Component

/**
 * Extension functions providing quick and concise access to the [PokemonPlaceholderRegistry].
 *
 * @author NovaCo
 */

/**
 * Parses the current string and replaces all Pokémon-related placeholders.
 *
 * @param pokemon The target [Pokemon] providing the data.
 * @return A new string with the data injected.
 */
fun String.parsePlaceholders(pokemon: Pokemon): String {
    return PokemonPlaceholderRegistry.parse(this, pokemon)
}

/**
 * Parses the current string into a [Component], replacing all Pokémon-related placeholders.
 * Ideal for setting item names, lore, or sending chat messages.
 *
 * @param pokemon The target [Pokemon] providing the data.
 * @return A Minecraft [Component] ready to be displayed.
 */
fun String.toParsedComponent(pokemon: Pokemon): Component {
    val parsedString = this.parsePlaceholders(pokemon)
    return Component.literal(parsedString)
}