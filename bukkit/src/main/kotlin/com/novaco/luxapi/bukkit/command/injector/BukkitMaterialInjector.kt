package com.novaco.luxapi.bukkit.command.injector

import com.novaco.luxapi.commons.command.exception.CommandParseException
import com.novaco.luxapi.commons.command.injector.CompletingInjector
import com.novaco.luxapi.commons.command.sender.CommandSender
import org.bukkit.Material

/**
 * Platform-specific argument injector for Bukkit's native [Material] enum.
 * Resolves a material name argument (case-insensitive, legacy names supported)
 * into the matching [Material] constant.
 */
class BukkitMaterialInjector : CompletingInjector<Material> {

    override val convertedClass: Class<Material> = Material::class.java

    override fun instantiate(sender: CommandSender, args: Array<String>, index: Int): Material {
        val materialName = args.getOrNull(index)
            ?: throw CommandParseException("§cError: Please specify a material.")

        return Material.matchMaterial(materialName)
            ?: throw CommandParseException("§cError: Unknown material '$materialName'.")
    }

    override fun getSuggestions(sender: CommandSender, args: Array<String>, index: Int): List<String> {
        val partial = args.getOrNull(index)?.lowercase() ?: ""
        return Material.entries
            .map { it.name }
            .filter { it.lowercase().startsWith(partial) }
    }
}
