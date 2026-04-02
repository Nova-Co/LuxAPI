package com.novaco.luxapi.commons.config.annotation

/**
 * Adds a comment description to a configuration field or class.
 * This will be rendered in the final .yml file.
 *
 * @property value The comment text to display.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Comment(
    val value: String
)