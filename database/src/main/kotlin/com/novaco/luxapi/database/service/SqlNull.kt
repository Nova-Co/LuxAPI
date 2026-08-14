package com.novaco.luxapi.database.service

/**
 * Marker for a nullable SQL bind parameter, since a raw Kotlin `null` inside a
 * vararg `Any?` list loses the JDBC column type [java.sql.PreparedStatement.setNull] needs.
 *
 * Usage: `service.update(sql).bind(SqlNull(java.sql.Types.VARCHAR))`
 */
data class SqlNull(val sqlType: Int)
