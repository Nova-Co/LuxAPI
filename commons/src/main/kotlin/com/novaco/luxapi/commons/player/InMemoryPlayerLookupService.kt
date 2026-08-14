package com.novaco.luxapi.commons.player

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Default [PlayerLookupService] backed by two [ConcurrentHashMap]s in memory.
 * Only resolves players seen since this process started; does not persist across restarts.
 */
class InMemoryPlayerLookupService : PlayerLookupService {

    private val nameToUuid = ConcurrentHashMap<String, UUID>()
    private val uuidToName = ConcurrentHashMap<UUID, String>()

    override fun resolveUuid(name: String): UUID? = nameToUuid[name.lowercase()]

    override fun resolveName(uuid: UUID): String? = uuidToName[uuid]

    override fun record(uuid: UUID, name: String) {
        nameToUuid[name.lowercase()] = uuid
        uuidToName[uuid] = name
    }
}
