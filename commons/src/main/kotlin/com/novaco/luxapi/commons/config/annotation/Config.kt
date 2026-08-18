package com.novaco.luxapi.commons.config.annotation

/**
 * Marks a class as a configuration file.
 *
 * @property path The filename or path (e.g., "config.yml" or "settings/messages.yml").
 * @property resource Optional classpath resource (e.g. "config.yml") copied into place on first
 * load when the target file doesn't exist yet. Lets a hand-authored default ship inside the jar
 * instead of relying solely on field defaults. Blank (default) skips this and falls back to the
 * programmatic-defaults behavior.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Config(
    val path: String,
    val resource: String = ""
)