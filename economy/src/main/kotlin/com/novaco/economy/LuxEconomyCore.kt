package com.novaco.economy

import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * The core interface for the LuxEconomy module.
 * This serves as the blueprint for economy transactions within LuxAPI.
 * External bridge plugins (e.g., LuxCEB) must implement this interface
 * and register it to handle the actual economy logic (e.g., via Vault).
 */
interface LuxEconomyCore {

    /**
     * Gets the current balance of the specified player.
     *
     * @param playerUuid The UUID of the player.
     * @return The current balance of the player.
     */
    fun getBalance(playerUuid: UUID): Double

    /**
     * Deposits the specified amount into the player's account.
     *
     * @param playerUuid The UUID of the player.
     * @param amount The amount to deposit.
     * @return A CompletableFuture containing true if the transaction was successful, false otherwise.
     */
    fun deposit(playerUuid: UUID, amount: Double): CompletableFuture<Boolean>

    /**
     * Withdraws the specified amount from the player's account.
     *
     * @param playerUuid The UUID of the player.
     * @param amount The amount to withdraw.
     * @return A CompletableFuture containing true if the transaction was successful, false otherwise.
     */
    fun withdraw(playerUuid: UUID, amount: Double): CompletableFuture<Boolean>

    /**
     * Checks if the player has at least the specified amount of money.
     *
     * @param playerUuid The UUID of the player.
     * @param amount The amount to check against.
     * @return True if the player has enough money, false otherwise.
     */
    fun hasEnough(playerUuid: UUID, amount: Double): Boolean {
        return getBalance(playerUuid) >= amount
    }
}