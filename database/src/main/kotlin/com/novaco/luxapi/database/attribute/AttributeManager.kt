package com.novaco.luxapi.database.attribute

import com.novaco.luxapi.commons.event.Subscribe
import com.novaco.luxapi.commons.event.player.PlayerJoinEvent
import com.novaco.luxapi.commons.event.player.PlayerQuitEvent
import com.novaco.luxapi.commons.player.LuxPlayer
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Centralized manager for handling the lifecycle of PersistentAttributes.
 * Automatically loads data when players join and saves it when they quit.
 */
object AttributeManager {

    private val registeredAttributes = mutableSetOf<Class<out PersistentAttribute>>()
    private val activeData = ConcurrentHashMap<UUID, ConcurrentHashMap<Class<*>, PersistentAttribute>>()

    /**
     * Registers a custom PersistentAttribute class into the system.
     * The class MUST have a primary constructor that takes a single UUID parameter.
     *
     * @param clazz The class type of the attribute to register.
     */
    fun registerAttribute(clazz: Class<out PersistentAttribute>) {
        registeredAttributes.add(clazz)
    }

    /**
     * Retrieves a loaded attribute for a specific player.
     *
     * @param player The target player.
     * @param clazz The class type of the attribute.
     * @return The attribute instance, or null if it isn't loaded/registered.
     */
    fun <T : PersistentAttribute> getAttribute(player: LuxPlayer, clazz: Class<T>): T? {
        val playerMap = activeData[player.uniqueId] ?: return null
        val attribute = playerMap[clazz] ?: return null
        return clazz.cast(attribute)
    }

    /**
     * Internal listener triggered when a player joins the server.
     * Instantiates and asynchronously loads all registered attributes for the player.
     */
    @Subscribe
    internal fun onPlayerJoin(event: PlayerJoinEvent) {
        val uuid = event.player.uniqueId
        val playerMap = ConcurrentHashMap<Class<*>, PersistentAttribute>()
        activeData[uuid] = playerMap

        for (clazz in registeredAttributes) {
            try {
                val constructor = clazz.getDeclaredConstructor(UUID::class.java)
                val instance = constructor.newInstance(uuid)

                playerMap[clazz] = instance
                instance.loadAsync()

            } catch (e: Exception) {
                println("[LuxAPI] Failed to initialize attribute ${clazz.simpleName} for player $uuid.")
                e.printStackTrace()
            }
        }
    }

    /**
     * Internal listener triggered when a player disconnects.
     * Triggers an asynchronous save for all attributes and clears them from memory.
     */
    @Subscribe
    internal fun onPlayerQuit(event: PlayerQuitEvent) {
        val uuid = event.player.uniqueId
        val playerMap = activeData.remove(uuid) ?: return

        for (attribute in playerMap.values) {
            attribute.saveAsync()
        }
    }

    /**
     * Forces a save for all online players.
     * Useful for server shutdowns or periodic global saves.
     */
    fun saveAllOnline() {
        activeData.values.forEach { playerMap ->
            playerMap.values.forEach { it.saveAsync() }
        }
    }
}

/**
 * Reified extension function to easily retrieve a PersistentAttribute from a player.
 *
 * Example usage:
 * val wallet = player.getAttribute<PlayerWallet>()
 * * @return The requested PersistentAttribute, or null if not found.
 */
inline fun <reified T : PersistentAttribute> LuxPlayer.getAttribute(): T? {
    return AttributeManager.getAttribute(this, T::class.java)
}