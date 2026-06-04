package com.novaco.luxapi.neoforge.command

import com.mojang.brigadier.CommandDispatcher
import com.novaco.luxapi.commons.command.CommandProcessor
import com.novaco.luxapi.commons.command.annotation.Command
import com.novaco.luxapi.commons.command.sender.CommandSender
import net.minecraft.SharedConstants
import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.Bootstrap
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.ClientInformation
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.util.UUID

/**
 * A dummy command strictly for testing the Brigadier registration bridge in NeoForge.
 */
@Command(name = "luxforge")
class DummyForgeCommand {

    /**
     * Fallback execution method required by CommandProcessor.
     */
    fun execute(sender: CommandSender) {
    }
}

/**
 * Unit tests for the NeoForgeCommandManager, ensuring proper caching, execution,
 * and the updated Brigadier full argument routing logic.
 */
class NeoForgeCommandManagerTest {

    companion object {
        /**
         * Initializes Minecraft's internal registries used by Brigadier wrappers.
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
        val manager = NeoForgeCommandManager()
        val processor = CommandProcessor(DummyForgeCommand())

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
        val manager = NeoForgeCommandManager()
        val mockDispatcher = mock<CommandDispatcher<CommandSourceStack>>()

        manager.setDispatcher(mockDispatcher)

        val processor = CommandProcessor(DummyForgeCommand())
        manager.registerToPlatform(processor)

        verify(mockDispatcher, times(1)).register(any())
    }

    /**
     * Tests that Brigadier correctly executes the command and routes all arguments
     * directly to the processor without dropping the subcommand.
     */
    @Test
    fun `test brigadier executes and routes arguments to processor`() {
        val dispatcher = CommandDispatcher<CommandSourceStack>()
        val manager = NeoForgeCommandManager()
        manager.setDispatcher(dispatcher)

        val processorSpy = spy(CommandProcessor(DummyForgeCommand()))
        manager.registerToPlatform(processorSpy)

        val mockSource = mock<CommandSourceStack>()
        val mockPlayer = mock<ServerPlayer>()
        val mockClientInfo = mock<ClientInformation>()

        whenever(mockSource.isPlayer).thenReturn(true)
        whenever(mockSource.playerOrException).thenReturn(mockPlayer)
        whenever(mockPlayer.scoreboardName).thenReturn("ForgeAdmin")
        whenever(mockPlayer.uuid).thenReturn(UUID.randomUUID())
        whenever(mockPlayer.clientInformation()).thenReturn(mockClientInfo)

        dispatcher.execute("luxforge give 64", mockSource)

        val argsCaptor = argumentCaptor<Array<String>>()
        verify(processorSpy).process(any(), argsCaptor.capture())

        val capturedArgs = argsCaptor.firstValue
        assertEquals(2, capturedArgs.size)
        assertEquals("give", capturedArgs[0])
        assertEquals("64", capturedArgs[1])
    }

    /**
     * Tests that command executions from non-player sources are safely ignored.
     */
    @Test
    fun `test non player sources are safely ignored by default`() {
        val dispatcher = CommandDispatcher<CommandSourceStack>()
        val manager = NeoForgeCommandManager()
        manager.setDispatcher(dispatcher)

        val processorSpy = spy(CommandProcessor(DummyForgeCommand()))
        manager.registerToPlatform(processorSpy)

        val mockSource = mock<CommandSourceStack>()
        whenever(mockSource.isPlayer).thenReturn(false)

        dispatcher.execute("luxforge", mockSource)

        verify(processorSpy, never()).process(any(), any())
    }
}