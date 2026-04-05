package com.novaco.luxapi.neoforge

import com.mojang.brigadier.CommandDispatcher
import com.novaco.luxapi.commons.LuxAPI
import com.novaco.luxapi.neoforge.gui.NeoForgeGuiBuilder
import com.novaco.luxapi.neoforge.gui.NeoForgePaginatedGuiBuilder
import com.novaco.luxapi.neoforge.scheduler.NeoForgeLuxScheduler
import net.minecraft.SharedConstants
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.server.Bootstrap
import net.minecraft.server.MinecraftServer
import net.neoforged.bus.api.IEventBus
import net.neoforged.neoforge.event.RegisterCommandsEvent
import net.neoforged.neoforge.event.server.ServerStartingEvent
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class LuxNeoForgeInitializerTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun initMinecraft() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    @Test
    @Disabled("Requires full Cobblemon mod and NeoForge environment to be loaded in the classpath.")
    fun `test neoforge initializer registers core providers and hooks gracefully`() {
        // 1. Mock the Mod Event Bus provided by NeoForge
        val mockModBus = mock<IEventBus>()

        // 2. Execute the main initialization hook
        val initializer = LuxNeoForgeInitializer(mockModBus)

        // 3. Verify Cross-Platform API Providers were successfully injected with NeoForge implementations
        val guiBuilder = LuxAPI.guiProvider?.invoke()
        assertTrue(guiBuilder is NeoForgeGuiBuilder, "GUI Provider must be injected with NeoForgeGuiBuilder.")

        val paginatedGuiBuilder = LuxAPI.paginatedGuiProvider?.invoke()
        assertTrue(paginatedGuiBuilder is NeoForgePaginatedGuiBuilder, "Paginated GUI Provider must be injected with NeoForgePaginatedGuiBuilder.")

        val scheduler = LuxAPI.schedulerProvider?.invoke()
        assertTrue(scheduler is NeoForgeLuxScheduler, "Scheduler Provider must be injected with NeoForgeLuxScheduler.")

        // 4. Verify Command Registration Hook via Reflection (Bypassing private visibility)
        val mockDispatcher = mock<CommandDispatcher<CommandSourceStack>>()
        val mockBuildContext = mock<CommandBuildContext>()
        val environment = Commands.CommandSelection.ALL

        val registerEvent = RegisterCommandsEvent(mockDispatcher, environment, mockBuildContext)

        val commandMethod = LuxNeoForgeInitializer::class.java.getDeclaredMethod("onRegisterCommands", RegisterCommandsEvent::class.java)
        commandMethod.isAccessible = true

        assertDoesNotThrow("Command registration hook should bind safely to the manager.") {
            commandMethod.invoke(initializer, registerEvent)
        }

        // 5. Verify Server Starting Hook (Player Injector Setup) via Reflection
        val mockServer = mock<MinecraftServer>()
        val mockStartingEvent = mock<ServerStartingEvent>()
        whenever(mockStartingEvent.server).thenReturn(mockServer)

        val startingMethod = LuxNeoForgeInitializer::class.java.getDeclaredMethod("onServerStarting", ServerStartingEvent::class.java)
        startingMethod.isAccessible = true

        assertDoesNotThrow("Server starting hook should configure the PlayerManager and Injector.") {
            startingMethod.invoke(initializer, mockStartingEvent)
        }
    }
}