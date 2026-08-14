package com.novaco.luxapi.commons.player

import java.util.UUID

/**
 * Resolves name/UUID pairs for players who are not currently online.
 *
 * [PlayerManager] only ever knows about players who are currently connected.
 * This service is the commons-level extension point for resolving a name to a UUID
 * (and back) regardless of online status, so commands like `/pay <offline player>`
 * can target someone who isn't on the server right now.
 *
 * The default [InMemoryPlayerLookupService] only remembers players seen since this
 * process started. A platform module wanting resolution across restarts should
 * register a persistence-backed implementation (e.g. via the `database` module)
 * through the same [com.novaco.luxapi.commons.service.ServiceManager] slot instead.
 */
interface PlayerLookupService {

    /**
     * Resolves a player's UUID from their (case-insensitive) name, if known.
     */
    fun resolveUuid(name: String): UUID?

    /**
     * Resolves a player's most recently seen name from their UUID, if known.
     */
    fun resolveName(uuid: UUID): String?

    /**
     * Records a name/UUID pair as seen. Call this whenever a player is confirmed
     * online (e.g. from a join listener, or opportunistically when a command
     * resolves an online player) so future offline lookups can find them.
     */
    fun record(uuid: UUID, name: String)
}
