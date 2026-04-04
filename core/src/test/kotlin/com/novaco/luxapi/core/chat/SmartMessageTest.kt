package com.novaco.luxapi.core.chat

import net.minecraft.SharedConstants
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.HoverEvent
import net.minecraft.server.Bootstrap
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.players.PlayerList
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class SmartMessageTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun initMinecraft() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    @Test
    fun `test message building appends standard text`() {
        val message = SmartMessage()
            .append("&aHello")
            .build()

        // It should contain the formatted string
        assertTrue(message.string.contains("§aHello"))
    }

    @Test
    fun `test hover and click events are attached to styles`() {
        val sword = ItemStack(Items.DIAMOND_SWORD)

        val component = SmartMessage()
            .appendHoverItem("[Sword]", sword)
            .appendClickableCommand("[Click Me]", "/give", "&7Click to receive")
            .build()

        // Extract siblings (the parts we appended to the empty root)
        val siblings = component.siblings
        assertEquals(2, siblings.size, "Should have two distinct appended parts.")

        // Verify Hover Item
        val hoverItemStyle = siblings[0].style
        assertNotNull(hoverItemStyle.hoverEvent, "First part must have a hover event.")
        assertEquals(HoverEvent.Action.SHOW_ITEM, hoverItemStyle.hoverEvent?.action)

        // Verify Click Command + Hover Text
        val clickStyle = siblings[1].style
        assertNotNull(clickStyle.clickEvent, "Second part must have a click event.")
        assertEquals(ClickEvent.Action.RUN_COMMAND, clickStyle.clickEvent?.action)
        assertEquals("/give", clickStyle.clickEvent?.value)
        assertEquals(HoverEvent.Action.SHOW_TEXT, clickStyle.hoverEvent?.action)
    }

    @Test
    fun `test smart message routes directly to player`() {
        val mockPlayer = mock(ServerPlayer::class.java)

        val smartMessage = SmartMessage().append("Direct test")
        smartMessage.send(mockPlayer)

        // Verify the native network message was triggered
        verify(mockPlayer).sendSystemMessage(smartMessage.build())
    }

    @Test
    fun `test smart message routes to global broadcast`() {
        val mockServer = mock(MinecraftServer::class.java)
        val mockPlayerList = mock(PlayerList::class.java)

        // Mock the server so it returns our fake PlayerList
        `when`(mockServer.playerList).thenReturn(mockPlayerList)

        val smartMessage = SmartMessage().append("Broadcast test")
        smartMessage.broadcast(mockServer)

        // Verify the native broadcast system was triggered
        verify(mockPlayerList).broadcastSystemMessage(smartMessage.build(), false)
    }
}