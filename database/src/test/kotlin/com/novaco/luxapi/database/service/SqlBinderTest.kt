package com.novaco.luxapi.database.service

import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.sql.PreparedStatement
import java.sql.Timestamp
import java.sql.Types
import java.time.Instant
import java.util.UUID

class SqlBinderTest {

    @Test
    fun `test binds all supported primitive and wrapper types`() {
        val statement = mock(PreparedStatement::class.java)
        val uuid = UUID.randomUUID()
        val instant = Instant.parse("2026-01-01T00:00:00Z")

        SqlBinder.bindAll(
            statement,
            arrayOf("text-value", 42, 100L, 3.14, 1.5f, true, uuid, instant)
        )

        verify(statement).setString(1, "text-value")
        verify(statement).setInt(2, 42)
        verify(statement).setLong(3, 100L)
        verify(statement).setDouble(4, 3.14)
        verify(statement).setFloat(5, 1.5f)
        verify(statement).setBoolean(6, true)
        verify(statement).setString(7, uuid.toString())
        verify(statement).setTimestamp(8, Timestamp.from(instant))
    }

    @Test
    fun `test binds SqlNull using its declared java sql type`() {
        val statement = mock(PreparedStatement::class.java)

        SqlBinder.bindAll(statement, arrayOf(SqlNull(Types.VARCHAR)))

        verify(statement).setNull(1, Types.VARCHAR)
    }

    @Test
    fun `test raw null throws instead of silently binding`() {
        val statement = mock(PreparedStatement::class.java)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            SqlBinder.bindAll(statement, arrayOf<Any?>(null))
        }

        assertTrue(exception.message!!.contains("SqlNull"))
    }

    @Test
    fun `test unsupported type throws`() {
        val statement = mock(PreparedStatement::class.java)

        assertThrows(IllegalArgumentException::class.java) {
            SqlBinder.bindAll(statement, arrayOf(listOf("unsupported")))
        }
    }
}
