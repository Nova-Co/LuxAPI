package com.novaco.luxapi.commons.config

import com.novaco.luxapi.commons.LuxAPI
import java.io.File

/**
 * The base blueprint for all configuration objects within the LuxAPI framework.
 * Provides essential lifecycle methods such as saving and reloading.
 * * @author Gemini / Novaco
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
     * * @param file The file location where this configuration is stored.
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
     * Synchronizes the object state with the data currently stored in the file.
     * * @return A fresh instance of the configuration, or null if the file is inaccessible.
     */
    fun reload(): LuxConfig? {
        val folder = configFile?.parentFile ?: return null
        return ConfigService.load(this::class.java, folder)
    }
}