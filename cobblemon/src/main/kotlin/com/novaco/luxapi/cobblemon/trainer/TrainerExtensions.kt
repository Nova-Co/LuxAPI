package com.novaco.luxapi.cobblemon.trainer

import com.cobblemon.mod.common.entity.npc.NPCEntity
import com.novaco.luxapi.commons.player.LuxPlayer

/**
 * Extension functions to streamline the creation and spawning of NPC Trainers.
 * Provides a highly readable, Kotlin DSL (Domain Specific Language) syntax.
 */

/**
 * Opens a builder context to configure and instantly spawn a custom NPC Trainer
 * in front of the player.
 *
 * Usage:
 * player.spawnTrainer {
 * setName("§cGym Leader Brock")
 * addPokemon("onix lvl=20")
 * addPokemon("geodude lvl=18")
 * }
 *
 * @param block The configuration block applied to the [TrainerBuilder].
 * @return The generated [NPCEntity], or null if spawning failed.
 */
inline fun LuxPlayer.spawnTrainer(block: TrainerBuilder.() -> Unit): NPCEntity? {
    val builder = TrainerBuilder(this)
    builder.block()
    return builder.spawn()
}