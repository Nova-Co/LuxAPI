package com.novaco.luxapi.bukkit.command

/**
 * Thrown by [BukkitCommandManager] when it can't reflectively extract the server's internal
 * `SimpleCommandMap` — meaning the running server implementation's internal shape doesn't match
 * what this reflection call expects (e.g. a `commandMap` field rename in a future/forked
 * server jar), not a normal runtime condition a caller could otherwise recover from.
 */
class CommandMapExtractionException(message: String, cause: Throwable) : RuntimeException(message, cause)
