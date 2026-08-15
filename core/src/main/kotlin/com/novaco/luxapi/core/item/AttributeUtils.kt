package com.novaco.luxapi.core.item

import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponents
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EquipmentSlotGroup
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attribute
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.item.ItemStack

/**
 * Utility for adding attribute modifiers to items (independent of enchantments) and for
 * applying/removing live modifiers directly on an entity's attribute instances.
 */
object AttributeUtils {

    /**
     * Adds an attribute modifier to an item, active while equipped in [slotGroup].
     */
    fun addItemModifier(
        stack: ItemStack,
        attribute: Holder<Attribute>,
        id: ResourceLocation,
        amount: Double,
        operation: AttributeModifier.Operation,
        slotGroup: EquipmentSlotGroup = EquipmentSlotGroup.ANY
    ): ItemStack {
        val existing = stack.get(DataComponents.ATTRIBUTE_MODIFIERS) ?: net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY
        val updated = existing.withModifierAdded(attribute, AttributeModifier(id, amount, operation), slotGroup)
        stack.set(DataComponents.ATTRIBUTE_MODIFIERS, updated)
        return stack
    }

    /**
     * Adds a transient (non-persistent) modifier directly to a living entity's attribute instance.
     */
    fun addEntityModifier(entity: LivingEntity, attribute: Holder<Attribute>, modifier: AttributeModifier) {
        entity.getAttribute(attribute)?.addTransientModifier(modifier)
    }

    fun removeEntityModifier(entity: LivingEntity, attribute: Holder<Attribute>, id: ResourceLocation): Boolean {
        return entity.getAttribute(attribute)?.removeModifier(id) ?: false
    }
}
