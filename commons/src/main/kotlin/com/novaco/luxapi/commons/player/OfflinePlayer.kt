package com.novaco.luxapi.commons.player

import java.util.UUID

/**
 * A resolved player identity that does not require the player to be online.
 * Distinct from [LuxPlayer], which always wraps a live platform player object.
 */
data class OfflinePlayer(val uniqueId: UUID, val name: String)
