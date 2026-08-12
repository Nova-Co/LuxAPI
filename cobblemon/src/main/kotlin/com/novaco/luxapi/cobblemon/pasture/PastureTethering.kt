package com.novaco.luxapi.cobblemon.pasture

import java.util.UUID

/**
 * A snapshot of one Pokémon tethered in a pasture. Built fresh from Cobblemon's internal
 * tethering record each time it's requested — not a live reference, so it can't go
 * stale-but-look-live.
 *
 * @property pokemonId UUID of the tethered Pokémon.
 * @property ownerId UUID of the player who tethered it.
 * @property ownerName Name of the player who tethered it, at the time of tethering.
 * @property pcId UUID of the PC box the Pokémon's data actually lives in.
 * @property entityId The world entity ID of the roaming Pokémon entity.
 */
data class PastureTethering(
    val pokemonId: UUID,
    val ownerId: UUID,
    val ownerName: String,
    val pcId: UUID,
    val entityId: Int
)
