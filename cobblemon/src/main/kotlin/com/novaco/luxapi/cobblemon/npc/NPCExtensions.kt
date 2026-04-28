package com.novaco.luxapi.cobblemon.npc

import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.novaco.luxapi.commons.player.LuxPlayer

/**
 * Opens a builder context to configure and instantly spawn a universal NPC
 * in front of the player.
 *
 * Example Usage (Trainer):
 * ```
 * player.spawnNPC {
 * name("§cGym Leader Brock")
 * skin("Brock")
 * addPokemon("onix lvl=20")
 * onInteract { p, npc -> BattleManager.startBattle(p, npc) }
 * }
 * ```
 *
 * Example Usage (Story Dialogue):
 * ```
 * player.spawnNPC {
 * name("§bProfessor Oak")
 * skin("Prof_Oak")
 * onInteract { p, npc ->
 * LuxDialogueBuilder()
 * .addPage("start", "oak", "Hello there!")
 * .buildAndOpen(LuxPlayer.of(p), npc)
 * }
 * }
 * ```
 *
 * @param block The configuration block applied to the [LuxNPCBuilder].
 * @return The generated [NPCEntity], or null if spawning failed.
 */
inline fun LuxPlayer.spawnNPC(block: LuxNPCBuilder.() -> Unit): NPCEntity? {
    val builder = LuxNPCBuilder(this)
    builder.block()
    return builder.spawn()
}