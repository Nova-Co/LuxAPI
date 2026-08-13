package com.novaco.luxapi.cobblemon.permission

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.api.permission.CobblemonPermissions
import com.cobblemon.mod.common.api.permission.PermissionValidator
import net.minecraft.SharedConstants
import net.minecraft.server.Bootstrap
import net.minecraft.server.level.ServerPlayer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class PermissionManagerTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun bootstrapMinecraft() {
            SharedConstants.tryDetectVersion()
            Bootstrap.bootStrap()
        }
    }

    private lateinit var originalValidator: PermissionValidator

    // Cobblemon.permissionValidator is a shared global var — swap it for a mock per
    // test and restore afterward so this doesn't leak state into other test classes.
    @BeforeEach
    fun captureValidator() {
        originalValidator = Cobblemon.permissionValidator
    }

    @AfterEach
    fun restoreValidator() {
        Cobblemon.permissionValidator = originalValidator
    }

    @Test
    fun `allNodes exposes Cobblemon's own built-in permission nodes`() {
        assertTrue(PermissionManager.allNodes().toList().contains(CobblemonPermissions.PC))
    }

    @Test
    fun `hasPermission by Permission delegates to the installed validator`() {
        val validator = mock<PermissionValidator>()
        Cobblemon.permissionValidator = validator
        val player = mock<ServerPlayer>()
        whenever(validator.hasPermission(player, CobblemonPermissions.PC)).thenReturn(true)

        val result = PermissionManager.hasPermission(player, CobblemonPermissions.PC)

        assertTrue(result)
    }

    @Test
    fun `hasPermission by raw node string delegates to the installed validator`() {
        val validator = mock<PermissionValidator>()
        Cobblemon.permissionValidator = validator
        val player = mock<ServerPlayer>()
        whenever(validator.hasPermission(player, "lux.custom.node", 2)).thenReturn(false)

        val result = PermissionManager.hasPermission(player, "lux.custom.node", 2)

        assertFalse(result)
    }
}
