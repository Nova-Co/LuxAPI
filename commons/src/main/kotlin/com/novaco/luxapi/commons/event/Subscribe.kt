package com.novaco.luxapi.commons.event

/**
 * Annotation used to mark a method as an event handler.
 * Methods annotated with this must accept exactly one parameter extending LuxEvent.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Subscribe