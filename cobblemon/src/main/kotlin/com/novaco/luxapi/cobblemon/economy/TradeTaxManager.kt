package com.novaco.luxapi.cobblemon.economy

import java.util.UUID

/**
 * Extensible tax hook for GTS purchases. Every registered [TradeTaxCalculator] runs in
 * registration order, each seeing the running tax total from the previous one.
 */
object TradeTaxManager {

    private val calculators = mutableListOf<TradeTaxCalculator>()

    /**
     * Registers a custom tax rule into the global tax chain.
     */
    fun registerTaxCalculator(calculator: TradeTaxCalculator) {
        calculators.add(calculator)
    }

    /**
     * Folds [price] through every registered [TradeTaxCalculator] and returns the final tax
     * amount, clamped to `[0.0, price]` so a misconfigured rule can never invert or exceed
     * the sale price.
     */
    fun calculateTax(sellerUuid: UUID, price: Double): Double {
        var tax = 0.0
        for (calculator in calculators) {
            tax = calculator.calculateTax(sellerUuid, price, tax)
        }
        return tax.coerceIn(0.0, price)
    }
}

/**
 * Functional interface for custom developer tax rules.
 */
fun interface TradeTaxCalculator {
    fun calculateTax(sellerUuid: UUID, price: Double, currentTax: Double): Double
}
