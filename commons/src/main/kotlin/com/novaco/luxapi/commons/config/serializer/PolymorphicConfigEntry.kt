package com.novaco.luxapi.commons.config.serializer

/**
 * Marks a type as a member of a discriminated-union config section handled by
 * [PolymorphicConfigRegistry] — e.g. one interface with several config-serializable
 * implementations, selected by a "type" field in the YAML (like Configurate's own
 * scalar-vs-list handling, but for developer-defined variants).
 */
interface PolymorphicConfigEntry {

    /**
     * The discriminator value this implementation is registered under.
     * Must match the id passed to [PolymorphicConfigRegistry.register] for this class.
     */
    fun id(): String
}
