package com.novaco.luxapi.commons.reflection

import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * Shared reflection helpers, consolidating the small reflective operations already
 * duplicated ad hoc across `commons` (no-arg construction in `CommandScanner`,
 * annotated-method discovery in `EventBus`, field access in `ConfigService`).
 */
object ReflectionUtils {

    /**
     * Constructs a new instance of [clazz] via its no-arg constructor, or null if
     * none exists or construction fails for any reason.
     */
    fun <T> newInstance(clazz: Class<T>): T? {
        return try {
            val constructor = clazz.getDeclaredConstructor()
            constructor.isAccessible = true
            constructor.newInstance()
        } catch (_: ReflectiveOperationException) {
            null
        }
    }

    /**
     * Reads the value of a declared field (including private ones) by name, or null
     * if the field doesn't exist or can't be read.
     */
    fun getFieldValue(target: Any, fieldName: String): Any? {
        return try {
            val field = findField(target.javaClass, fieldName) ?: return null
            field.isAccessible = true
            field.get(target)
        } catch (_: ReflectiveOperationException) {
            null
        }
    }

    /**
     * Writes [value] into a declared field (including private ones) by name.
     * Returns true if the write succeeded, false otherwise.
     */
    fun setFieldValue(target: Any, fieldName: String, value: Any?): Boolean {
        return try {
            val field = findField(target.javaClass, fieldName) ?: return false
            field.isAccessible = true
            field.set(target, value)
            true
        } catch (_: ReflectiveOperationException) {
            false
        }
    }

    /**
     * Finds every declared field on [clazz] (its own class only, not superclasses)
     * annotated with [annotation].
     */
    fun findAnnotatedFields(clazz: Class<*>, annotation: Class<out Annotation>): List<Field> {
        return clazz.declaredFields.filter { it.isAnnotationPresent(annotation) }
    }

    /**
     * Finds every declared method on [clazz] (its own class only, not superclasses)
     * annotated with [annotation].
     */
    fun findAnnotatedMethods(clazz: Class<*>, annotation: Class<out Annotation>): List<Method> {
        return clazz.declaredMethods.filter { it.isAnnotationPresent(annotation) }
    }

    /**
     * Walks up the class hierarchy to find a declared field by name, since
     * `Class.getDeclaredField` only looks at the exact class, not its superclasses.
     */
    private fun findField(clazz: Class<*>, fieldName: String): Field? {
        var current: Class<*>? = clazz
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName)
            } catch (_: NoSuchFieldException) {
                current = current.superclass
            }
        }
        return null
    }
}
