package com.novaco.luxapi.commons.command

import com.novaco.luxapi.commons.command.annotation.Command
import com.novaco.luxapi.commons.command.injector.ArgumentInjector
import com.novaco.luxapi.commons.command.injector.CompletingInjector
import com.novaco.luxapi.commons.command.injector.InjectorRegistry
import com.novaco.luxapi.commons.command.sender.CommandSender
import com.novaco.luxapi.commons.command.tab.TabHandler
import com.novaco.luxapi.commons.command.tab.TabRegistry
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Locally-scoped test types, one per test case, kept out of any real command's argument space
 * and never shared across test methods here. [InjectorRegistry]/[TabRegistry] are process-wide
 * singletons with no reset hook, so reusing one type across methods would make behavior depend
 * on JUnit's (unspecified) method execution order.
 */
class WidgetFallback(val name: String)
class WidgetPrecedence(val name: String)
class WidgetPlain(val name: String)

class WidgetCompletingInjector<T>(
    private val convert: (String) -> T,
    override val convertedClass: Class<T>,
    private val known: List<String>
) : CompletingInjector<T> {
    override fun instantiate(sender: CommandSender, args: Array<String>, index: Int): T? {
        return args.getOrNull(index)?.let(convert)
    }

    override fun getSuggestions(sender: CommandSender, args: Array<String>, index: Int): List<String> {
        val partial = args.getOrNull(index)?.lowercase() ?: ""
        return known.filter { it.lowercase().startsWith(partial) }
    }
}

@Command(name = "testfallback")
class FallbackCommand {
    @Suppress("UNUSED_PARAMETER")
    fun execute(sender: CommandSender, target: WidgetFallback) {}
}

@Command(name = "testprecedence")
class PrecedenceCommand {
    @Suppress("UNUSED_PARAMETER")
    fun execute(sender: CommandSender, target: WidgetPrecedence) {}
}

@Command(name = "testplain")
class PlainCommand {
    @Suppress("UNUSED_PARAMETER")
    fun execute(sender: CommandSender, target: WidgetPlain) {}
}

class DummyProcessorSender : CommandSender {
    override val name: String = "Dummy"
    override val uniqueId: java.util.UUID? = null
    override fun sendMessage(message: String) {}
    override fun hasPermission(permission: String): Boolean = true
}

class CommandProcessorTest {

    @Test
    fun `test suggestions fall back to a CompletingInjector when no TabHandler is registered`() {
        InjectorRegistry.register(
            WidgetCompletingInjector(::WidgetFallback, WidgetFallback::class.java, listOf("Alpha", "Avocado", "Beta"))
        )

        val processor = CommandProcessor(FallbackCommand())
        val suggestions = processor.getSuggestions(DummyProcessorSender(), arrayOf("a"))

        assertEquals(setOf("Alpha", "Avocado"), suggestions.toSet())
    }

    @Test
    fun `test an explicit TabRegistry handler takes precedence over the injector fallback`() {
        InjectorRegistry.register(
            WidgetCompletingInjector(::WidgetPrecedence, WidgetPrecedence::class.java, listOf("IgnoredByInjector"))
        )
        TabRegistry.register(WidgetPrecedence::class.java, object : TabHandler {
            override fun getSuggestions(sender: CommandSender, args: Array<String>): List<String> = listOf("FromTabRegistry")
        })

        val processor = CommandProcessor(PrecedenceCommand())
        val suggestions = processor.getSuggestions(DummyProcessorSender(), arrayOf("any"))

        assertEquals(listOf("FromTabRegistry"), suggestions)
    }

    @Test
    fun `test a plain non-completing injector yields no suggestions`() {
        InjectorRegistry.register(object : ArgumentInjector<WidgetPlain> {
            override val convertedClass: Class<WidgetPlain> = WidgetPlain::class.java
            override fun instantiate(sender: CommandSender, args: Array<String>, index: Int): WidgetPlain? {
                return args.getOrNull(index)?.let { WidgetPlain(it) }
            }
        })

        val processor = CommandProcessor(PlainCommand())
        val suggestions = processor.getSuggestions(DummyProcessorSender(), arrayOf("any"))

        assertTrue(suggestions.isEmpty())
    }
}
