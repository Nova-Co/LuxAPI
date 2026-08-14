package com.novaco.luxapi.cobblemon.battle

import com.novaco.luxapi.commons.player.LuxPlayer
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class LuxBattleBuilderTest {

    @Test
    fun `setDoubleBattle returns the same builder instance for chaining`() {
        val builder = LuxBattleBuilder(mock<LuxPlayer>())

        val result = builder.setDoubleBattle(true)

        assertSame(builder, result)
    }

    @Test
    fun `setSpectatorAllowed returns the same builder instance for chaining`() {
        val builder = LuxBattleBuilder(mock<LuxPlayer>())

        val result = builder.setSpectatorAllowed(false)

        assertSame(builder, result)
    }

    @Test
    fun `builder methods can be chained fluently`() {
        val builder = LuxBattleBuilder(mock<LuxPlayer>())

        val result = builder.setDoubleBattle(true).setSpectatorAllowed(false)

        assertSame(builder, result)
    }
}
