package com.novaco.luxapi.commons.command.injector

import com.novaco.luxapi.commons.command.injector.impl.IntegerInjector
import com.novaco.luxapi.commons.command.injector.impl.PlayerInjector
import com.novaco.luxapi.commons.command.injector.impl.StringInjector
import com.novaco.luxapi.commons.player.PlayerManager

/**
 * A central registry holding all registered ArgumentInjectors.
 * This registry automatically initializes basic injectors and provides
 * methods for platform-specific modules (e.g., Fabric, NeoForge) to register
 * injectors that require platform-dependent implementations.
 */
object InjectorRegistry {
    private val injectors = mutableMapOf<Class<*>, ArgumentInjector<*>>()

    init {
        // Register standard, platform-independent injectors
        register(IntegerInjector())
        register(StringInjector())
    }

    /**
     * Registers a new injector into the system.
     * Overwrites any existing injector for the same target class.
     *
     * @param injector The ArgumentInjector to register.
     */
    fun <T : Any> register(injector: ArgumentInjector<T>) {
        injectors[injector.convertedClass] = injector
    }

    /**
     * Finds an appropriate injector for the requested class type.
     *
     * @param clazz The target class to find an injector for.
     * @return The registered ArgumentInjector, or null if none is found.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getInjector(clazz: Class<T>): ArgumentInjector<T>? {
        return injectors[clazz] as? ArgumentInjector<T>
    }

    /**
     * Registers the PlayerInjector.
     * Since PlayerManager implementations vary between platforms (Fabric vs. NeoForge),
     * this method MUST be called by the respective platform module during server initialization.
     *
     * @param playerManager The platform-specific implementation of PlayerManager.
     */
    fun registerPlayerInjector(playerManager: PlayerManager) {
        register(PlayerInjector(playerManager))
    }
}