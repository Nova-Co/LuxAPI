package com.novaco.luxapi.core.command

import net.minecraft.SharedConstants
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component
import net.minecraft.server.Bootstrap
import net.minecraft.world.entity.player.Player
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.*
import java.util.UUID

class MinecraftCommandSenderTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun initMinecraft() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    @Test
    fun `test player sender properties`() {
        // 1. Mock the native objects
        val mockSource = mock(CommandSourceStack::class.java)
        val mockPlayer = mock(Player::class.java)
        val playerUuid = UUID.randomUUID()

        // 2. Define their behavior
        `when`(mockSource.entity).thenReturn(mockPlayer)
        `when`(mockPlayer.uuid).thenReturn(playerUuid)
        `when`(mockSource.textName).thenReturn("NovacoAdmin")

        // 3. Wrap it
        val sender = MinecraftCommandSender(mockSource)

        // 4. Verify properties
        assertEquals("NovacoAdmin", sender.name)
        assertEquals(playerUuid, sender.uniqueId)
        assertTrue(sender.isPlayer(), "Sender should be identified as a physical player.")
        assertNotNull(sender.getPlayer(), "Casting to Player should succeed.")
    }

    @Test
    fun `test console sender properties`() {
        val mockSource = mock(CommandSourceStack::class.java)

        // Console has no entity attached
        `when`(mockSource.entity).thenReturn(null)
        `when`(mockSource.textName).thenReturn("Server")

        val sender = MinecraftCommandSender(mockSource)

        assertEquals("Server", sender.name)
        assertNull(sender.uniqueId, "Console should have a null UUID.")
        assertFalse(sender.isPlayer(), "Console is not a player.")
        assertNull(sender.getPlayer(), "Casting console to Player should return null.")
    }

    @Test
    fun `test send message translates color codes`() {
        val mockSource = mock(CommandSourceStack::class.java)
        val sender = MinecraftCommandSender(mockSource)

        sender.sendMessage("&aSuccess!")

        // Capture the component passed to the native send method
        val componentCaptor = ArgumentCaptor.forClass(Component::class.java)
        verify(mockSource).sendSystemMessage(componentCaptor.capture())

        val capturedComponent = componentCaptor.value
        assertEquals("§aSuccess!", capturedComponent.string, "Legacy color codes should be translated before hitting the native server.")
    }

    @Test
    fun `test permission check delegates to source stack level 2`() {
        val mockSource = mock(CommandSourceStack::class.java)
        val sender = MinecraftCommandSender(mockSource)

        `when`(mockSource.hasPermission(2)).thenReturn(true)
        assertTrue(sender.hasPermission("luxapi.admin"))

        `when`(mockSource.hasPermission(2)).thenReturn(false)
        assertFalse(sender.hasPermission("luxapi.admin"))
    }
}