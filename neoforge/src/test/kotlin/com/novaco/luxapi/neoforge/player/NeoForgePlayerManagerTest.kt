package com.novaco.luxapi.neoforge.player

import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ClientInformation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.players.PlayerList
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import java.util.UUID

/**
 * Unit tests for the NeoForgePlayerManager, validating player retrieval and caching mechanisms.
 */
class NeoForgePlayerManagerTest {

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
     * Tests the retrieval of a player by their exact username, checking both cached and native states.
     */
    @Test
    fun `test get player by name`() {
        val mockServer = mock<MinecraftServer>()
        val mockPlayerList = mock<PlayerList>()
        val mockPlayer = mock<ServerPlayer>()
        val mockClientInfo = mock<ClientInformation>()

        whenever(mockPlayer.scoreboardName).thenReturn("ValidForgePlayer")
        whenever(mockPlayer.uuid).thenReturn(UUID.randomUUID())
        whenever(mockClientInfo.language()).thenReturn("en_us")
        whenever(mockPlayer.clientInformation()).thenReturn(mockClientInfo)

        whenever(mockServer.playerList).thenReturn(mockPlayerList)
        whenever(mockPlayerList.getPlayerByName("ValidForgePlayer")).thenReturn(mockPlayer)
        whenever(mockPlayerList.getPlayerByName("OfflinePlayer")).thenReturn(null)

        val manager = NeoForgePlayerManager(mockServer)
        val found = manager.getPlayer("ValidForgePlayer")

        assertNotNull(found)
        assertEquals(mockPlayer, found?.parent)

        val notFound = manager.getPlayer("OfflinePlayer")
        assertNull(notFound)
    }

    /**
     * Tests the retrieval of a player by their unique identifier.
     */
    @Test
    fun `test get player by uuid`() {
        val mockServer = mock<MinecraftServer>()
        val mockPlayerList = mock<PlayerList>()
        val mockPlayer = mock<ServerPlayer>()
        val mockClientInfo = mock<ClientInformation>()
        val targetUuid = UUID.randomUUID()
        val missingUuid = UUID.randomUUID()

        whenever(mockPlayer.scoreboardName).thenReturn("TargetPlayer")
        whenever(mockPlayer.uuid).thenReturn(targetUuid)
        whenever(mockClientInfo.language()).thenReturn("en_us")
        whenever(mockPlayer.clientInformation()).thenReturn(mockClientInfo)

        whenever(mockServer.playerList).thenReturn(mockPlayerList)
        whenever(mockPlayerList.getPlayer(targetUuid)).thenReturn(mockPlayer)
        whenever(mockPlayerList.getPlayer(missingUuid)).thenReturn(null)

        val manager = NeoForgePlayerManager(mockServer)

        assertNotNull(manager.getPlayer(targetUuid))
        assertNull(manager.getPlayer(missingUuid))
    }

    /**
     * Tests that retrieving all online players returns a properly mapped list of LuxPlayers.
     */
    @Test
    fun `test get online players returns mapped list`() {
        val mockServer = mock<MinecraftServer>()
        val mockPlayerList = mock<PlayerList>()
        val mockClientInfo = mock<ClientInformation>()

        whenever(mockClientInfo.language()).thenReturn("en_us")

        val p1 = mock<ServerPlayer> {
            on { scoreboardName } doReturn "Forge1"
            on { uuid } doReturn UUID.randomUUID()
            on { clientInformation() } doReturn mockClientInfo
        }
        val p2 = mock<ServerPlayer> {
            on { scoreboardName } doReturn "Forge2"
            on { uuid } doReturn UUID.randomUUID()
            on { clientInformation() } doReturn mockClientInfo
        }
        val p3 = mock<ServerPlayer> {
            on { scoreboardName } doReturn "Forge3"
            on { uuid } doReturn UUID.randomUUID()
            on { clientInformation() } doReturn mockClientInfo
        }

        val nativePlayers = listOf(p1, p2, p3)

        whenever(mockServer.playerList).thenReturn(mockPlayerList)
        whenever(mockPlayerList.players).thenReturn(nativePlayers)

        val manager = NeoForgePlayerManager(mockServer)
        val onlinePlayers = manager.getOnlinePlayers()

        assertEquals(3, onlinePlayers.size)
        assertTrue(onlinePlayers.all { it is NeoForgeLuxPlayer })
        assertEquals(nativePlayers[0], onlinePlayers[0].parent)
    }
}