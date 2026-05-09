package com.novaco.luxapi.cobblemon.npc

/**
 * Defines the built-in AI movement and behavior presets for a LuxNPC.
 */
enum class LuxMovement {
    /** * The NPC will remain frozen exactly where spawned.
     * It cannot be pushed by entities or wander off.
     */
    STATIONARY,

    /** * The NPC will utilize Cobblemon's native AI to wander
     * naturally around its initial spawn location.
     */
    WANDER
}