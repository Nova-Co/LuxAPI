package com.novaco.luxapi.commons.event

import org.slf4j.LoggerFactory
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * The central dispatcher for the cross-platform event system.
 * Handles the registration of listeners and the broadcasting of events.
 */
object EventBus {

    private val logger = LoggerFactory.getLogger(EventBus::class.java)
    private val listeners = ConcurrentHashMap<Class<out LuxEvent>, MutableList<Pair<Any, Method>>>()

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
                    listeners.computeIfAbsent(eventClass) { CopyOnWriteArrayList() }.add(Pair(listener, method))
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
            } catch (e: InvocationTargetException) {
                logger.error("Listener '{}.{}' threw while handling {}", instance::class.simpleName, method.name, eventClass.simpleName, e)
            } catch (e: IllegalAccessException) {
                logger.error("Listener '{}.{}' threw while handling {}", instance::class.simpleName, method.name, eventClass.simpleName, e)
            } catch (e: IllegalArgumentException) {
                logger.error("Listener '{}.{}' threw while handling {}", instance::class.simpleName, method.name, eventClass.simpleName, e)
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