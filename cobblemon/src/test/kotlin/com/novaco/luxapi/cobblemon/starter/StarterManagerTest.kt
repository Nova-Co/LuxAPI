package com.novaco.luxapi.cobblemon.starter

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.starter.StarterHandler
import com.cobblemon.mod.common.config.starter.StarterCategory
import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
import net.minecraft.server.level.ServerPlayer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class StarterManagerTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun bootstrapMinecraft() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    private val originalHandler = Cobblemon.starterHandler

    @AfterEach
    fun restoreHandler() {
        Cobblemon.starterHandler = originalHandler
    }

    private class RecordingStarterHandler : StarterHandler {
        var requestedFor: ServerPlayer? = null
        var chosenCategory: String? = null
        var chosenIndex: Int? = null

        override fun getStarterList(player: ServerPlayer): List<StarterCategory> = emptyList()
        override fun handleJoin(player: ServerPlayer) {}
        override fun requestStarterChoice(player: ServerPlayer) {
            requestedFor = player
        }
        override fun chooseStarter(player: ServerPlayer, categoryName: String, index: Int) {
            chosenCategory = categoryName
            chosenIndex = index
        }
    }

    @Test
    fun `requestStarterChoice delegates to the current Cobblemon starterHandler`() {
        val handler = RecordingStarterHandler()
        Cobblemon.starterHandler = handler
        val player = mock<ServerPlayer>()

        StarterManager.requestStarterChoice(player)

        assertEquals(player, handler.requestedFor)
    }

    @Test
    fun `chooseStarter delegates to the current Cobblemon starterHandler`() {
        val handler = RecordingStarterHandler()
        Cobblemon.starterHandler = handler
        val player = mock<ServerPlayer>()

        StarterManager.chooseStarter(player, "kanto", 2)

        assertEquals("kanto", handler.chosenCategory)
        assertEquals(2, handler.chosenIndex)
    }

    @Test
    fun `getStarterList delegates to the current Cobblemon starterHandler`() {
        Cobblemon.starterHandler = RecordingStarterHandler()
        val player = mock<ServerPlayer>()

        val result = StarterManager.getStarterList(player)

        assertTrue(result.isEmpty())
    }
}
