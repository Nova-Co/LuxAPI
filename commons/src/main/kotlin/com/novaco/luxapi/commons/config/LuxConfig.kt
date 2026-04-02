package com.novaco.luxapi.commons.config

import com.novaco.luxapi.commons.config.annotation.Config
import java.io.File

/**
 * Base class for all configuration objects in LuxAPI.
 */
abstract class LuxConfig {

    @Transient
    private var configFile: File? = null

    /**
     * Internal method to set the physical file location.
     */
    fun init(file: File) {
        this.configFile = file
    }

    /**
     * Saves the current state of this config object back to the file.
     */
    fun save() {
        configFile?.let { ConfigService.save(this, it) }
    }

    /**
     * Reloads the data from the file back into this object.
     * Note: This usually requires a fresh load from ConfigService.
     */
    fun reload(): LuxConfig? {
        return configFile?.let { ConfigService.load(this::class.java, it.parentFile) }
    }
}