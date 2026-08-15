package com.novaco.luxapi.core.effect

import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.ints.IntList
import net.minecraft.core.component.DataComponents
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.projectile.FireworkRocketEntity
import net.minecraft.world.item.Items
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.FireworkExplosion
import net.minecraft.world.item.component.Fireworks

/**
 * A fluent builder for firework explosion effects, independent of any launched rocket item.
 */
class FireworkBuilder {

    private var shape: FireworkExplosion.Shape = FireworkExplosion.Shape.SMALL_BALL
    private val colors: IntList = IntArrayList()
    private val fadeColors: IntList = IntArrayList()
    private var trail = false
    private var twinkle = false
    private var flightDuration = 1

    fun shape(shape: FireworkExplosion.Shape) = apply { this.shape = shape }

    fun colors(vararg rgb: Int) = apply { colors.clear(); colors.addAll(rgb.toList()) }

    fun fadeColors(vararg rgb: Int) = apply { fadeColors.clear(); fadeColors.addAll(rgb.toList()) }

    fun trail(value: Boolean = true) = apply { this.trail = value }

    fun twinkle(value: Boolean = true) = apply { this.twinkle = value }

    fun flightDuration(ticks: Int) = apply { this.flightDuration = ticks }

    /**
     * Builds the firework rocket ItemStack (data only — use [spawn] to actually launch it).
     */
    fun build(): ItemStack {
        val explosion = FireworkExplosion(shape, colors, fadeColors, trail, twinkle)
        val stack = ItemStack(Items.FIREWORK_ROCKET)
        stack.set(DataComponents.FIREWORKS, Fireworks(flightDuration, listOf(explosion)))
        return stack
    }

    /**
     * Builds and immediately launches the firework at the given position.
     */
    fun spawn(level: ServerLevel, x: Double, y: Double, z: Double) {
        level.addFreshEntity(FireworkRocketEntity(level, x, y, z, build()))
    }
}
