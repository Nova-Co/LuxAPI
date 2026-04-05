package com.novaco.luxapi.neoforge.command

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
 * A dummy command strictly for testing the Brigadier registration bridge in NeoForge.
 */
@Command(name = "luxforge")
class DummyForgeCommand {
    fun execute(sender: CommandSender) {
        // Fallback required by CommandProcessor
    }
}

class NeoForgeCommandManagerTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun initMinecraft() {
            // Required to initialize Minecraft's internal registries used by Brigadier wrappers
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    @Test
    fun `test command caching before dispatcher is set`() {
        val manager = NeoForgeCommandManager()
        val processor = CommandProcessor(DummyForgeCommand())

        // 1. Register to platform BEFORE the dispatcher is available (simulating early mod load)
        manager.registerToPlatform(processor)

        // 2. Mock a dispatcher to verify delayed registration
        val mockDispatcher = mock<CommandDispatcher<CommandSourceStack>>()
        manager.setDispatcher(mockDispatcher)

        // 3. Verify that setting the dispatcher flushed the cache and registered the command
        verify(mockDispatcher, times(1)).register(any())
    }

    @Test
    fun `test immediate registration if dispatcher is already set`() {
        val manager = NeoForgeCommandManager()
        val mockDispatcher = mock<CommandDispatcher<CommandSourceStack>>()

        // 1. Set dispatcher FIRST (simulating late mod load or reload)
        manager.setDispatcher(mockDispatcher)

        // 2. Register command
        val processor = CommandProcessor(DummyForgeCommand())
        manager.registerToPlatform(processor)

        // 3. Verify it bypassed the cache and registered immediately
        verify(mockDispatcher, times(1)).register(any())
    }

    @Test
    fun `test brigadier executes and routes arguments to processor`() {
        // 1. Setup real dispatcher and our manager
        val dispatcher = CommandDispatcher<CommandSourceStack>()
        val manager = NeoForgeCommandManager()
        manager.setDispatcher(dispatcher)

        // 2. Create a SPY of our CommandProcessor so we can intercept calls to it
        val processorSpy = spy(CommandProcessor(DummyForgeCommand()))
        manager.registerToPlatform(processorSpy)

        // 3. Mock the Minecraft Native Sender
        val mockSource = mock<CommandSourceStack>()
        val mockPlayer = mock<ServerPlayer>()

        whenever(mockSource.isPlayer).thenReturn(true)
        whenever(mockSource.playerOrException).thenReturn(mockPlayer)
        whenever(mockPlayer.scoreboardName).thenReturn("ForgeAdmin")
        whenever(mockPlayer.uuid).thenReturn(java.util.UUID.randomUUID())

        // 4. Simulate a player typing a command with arguments in the game
        dispatcher.execute("luxforge give 64", mockSource)

        // 5. Verify the Brigadier node captured the greedy string and routed it properly
        val argsCaptor = argumentCaptor<Array<String>>()
        verify(processorSpy).process(any(), argsCaptor.capture())

        val capturedArgs = argsCaptor.firstValue
        assertEquals(2, capturedArgs.size, "Should split the greedy argument string.")
        assertEquals("give", capturedArgs[0])
        assertEquals("64", capturedArgs[1])
    }

    @Test
    fun `test non player sources are safely ignored by default`() {
        val dispatcher = CommandDispatcher<CommandSourceStack>()
        val manager = NeoForgeCommandManager()
        manager.setDispatcher(dispatcher)

        val processorSpy = spy(CommandProcessor(DummyForgeCommand()))
        manager.registerToPlatform(processorSpy)

        // Simulate Server Console or Command Block (isPlayer = false)
        val mockSource = mock<CommandSourceStack>()
        whenever(mockSource.isPlayer).thenReturn(false)

        dispatcher.execute("luxforge", mockSource)

        // Verify the processor was NEVER called because sender mapped to null
        verify(processorSpy, never()).process(any(), any())
    }
}