package com.novaco.luxapi.commons.service

import java.util.concurrent.ConcurrentHashMap

/**
 * The central registry for cross-platform services.
 * Allows modules to register and consume services dynamically without tight coupling.
 */
@Suppress("UNCHECKED_CAST")
object ServiceManager {

    private val services = ConcurrentHashMap<Class<*>, Any>()

    /**
     * Registers a service provider for a specific service class.
     *
     * @param serviceClass The interface or abstract class representing the service.
     * @param provider The actual implementation of the service.
     * @param <T> The type of the service.
     */
    fun <T : Any> register(serviceClass: Class<T>, provider: T) {
        services[serviceClass] = provider
    }

    /**
     * Retrieves a registered service provider for the specified service class.
     *
     * @param serviceClass The interface or abstract class representing the service.
     * @param <T> The type of the service.
     * @return The registered provider, or null if no provider is found.
     */
    fun <T : Any> get(serviceClass: Class<T>): T? {
        return services[serviceClass] as? T
    }

    /**
     * Checks if a specific service has been registered.
     *
     * @param serviceClass The interface or abstract class representing the service.
     * @param <T> The type of the service.
     * @return True if the service is registered, false otherwise.
     */
    fun <T : Any> has(serviceClass: Class<T>): Boolean {
        return services.containsKey(serviceClass)
    }

    /**
     * Unregisters a specific service.
     *
     * @param serviceClass The interface or abstract class representing the service.
     * @param <T> The type of the service.
     */
    fun <T : Any> unregister(serviceClass: Class<T>) {
        services.remove(serviceClass)
    }
}