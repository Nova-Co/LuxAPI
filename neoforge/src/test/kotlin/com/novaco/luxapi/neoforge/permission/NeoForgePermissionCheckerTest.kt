package com.novaco.luxapi.neoforge.permission

import com.novaco.luxapi.commons.command.sender.CommandSender
import com.novaco.luxapi.commons.player.LuxPlayer
import com.novaco.luxapi.core.command.MinecraftCommandSender
import net.minecraft.SharedConstants
import net.minecraft.commands.CommandSourceStack
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.Bootstrap
import net.minecraft.server.level.ServerPlayer
import net.neoforged.neoforge.server.permission.PermissionAPI
import net.neoforged.neoforge.server.permission.events.PermissionGatherEvent
import net.neoforged.neoforge.server.permission.handler.IPermissionHandler
import net.neoforged.neoforge.server.permission.nodes.PermissionDynamicContext
import net.neoforged.neoforge.server.permission.nodes.PermissionNode
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.UUID

class NeoForgePermissionCheckerTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun initMinecraft() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    private class FakeHandler(
        private val nodes: Set<PermissionNode<*>>,
        private val answer: (PermissionNode<*>) -> Boolean
    ) : IPermissionHandler {
        override fun getIdentifier(): ResourceLocation = ResourceLocation.fromNamespaceAndPath("luxapi", "test")
        override fun getRegisteredNodes(): Set<PermissionNode<*>> = nodes

        @Suppress("UNCHECKED_CAST")
        override fun <T> getPermission(
            player: ServerPlayer,
            node: PermissionNode<T>,
            vararg context: PermissionDynamicContext<*>
        ): T = answer(node) as T

        @Suppress("UNCHECKED_CAST")
        override fun <T> getOfflinePermission(
            player: UUID,
            node: PermissionNode<T>,
            vararg context: PermissionDynamicContext<*>
        ): T = answer(node) as T
    }

    private val checker = NeoForgePermissionChecker()

    @AfterEach
    fun uninstallHandler() {
        setActiveHandler(null)
    }

    private fun setActiveHandler(handler: IPermissionHandler?) {
        val field = PermissionAPI::class.java.getDeclaredField("activeHandler")
        field.isAccessible = true
        field.set(null, handler)
    }

    private fun gatheredNodes(): Set<PermissionNode<*>> {
        val event = PermissionGatherEvent.Nodes()
        checker.onGatherNodes(event)
        return event.nodes.toSet()
    }

    private fun installHandler(answer: (PermissionNode<*>) -> Boolean) {
        setActiveHandler(FakeHandler(gatheredNodes(), answer))
    }

    private fun player(isOp: Boolean): LuxPlayer {
        val serverPlayer = mock<ServerPlayer>()
        val source = mock<CommandSourceStack>()
        whenever(serverPlayer.createCommandSourceStack()).thenReturn(source)
        whenever(source.hasPermission(4)).thenReturn(isOp)
        return mock<LuxPlayer>().also { whenever(it.parent).thenReturn(serverPlayer) }
    }

    @Test
    fun `provider grant works for a non-operator`() {
        checker.declare("luxapi.test.grant")
        installHandler { true }

        assertTrue(checker.has(player(isOp = false), "luxapi.test.grant"))
    }

    @Test
    fun `provider denial beats operator status`() {
        checker.declare("luxapi.test.deny")
        installHandler { false }

        assertFalse(checker.has(player(isOp = true), "luxapi.test.deny"))
    }

    @Test
    fun `node lookup ignores case`() {
        checker.declare("LuxAPI.Test.Mixed")
        installHandler { true }

        assertTrue(checker.has(player(isOp = false), "luxapi.test.mixed"))
    }

    @Test
    fun `undeclared node falls back to op level 4 without throwing`() {
        installHandler { true }

        assertTrue(checker.has(player(isOp = true), "luxapi.test.never.declared"))
        assertFalse(checker.has(player(isOp = false), "luxapi.test.never.declared"))
    }

    @Test
    fun `node declared after gathering falls back to op level 4`() {
        installHandler { true }
        checker.declare("luxapi.test.too.late")

        assertFalse(checker.has(player(isOp = false), "luxapi.test.too.late"))
        assertTrue(checker.has(player(isOp = true), "luxapi.test.too.late"))
    }

    @Test
    fun `no active permission handler falls back to op level 4`() {
        checker.declare("luxapi.test.early")

        assertTrue(checker.has(player(isOp = true), "luxapi.test.early"))
        assertFalse(checker.has(player(isOp = false), "luxapi.test.early"))
    }

    @Test
    fun `gathering adds one node per declared permission`() {
        checker.declare("luxapi.command.admin")
        checker.declare("luxapi.command.admin")
        checker.declare("othermod.thing")
        checker.declare("noNamespace")

        val names = gatheredNodes().map { it.nodeName }.toSet()

        assertEquals(setOf("luxapi.command.admin", "othermod.thing", "luxapi.nonamespace"), names)
    }

    @Test
    fun `console uses op level 2 because NeoForge has no non-player permission lookup`() {
        val source = mock<CommandSourceStack>()
        whenever(source.entity).thenReturn(null)
        whenever(source.hasPermission(2)).thenReturn(true)

        assertTrue(checker.has(MinecraftCommandSender(source), "luxapi.test.console"))

        whenever(source.hasPermission(2)).thenReturn(false)
        assertFalse(checker.has(MinecraftCommandSender(source), "luxapi.test.console"))
    }

    @Test
    fun `a command source run by a player resolves the node for that player`() {
        checker.declare("luxapi.test.sourceplayer")
        installHandler { true }
        val serverPlayer = mock<ServerPlayer>()
        val source = mock<CommandSourceStack>()
        whenever(source.entity).thenReturn(serverPlayer)
        whenever(serverPlayer.createCommandSourceStack()).thenReturn(source)
        whenever(source.hasPermission(4)).thenReturn(false)

        assertTrue(checker.has(MinecraftCommandSender(source), "luxapi.test.sourceplayer"))
    }

    @Test
    fun `unknown sender types are denied`() {
        assertFalse(checker.has(mock<CommandSender>(), "luxapi.test.grant"))
    }
}
