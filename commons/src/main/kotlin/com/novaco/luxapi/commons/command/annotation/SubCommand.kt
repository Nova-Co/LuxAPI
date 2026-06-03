package com.novaco.luxapi.commons.command.annotation

/**
 * Marks a method inside a command class as a executable sub-command.
 *
 * @property name The specific sub-command identifier.
 * @property aliases Alternative names for this sub-command.
 * @property permission Required permission to execute this specific sub-command.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class SubCommand(
    val name: String,
    val aliases: Array<String> = [],
    val permission: String = ""
)