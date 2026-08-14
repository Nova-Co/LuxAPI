package com.novaco.luxapi.commons.command.injector

import com.novaco.luxapi.commons.command.sender.CommandSender

/**
 * Opt-in extension of [ArgumentInjector] that also knows how to suggest tab-completion
 * values for the argument it parses. A command taking a type whose injector implements
 * this gets tab-completion for free, without a dedicated
 * [com.novaco.luxapi.commons.command.tab.TabHandler] or
 * [com.novaco.luxapi.commons.command.tab.TabRegistry] registration.
 *
 * Precedence in [com.novaco.luxapi.commons.command.CommandProcessor.getParameterSuggestions]:
 * an explicit `@TabComplete` annotation wins first, then an explicit [com.novaco.luxapi.commons.command.tab.TabRegistry]
 * registration, then this fallback.
 */
interface CompletingInjector<T> : ArgumentInjector<T> {

    /**
     * @param sender The entity requesting suggestions.
     * @param args The raw arguments typed so far, including the partial token being completed.
     * @param index The index into [args] of the token currently being completed — same
     * convention as [ArgumentInjector.instantiate]'s `index`.
     */
    fun getSuggestions(sender: CommandSender, args: Array<String>, index: Int): List<String>
}
