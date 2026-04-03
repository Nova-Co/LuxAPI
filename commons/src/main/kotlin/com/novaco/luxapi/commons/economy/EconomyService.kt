package com.novaco.luxapi.commons.economy

import com.novaco.luxapi.commons.player.LuxPlayer
import java.util.UUID

/**
 * A platform-agnostic interface for handling economy transactions.
 * Servers can implement this using Vault (Paper), Impactor (Fabric), or their own custom economy.
 */
interface EconomyService {

    /** Checks if a player has at least the specified amount of funds. */
    fun hasEnough(player: LuxPlayer, amount: Double): Boolean

    /** Withdraws money from an online player. Returns true if successful. */
    fun withdraw(player: LuxPlayer, amount: Double): Boolean

    /** Deposits money into an account (can be an offline player via UUID). Returns true if successful. */
    fun deposit(uuid: UUID, amount: Double): Boolean

    /** Gets the current balance of a player. */
    fun getBalance(player: LuxPlayer): Double
}