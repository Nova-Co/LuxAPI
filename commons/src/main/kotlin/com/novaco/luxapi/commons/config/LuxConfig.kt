package com.novaco.luxapi.commons.config

import java.io.File
import java.lang.reflect.Modifier

/**
 * The base blueprint for all configuration objects within the LuxAPI framework.
 * Provides essential lifecycle methods such as saving and reloading.
 *
 */
abstract class LuxConfig {

    /**
     * Stores the reference to the physical file on the disk.
     * Marked as @Transient to prevent it from being serialized into the config itself.
     */
    @Transient
    private var configFile: File? = null

    /**
     * Initializes the configuration with a specific file reference.
     *
     * @param file The file location where this configuration is stored.
     */
    fun init(file: File) {
        this.configFile = file
    }

    /**
     * Commits the current state of the object variables to the physical file.
     * This method triggers the ConfigService to perform a write operation.
     */
    fun save() {
        configFile?.let { ConfigService.save(this, it) }
    }

    /**
     * Synchronizes the current object state with the data stored in the file.
     * It updates the fields of THIS instance automatically.
     *
     * Skips any field marked `@Transient` (or JVM `transient`) instead of the
     * `configFile` field alone, so a subclass can declare a non-persisted or
     * computed field without it being silently overwritten by `null`/default
     * on every reload.
     */
    fun reload() {
        val file = configFile ?: return
        val folder = file.parentFile
        val freshInstance = ConfigService.load(this::class.java, folder)

        this::class.java.declaredFields.forEach { field ->
            val isTransient = Modifier.isTransient(field.modifiers) ||
                field.isAnnotationPresent(Transient::class.java)
            if (!isTransient) {
                field.isAccessible = true
                val freshValue = field.get(freshInstance)
                field.set(this, freshValue)
            }
        }
    }
}