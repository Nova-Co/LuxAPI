package com.novaco.luxapi.database.service

import java.sql.PreparedStatement
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID

/**
 * Dispatches Kotlin-typed bind parameters onto a [PreparedStatement] using the
 * matching `setX` JDBC method, avoiding `setObject`'s driver-dependent behavior.
 */
internal object SqlBinder {

    fun bindAll(statement: PreparedStatement, params: Array<out Any?>) {
        params.forEachIndexed { index, param ->
            bind(statement, index + 1, param)
        }
    }

    private fun bind(statement: PreparedStatement, index: Int, param: Any?) {
        when (param) {
            is String -> statement.setString(index, param)
            is Int -> statement.setInt(index, param)
            is Long -> statement.setLong(index, param)
            is Double -> statement.setDouble(index, param)
            is Float -> statement.setFloat(index, param)
            is Boolean -> statement.setBoolean(index, param)
            is UUID -> statement.setString(index, param.toString())
            is Instant -> statement.setTimestamp(index, Timestamp.from(param))
            is SqlNull -> statement.setNull(index, param.sqlType)
            null -> throw IllegalArgumentException(
                "Raw null at parameter index $index is not supported; use SqlNull(java.sql.Types.X) for nullable binds."
            )
            else -> throw IllegalArgumentException(
                "Unsupported SQL parameter type '${param::class.java.name}' at index $index"
            )
        }
    }
}
