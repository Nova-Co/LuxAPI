package com.novaco.luxapi.commons.metadata

import com.novaco.luxapi.commons.player.LuxPlayer
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * A property delegate that provides seamless reading and writing of player metadata.
 * Designed to replace bulky attribute management systems with idiomatic Kotlin properties.
 *
 * @param key The unique string key identifying this metadata entry.
 * @param default The fallback value to return if the metadata is unset.
 */
class MetadataDelegate<T : Any>(private val key: String, private val default: T) : ReadWriteProperty<LuxPlayer, T> {

    @Suppress("UNCHECKED_CAST")
    override fun getValue(thisRef: LuxPlayer, property: KProperty<*>): T {
        return thisRef.getMetadata(key, default::class.java as Class<T>) ?: default
    }

    override fun setValue(thisRef: LuxPlayer, property: KProperty<*>, value: T) {
        thisRef.setMetadata(key, value)
    }
}