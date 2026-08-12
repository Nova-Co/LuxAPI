package com.novaco.luxapi.cobblemon.npc.extensions

import com.cobblemon.mod.common.entity.npc.NPCEntity
import net.minecraft.core.registries.BuiltInRegistries

/**
 * Extension functions for triggering NPC animations and battle-theme cues
 * without needing to touch Cobblemon's raw packet or MoLang APIs directly.
 *
 * Note: a free-form `playAnimation(String, List<String>)` and a `playSound(SoundEvent, Float, Float)`
 * wrapper were deliberately NOT added here — `NPCEntity` already exposes both as public members
 * (`playAnimation` via Cobblemon's `PosableEntity` interface, `playSound` via vanilla `Entity`), so a
 * same-signature extension would just be dead, shadowed code. Call those directly on the entity.
 */

/**
 * The 4 battle-related animations Cobblemon itself plays internally
 * (see [NPCEntity.SEND_OUT_ANIMATION], [NPCEntity.RECALL_ANIMATION],
 * [NPCEntity.WIN_ANIMATION], [NPCEntity.LOSE_ANIMATION]).
 */
enum class BattleAnimation(val key: String) {
    SEND_OUT(NPCEntity.SEND_OUT_ANIMATION),
    RECALL(NPCEntity.RECALL_ANIMATION),
    WIN(NPCEntity.WIN_ANIMATION),
    LOSE(NPCEntity.LOSE_ANIMATION)
}

/**
 * Plays one of Cobblemon's known battle animations on this NPC.
 */
fun NPCEntity.playAnimation(animation: BattleAnimation) {
    this.playAnimation(animation.key)
}

/**
 * Plays this NPC's configured battle theme (falls back to Cobblemon's default PvN battle theme
 * if none is set on the NPC's class/preset).
 */
fun NPCEntity.playBattleTheme() {
    val themeLocation = this.getBattleTheme()
    val soundEvent = BuiltInRegistries.SOUND_EVENT.get(themeLocation) ?: return
    this.playSound(soundEvent, 1.0f, 1.0f)
}
