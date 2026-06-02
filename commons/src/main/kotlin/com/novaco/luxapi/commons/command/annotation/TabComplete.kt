package com.novaco.luxapi.commons.command.annotation

import com.novaco.luxapi.commons.command.tab.TabHandler
import kotlin.reflect.KClass

/**
 * Marks a command parameter to use a specific TabHandler for suggestions.
 * This allows native types like String or Int to have dynamic tab completions
 * without requiring custom argument wrappers.
 *
 * @property value The TabHandler class to instantiate and use.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class TabComplete(
    val value: KClass<out TabHandler>
)