package com.novaco.luxapi.fabric.permission

import com.novaco.luxapi.commons.command.sender.CommandSender
import com.novaco.luxapi.commons.player.LuxPlayer
import com.novaco.luxapi.core.command.MinecraftCommandSender
import me.lucko.fabric.api.permissions.v0.PermissionCheckEvent
import net.fabricmc.fabric.api.util.TriState
import net.minecraft.SharedConstants
import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.Bootstrap
import net.minecraft.server.level.ServerPlayer
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class FabricPermissionCheckerTest {

    companion object {
        private const val DENIED_BY_PROVIDER = "luxapi.test.provider.denied"
        private const val GRANTED_BY_PROVIDER = "luxapi.test.provider.granted"

        @JvmStatic
        @BeforeAll
        fun initMinecraft() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
            PermissionCheckEvent.EVENT.register { _, node ->
                when (node) {
                    DENIED_BY_PROVIDER -> TriState.FALSE
                    GRANTED_BY_PROVIDER -> TriState.TRUE
                    else -> TriState.DEFAULT
                }
            }
        }
    }

    private val checker = FabricPermissionChecker()

    private fun playerSender(opLevel4: Boolean): LuxPlayer {
        val serverPlayer = mock<ServerPlayer>()
        val source = mock<CommandSourceStack>()
        whenever(serverPlayer.createCommandSourceStack()).thenReturn(source)
        whenever(source.hasPermission(4)).thenReturn(opLevel4)
        return mock<LuxPlayer>().also { whenever(it.parent).thenReturn(serverPlayer) }
    }

    private fun consoleSender(opLevel2: Boolean): MinecraftCommandSender {
        val source = mock<CommandSourceStack>()
        whenever(source.entity).thenReturn(null)
        whenever(source.hasPermission(2)).thenReturn(opLevel2)
        return MinecraftCommandSender(source)
    }

    @Test
    fun `player falls back to op level 4 when no provider has an opinion`() {
        assertTrue(checker.has(playerSender(opLevel4 = true), "luxapi.test.nobody.knows"))
        assertFalse(checker.has(playerSender(opLevel4 = false), "luxapi.test.nobody.knows"))
    }

    @Test
    fun `provider denial beats operator status`() {
        assertFalse(checker.has(playerSender(opLevel4 = true), DENIED_BY_PROVIDER))
    }

    @Test
    fun `provider grant works for a non-operator`() {
        assertTrue(checker.has(playerSender(opLevel4 = false), GRANTED_BY_PROVIDER))
    }

    @Test
    fun `console falls back to op level 2 when no provider has an opinion`() {
        assertTrue(checker.has(consoleSender(opLevel2 = true), "luxapi.test.nobody.knows"))
        assertFalse(checker.has(consoleSender(opLevel2 = false), "luxapi.test.nobody.knows"))
    }

    @Test
    fun `provider rules apply to console and command blocks too`() {
        assertFalse(checker.has(consoleSender(opLevel2 = true), DENIED_BY_PROVIDER))
        assertTrue(checker.has(consoleSender(opLevel2 = false), GRANTED_BY_PROVIDER))
    }

    @Test
    fun `unknown sender types are denied`() {
        val stranger = mock<CommandSender>()

        assertFalse(checker.has(stranger, GRANTED_BY_PROVIDER))
    }
}
