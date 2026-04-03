package com.novaco.luxapi.commons.platform

/**
 * Represents the specific server platform environment the API is currently executing in.
 */
enum class PlatformType {
    /**
     * Represents the Fabric Mod Loader.
     */
    FABRIC,

    /**
     * Represents the NeoForge Mod Loader.
     */
    NEOFORGE,

    /**
     * Represents an unrecognized or unsupported platform.
     */
    UNKNOWN
}