package com.novaco.luxapi.commons.command.annotation

import com.novaco.luxapi.commons.command.tab.TabHandler
import kotlin.reflect.KClass

/**
 * Instructs the command parser to use a specific TabHandler for this parameter.
 *
 * @property value The TabHandler class to instantiate for suggestions.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class TabComplete(
    val value: KClass<out TabHandler>
)