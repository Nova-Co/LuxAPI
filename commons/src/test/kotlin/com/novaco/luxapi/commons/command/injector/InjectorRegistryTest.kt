package com.novaco.luxapi.commons.command.injector

import com.novaco.luxapi.commons.command.injector.impl.IntegerInjector
import com.novaco.luxapi.commons.command.injector.impl.StringInjector
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class InjectorRegistryTest {

    @Test
    fun `test default injectors are registered on initialization`() {
        val stringInjector = InjectorRegistry.getInjector(String::class.java)
        val intInjector = InjectorRegistry.getInjector(Int::class.javaObjectType)

        assertNotNull(stringInjector, "StringInjector should be registered by default.")
        assertNotNull(intInjector, "IntegerInjector should be registered by default.")
    }

    @Test
    fun `test custom injector registration and retrieval`() {
        // Create a dummy class and injector
        class CustomItem(val name: String)

        val customInjector = object : ArgumentInjector<CustomItem> {
            override val convertedClass: Class<CustomItem> = CustomItem::class.java
            override fun instantiate(sender: com.novaco.luxapi.commons.command.sender.CommandSender, args: Array<String>, index: Int): CustomItem? {
                return CustomItem("Test")
            }
        }

        // Register it
        InjectorRegistry.register(customInjector)

        // Retrieve and verify
        val retrieved = InjectorRegistry.getInjector(CustomItem::class.java)
        assertNotNull(retrieved, "Should be able to retrieve a custom registered injector.")
        assertEquals(CustomItem::class.java, retrieved?.convertedClass, "The retrieved injector should match the registered class type.")
    }
}