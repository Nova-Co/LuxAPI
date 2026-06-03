package com.novaco.luxapi.commons.command.injector

import com.novaco.luxapi.commons.command.injector.impl.IntegerInjector
import com.novaco.luxapi.commons.command.injector.impl.PlayerInjector
import com.novaco.luxapi.commons.command.injector.impl.StringInjector
import com.novaco.luxapi.commons.command.sender.CommandSender
import com.novaco.luxapi.commons.player.PlayerManager

/**
 * A centralized registry maintaining all active ArgumentInjectors.
 * Facilitates the dynamic injection of complex types during command execution.
 */
object InjectorRegistry {
    private val injectors = mutableMapOf<Class<*>, ArgumentInjector<*>>()

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
     */
    fun registerPlayerInjector(playerManager: PlayerManager) {
        register(PlayerInjector(playerManager))
    }
}