package com.novaco.luxapi.commons.event

import java.lang.reflect.Method

/**
 * The central dispatcher for the cross-platform event system.
 * Handles the registration of listeners and the broadcasting of events.
 */
object EventBus {

    private val listeners = mutableMapOf<Class<out LuxEvent>, MutableList<Pair<Any, Method>>>()

    /**
     * Registers an object containing methods annotated with @Subscribe.
     */
    fun register(listener: Any) {
        listener.javaClass.declaredMethods.forEach { method ->
            if (method.isAnnotationPresent(Subscribe::class.java) && method.parameterCount == 1) {
                val eventType = method.parameterTypes[0]
                if (LuxEvent::class.java.isAssignableFrom(eventType)) {
                    @Suppress("UNCHECKED_CAST")
                    val eventClass = eventType as Class<out LuxEvent>

                    method.isAccessible = true
                    listeners.computeIfAbsent(eventClass) { mutableListOf() }.add(Pair(listener, method))
                }
            }
        }
    }

    /**
     * Unregisters a specific listener object from all events.
     */
    fun unregister(listener: Any) {
        listeners.values.forEach { list ->
            list.removeIf { it.first == listener }
        }
    }

    /**
     * Dispatches an event to all registered listeners listening for this specific event type.
     */
    fun fire(event: LuxEvent) {
        val eventClass = event.javaClass
        listeners[eventClass]?.forEach { (instance, method) ->
            try {
                method.invoke(instance, event)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Clears all registered listeners.
     * Highly recommended for unit testing teardowns.
     */
    fun clear() {
        listeners.clear()
    }
}