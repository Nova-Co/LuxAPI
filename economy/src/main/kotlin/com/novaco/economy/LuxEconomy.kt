package com.novaco.economy

import org.bukkit.plugin.java.JavaPlugin

/**
 * Main plugin class for LuxEC (LuxAPI Economy Module).
 * Acts as the base lifecycle holder for core economy contracts.
 */
class LuxEconomy : JavaPlugin() {

    override fun onEnable() {
        val console = server.consoleSender

        console.sendMessage("")
        console.sendMessage("§3LuxEC §8| §fCore Economy Module")
        console.sendMessage("§8- §7Developer: §bNova Co.")
        console.sendMessage("§8- §7Status: §aEnabled")
        console.sendMessage("")
    }

    override fun onDisable() {
        val console = server.consoleSender
        console.sendMessage(" §3LuxEC §8| §cModule disabled.")
    }
}