package com.novaco.luxapi.fabric.command

import com.mojang.brigadier.CommandDispatcher
import com.novaco.luxapi.commons.command.CommandProcessor
import com.novaco.luxapi.commons.command.annotation.Command
import com.novaco.luxapi.commons.command.sender.CommandSender
import net.minecraft.SharedConstants
import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.Bootstrap
import net.minecraft.server.level.ServerPlayer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

/**
 * A dummy command strictly for testing the Brigadier registration bridge in Fabric.
 */
@Command(name = "luxfabric")
class DummyFabricCommand {

    /**
     * Fallback execution method required by CommandProcessor.
     */
    fun execute(sender: CommandSender) {
    }
}

/**
 * Unit tests for the FabricCommandManager, verifying caching, execution,
 * and the updated Brigadier argument dropping logic.
 */
class FabricCommandManagerTest {

    companion object {
        /**
         * Initializes Minecraft's internal registries required by Brigadier wrappers.
         */
        @JvmStatic
        @BeforeAll
        fun initMinecraft() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    /**
     * Tests that the command processor is cached if registered before the dispatcher is available.
     */
    @Test
    fun `test command caching before dispatcher is set`() {
        val manager = FabricCommandManager()
        val processor = CommandProcessor(DummyFabricCommand())

        manager.registerToPlatform(processor)

        val mockDispatcher = mock<CommandDispatcher<CommandSourceStack>>()
        manager.setDispatcher(mockDispatcher)

        verify(mockDispatcher, times(1)).register(any())
    }

    /**
     * Tests that the command processor is registered immediately if the dispatcher is already set.
     */
    @Test
    fun `test immediate registration if dispatcher is already set`() {
        val manager = FabricCommandManager()
        val mockDispatcher = mock<CommandDispatcher<CommandSourceStack>>()

        manager.setDispatcher(mockDispatcher)

        val processor = CommandProcessor(DummyFabricCommand())
        manager.registerToPlatform(processor)

        verify(mockDispatcher, times(1)).register(any())
    }

    /**
     * Tests that Brigadier correctly executes the command and drops the subcommand argument
     * before routing to the processor.
     */
    @Test
    fun `test brigadier executes and routes arguments to processor`() {
        val dispatcher = CommandDispatcher<CommandSourceStack>()
        val manager = FabricCommandManager()
        manager.setDispatcher(dispatcher)

        val processorSpy = spy(CommandProcessor(DummyFabricCommand()))
        manager.registerToPlatform(processorSpy)

        val mockSource = mock<CommandSourceStack>()
        val mockPlayer = mock<ServerPlayer>()

        whenever(mockSource.isPlayer).thenReturn(true)
        whenever(mockSource.playerOrException).thenReturn(mockPlayer)
        whenever(mockPlayer.scoreboardName).thenReturn("Admin")
        whenever(mockPlayer.uuid).thenReturn(java.util.UUID.randomUUID())

        dispatcher.execute("luxfabric give 100", mockSource)

        val argsCaptor = argumentCaptor<Array<String>>()
        verify(processorSpy).process(any(), argsCaptor.capture())

        val capturedArgs = argsCaptor.firstValue
        assertEquals(1, capturedArgs.size)
        assertEquals("100", capturedArgs[0])
    }

    /**
     * Tests that command executions from non-player sources are safely ignored.
     */
    @Test
    fun `test non player sources are safely ignored by default`() {
        val dispatcher = CommandDispatcher<CommandSourceStack>()
        val manager = FabricCommandManager()
        manager.setDispatcher(dispatcher)

        val processorSpy = spy(CommandProcessor(DummyFabricCommand()))
        manager.registerToPlatform(processorSpy)

        val mockSource = mock<CommandSourceStack>()
        whenever(mockSource.isPlayer).thenReturn(false)

        dispatcher.execute("luxfabric", mockSource)

        verify(processorSpy, never()).process(any(), any())
    }
}