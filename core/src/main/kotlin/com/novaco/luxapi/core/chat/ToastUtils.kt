package com.novaco.luxapi.core.chat

import net.minecraft.advancements.Advancement
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.advancements.AdvancementType
import net.minecraft.advancements.CriteriaTriggers
import net.minecraft.advancements.Criterion
import net.minecraft.advancements.critereon.ImpossibleTrigger
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

/**
 * Sends the advancement-style pop-up toast to a player without a real, registered advancement.
 * Builds a throwaway [AdvancementHolder] with an unreachable ("impossible") criterion, awards it
 * to trigger the toast, then immediately revokes it so it never appears in the player's book.
 */
object ToastUtils {

    fun sendToast(player: ServerPlayer, icon: ItemStack, title: Component, description: Component = Component.empty()) {
        val holder = buildHolder(icon, title, description)
        val advancements = player.advancements
        advancements.award(holder, "impossible")
        advancements.revoke(holder, "impossible")
    }

    private fun buildHolder(icon: ItemStack, title: Component, description: Component): AdvancementHolder {
        val advancement = Advancement.Builder.advancement()
            .display(icon, title, description, null, AdvancementType.TASK, true, false, true)
            .addCriterion("impossible", Criterion(CriteriaTriggers.IMPOSSIBLE, ImpossibleTrigger.TriggerInstance()))
            .build(ResourceLocation.fromNamespaceAndPath("luxapi", "toast/" + java.util.UUID.randomUUID()))
        return advancement
    }
}
