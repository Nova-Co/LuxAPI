package com.novaco.luxapi.database.migration

import java.sql.Connection

/**
 * A single ordered schema change within one module's migration sequence.
 *
 * @param version Ascending, unique within its module's registered migrations.
 * @param apply Runs inside a transaction; throwing rolls back that migration and aborts startup.
 */
data class Migration(
    val version: Int,
    val description: String,
    val apply: (Connection) -> Unit
)
