package com.novaco.luxapi.commons.command.annotation

/**
 * Marks a method inside a @LuxCommand class as a sub-command.
 * * @property name The sub-command name (e.g., "reload" in "/lux reload").
 * @property aliases Alternative names (e.g., "rl").
 * @property permission Required permission for this specific sub-command.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class SubCommand(
    val name: String,
    val aliases: Array<String> = [],
    val permission: String = ""
)