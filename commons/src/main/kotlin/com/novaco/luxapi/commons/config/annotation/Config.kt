package com.novaco.luxapi.commons.config.annotation

/**
 * Marks a class as a configuration file.
 *
 * @property path The filename or path (e.g., "config.yml" or "settings/messages.yml").
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Config(
    val path: String
)