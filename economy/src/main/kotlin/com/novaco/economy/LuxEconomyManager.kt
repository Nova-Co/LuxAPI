package com.novaco.economy

/**
 * Manager class responsible for holding and providing the active economy implementation.
 * The server's economy bridge plugin should register its provider here during server startup.
 */
object LuxEconomyManager {

    private var provider: LuxEconomyCore? = null

    /**
     * Registers the economy provider implementation.
     *
     * @param economyProvider The implemented LuxEconomyCore instance.
     */
    fun registerProvider(economyProvider: LuxEconomyCore) {
        this.provider = economyProvider
    }

    /**
     * Retrieves the registered economy provider.
     *
     * @return The active LuxEconomyCore instance.
     * @throws IllegalStateException If no provider has been registered yet.
     */
    fun getProvider(): LuxEconomyCore {
        return provider ?: throw IllegalStateException("LuxEconomyCore provider has not been registered yet!")
    }

    /**
     * Checks if an economy provider is currently registered and available.
     *
     * @return True if a provider is registered, false otherwise.
     */
    fun isReady(): Boolean {
        return provider != null
    }
}