package com.novaco.luxapi.commons.command.injector

import com.novaco.luxapi.commons.command.injector.impl.IntegerInjector
import com.novaco.luxapi.commons.command.injector.impl.OfflinePlayerInjector
import com.novaco.luxapi.commons.command.injector.impl.PlayerInjector
import com.novaco.luxapi.commons.command.injector.impl.StringInjector
import com.novaco.luxapi.commons.command.sender.CommandSender
import com.novaco.luxapi.commons.player.PlayerLookupService
import com.novaco.luxapi.commons.player.PlayerManager
import java.util.concurrent.ConcurrentHashMap

/**
 * A centralized registry maintaining all active ArgumentInjectors.
 * Facilitates the dynamic injection of complex types during command execution.
 * Backed by a [ConcurrentHashMap] since registration can happen from multiple
 * platform init paths while lookups are already happening on the command thread.
 */
object InjectorRegistry {
    private val injectors = ConcurrentHashMap<Class<*>, ArgumentInjector<*>>()

    init {
        register(IntegerInjector())
        register(StringInjector())
    }

    /**
     * Registers a pre-instantiated injector into the registry.
     *
     * @param injector The ArgumentInjector implementation to register.
     */
    fun <T : Any> register(injector: ArgumentInjector<T>) {
        injectors[injector.convertedClass] = injector
    }

    /**
     * Registers a new injector dynamically using Kotlin's reified types and a lambda factory.
     *
     * @param factory The functional implementation defining the conversion logic.
     */
    inline fun <reified T : Any> register(noinline factory: (CommandSender, Array<String>, Int) -> T?) {
        register(object : ArgumentInjector<T> {
            override val convertedClass: Class<T> = T::class.java
            override fun instantiate(sender: CommandSender, args: Array<String>, index: Int): T? {
                return factory(sender, args, index)
            }
        })
    }

    /**
     * Retrieves an appropriate injector for the specified class type.
     *
     * @param clazz The target class to evaluate.
     * @return The registered ArgumentInjector, or null if unsupported.
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> getInjector(clazz: Class<T>): ArgumentInjector<T>? {
        return injectors[clazz] as? ArgumentInjector<T>
    }

    /**
     * Registers the platform-dependent PlayerInjector.
     *
     * @param playerManager The active platform implementation of PlayerManager.
     * @param lookupService Optional; when provided, successful online resolutions are recorded
     * so a later [OfflinePlayerInjector] lookup for the same player can still succeed offline.
     */
    fun registerPlayerInjector(playerManager: PlayerManager, lookupService: PlayerLookupService? = null) {
        register(PlayerInjector(playerManager, lookupService))
    }

    /**
     * Registers the [OfflinePlayerInjector], resolving [com.novaco.luxapi.commons.player.OfflinePlayer]
     * arguments whether or not the target is currently online.
     *
     * @param playerManager The active platform implementation of PlayerManager.
     * @param lookupService The name/UUID cache used to resolve targets that are not online.
     */
    fun registerOfflinePlayerInjector(playerManager: PlayerManager, lookupService: PlayerLookupService) {
        register(OfflinePlayerInjector(playerManager, lookupService))
    }
}