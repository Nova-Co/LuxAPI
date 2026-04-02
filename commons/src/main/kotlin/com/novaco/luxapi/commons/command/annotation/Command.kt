package com.novaco.luxapi.commons.command.annotation

/**
 * Marks a class or function as a registrable command.
 * * @property name The primary name of the command (e.g., "pokemon").
 * @property aliases Alternative names for the command (e.g., "poke", "p").
 * @property permission The permission node required to use this command.
 * @property description A brief description of what the command does.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Command(
    val name: String,
    val aliases: Array<String> = [],
    val permission: String = "",
    val description: String = "No description provided."
)