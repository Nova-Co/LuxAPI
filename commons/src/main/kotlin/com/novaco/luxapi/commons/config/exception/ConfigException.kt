package com.novaco.luxapi.commons.config.exception

/**
 * Thrown when a config file fails to load, save, or resolve its default resource.
 * Wraps the underlying Configurate/IO failure so callers don't need to catch
 * Configurate-internal exception types.
 */
class ConfigException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
