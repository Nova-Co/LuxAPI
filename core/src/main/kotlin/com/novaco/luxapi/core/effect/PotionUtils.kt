package com.novaco.luxapi.core.effect

import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponents
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.alchemy.PotionContents

/**
 * Utility for applying/clearing status effects on entities and building custom potion items.
 */
object PotionUtils {

    /**
     * Applies a status effect for [durationTicks] at [amplifier] (0 = level I).
     */
    fun apply(entity: LivingEntity, effect: Holder<MobEffect>, durationTicks: Int, amplifier: Int = 0): Boolean {
        return entity.addEffect(MobEffectInstance(effect, durationTicks, amplifier))
    }

    fun clear(entity: LivingEntity, effect: Holder<MobEffect>): Boolean {
        return entity.removeEffect(effect)
    }

    fun has(entity: LivingEntity, effect: Holder<MobEffect>): Boolean {
        return entity.hasEffect(effect)
    }

    /**
     * Builds a potion-type ItemStack (potion/splash potion/lingering potion/tipped arrow)
     * carrying a custom list of effects instead of a base vanilla potion.
     */
    fun customPotionItem(item: Item, effects: List<MobEffectInstance>, customColor: Int? = null): ItemStack {
        val stack = ItemStack(item)
        stack.set(
            DataComponents.POTION_CONTENTS,
            PotionContents(java.util.Optional.empty(), java.util.Optional.ofNullable(customColor), effects)
        )
        return stack
    }
}
