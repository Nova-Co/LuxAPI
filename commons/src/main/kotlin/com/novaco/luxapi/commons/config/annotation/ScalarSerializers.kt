package com.novaco.luxapi.commons.config.annotation

import org.spongepowered.configurate.serialize.TypeSerializer
import kotlin.reflect.KClass

/**
 * Registers extra [TypeSerializer]s scoped to just this config class, instead of going through
 * the global [com.novaco.luxapi.commons.config.serializer.ConfigTypeSerializerRegistry]. Useful
 * for a one-off type only this config uses. Each listed class must have a no-arg constructor.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ScalarSerializers(
    val value: Array<KClass<out TypeSerializer<*>>>
)
