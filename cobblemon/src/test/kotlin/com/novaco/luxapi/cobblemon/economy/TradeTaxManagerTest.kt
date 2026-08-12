package com.novaco.luxapi.cobblemon.economy

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

class TradeTaxManagerTest {

    @BeforeEach
    fun clearCalculators() {
        val field = TradeTaxManager::class.java.getDeclaredField("calculators")
        field.isAccessible = true

        @Suppress("UNCHECKED_CAST")
        val list = field.get(TradeTaxManager) as MutableList<TradeTaxCalculator>
        list.clear()
    }

    @Test
    fun `no calculators registered means zero tax`() {
        val tax = TradeTaxManager.calculateTax(UUID.randomUUID(), 100.0)

        assertEquals(0.0, tax)
    }

    @Test
    fun `single calculator applies a flat percentage`() {
        TradeTaxManager.registerTaxCalculator { _, price, _ -> price * 0.10 }

        val tax = TradeTaxManager.calculateTax(UUID.randomUUID(), 200.0)

        assertEquals(20.0, tax)
    }

    @Test
    fun `multiple calculators fold in registration order`() {
        TradeTaxManager.registerTaxCalculator { _, price, _ -> price * 0.10 }
        TradeTaxManager.registerTaxCalculator { _, _, currentTax -> currentTax + 5.0 }

        val tax = TradeTaxManager.calculateTax(UUID.randomUUID(), 200.0)

        assertEquals(25.0, tax)
    }

    @Test
    fun `tax is clamped to price when a calculator over-taxes`() {
        TradeTaxManager.registerTaxCalculator { _, price, _ -> price * 5.0 }

        val tax = TradeTaxManager.calculateTax(UUID.randomUUID(), 100.0)

        assertEquals(100.0, tax)
    }

    @Test
    fun `tax is clamped to zero when a calculator goes negative`() {
        TradeTaxManager.registerTaxCalculator { _, _, _ -> -50.0 }

        val tax = TradeTaxManager.calculateTax(UUID.randomUUID(), 100.0)

        assertEquals(0.0, tax)
    }
}
