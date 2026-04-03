package com.novaco.luxapi.commons.platform

/**
 * A central utility to retrieve information about the current server platform.
 */
object LuxPlatform {

    /**
     * The active platform type.
     * This should be initialized by the platform-specific entry points (e.g., LuxFabricInitializer).
     */
    var type: PlatformType = PlatformType.UNKNOWN
        internal set

    /**
     * Checks if the current environment is running Fabric.
     *
     * @return True if on Fabric, false otherwise.
     */
    fun isFabric(): Boolean = type == PlatformType.FABRIC

    /**
     * Checks if the current environment is running NeoForge.
     *
     * @return True if on NeoForge, false otherwise.
     */
    fun isNeoForge(): Boolean = type == PlatformType.NEOFORGE
}