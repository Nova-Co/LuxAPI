package com.novaco.luxapi.fabric

import com.mojang.brigadier.CommandDispatcher
import com.novaco.luxapi.commons.LuxAPI
import com.novaco.luxapi.fabric.gui.FabricGuiBuilder
import com.novaco.luxapi.fabric.gui.FabricPaginatedGuiBuilder
import com.novaco.luxapi.fabric.scheduler.FabricLuxScheduler
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.SharedConstants
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.server.Bootstrap
import net.minecraft.server.MinecraftServer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.kotlin.mock

class LuxFabricInitializerTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun initMinecraft() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    @Test
    @Disabled("Requires full Cobblemon mod and Fabric environment to be loaded in the classpath.")
    fun `test fabric initializer registers core providers and hooks gracefully`() {
        val initializer = LuxFabricInitializer()

        // 1. Execute the main initialization hook
        assertDoesNotThrow("onInitialize should execute without throwing any exceptions.") {
            initializer.onInitialize()
        }

        // 2. Verify Cross-Platform API Providers were successfully injected with Fabric implementations
        val guiBuilder = LuxAPI.guiProvider?.invoke()
        assertTrue(guiBuilder is FabricGuiBuilder, "GUI Provider must be injected with FabricGuiBuilder.")

        val paginatedGuiBuilder = LuxAPI.paginatedGuiProvider?.invoke()
        assertTrue(paginatedGuiBuilder is FabricPaginatedGuiBuilder, "Paginated GUI Provider must be injected with FabricPaginatedGuiBuilder.")

        val scheduler = LuxAPI.schedulerProvider?.invoke()
        assertTrue(scheduler is FabricLuxScheduler, "Scheduler Provider must be injected with FabricLuxScheduler.")

        // 3. Verify Command Registration Hook
        val mockDispatcher = mock<CommandDispatcher<CommandSourceStack>>()
        val mockBuildContext = mock<CommandBuildContext>()
        val environment = Commands.CommandSelection.ALL

        // Simulate Fabric firing the Command Registration event
        assertDoesNotThrow("Command registration hook should bind safely to the manager.") {
            CommandRegistrationCallback.EVENT.invoker().register(mockDispatcher, mockBuildContext, environment)
        }

        // 4. Verify Server Starting Hook (Player Injector Setup)
        val mockServer = mock<MinecraftServer>()

        // Simulate Fabric firing the Server Starting event
        assertDoesNotThrow("Server starting hook should configure the PlayerManager and Injector.") {
            ServerLifecycleEvents.SERVER_STARTING.invoker().onServerStarting(mockServer)
        }
    }
}