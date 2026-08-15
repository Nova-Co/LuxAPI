package com.novaco.luxapi.core.command

import com.mojang.brigadier.suggestion.SuggestionProvider
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.SharedSuggestionProvider

/**
 * Ready-made Brigadier [SuggestionProvider]s for common argument sources.
 */
object SuggestionUtils {

    /**
     * Suggests a fixed, static list of values.
     */
    fun of(vararg values: String): SuggestionProvider<CommandSourceStack> {
        val list = values.toList()
        return SuggestionProvider { _, builder -> SharedSuggestionProvider.suggest(list, builder) }
    }

    /**
     * Suggests a dynamically computed list of values, recomputed on every keystroke.
     */
    fun dynamic(values: () -> Collection<String>): SuggestionProvider<CommandSourceStack> {
        return SuggestionProvider { _, builder -> SharedSuggestionProvider.suggest(values(), builder) }
    }

    /**
     * Suggests the names of currently online players.
     */
    fun onlinePlayers(): SuggestionProvider<CommandSourceStack> {
        return SuggestionProvider { context, builder ->
            val names = context.source.server.playerList.players.map { it.gameProfile.name }
            SharedSuggestionProvider.suggest(names, builder)
        }
    }
}
