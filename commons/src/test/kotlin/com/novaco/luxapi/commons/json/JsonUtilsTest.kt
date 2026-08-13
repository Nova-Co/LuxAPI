package com.novaco.luxapi.commons.json

import com.novaco.luxapi.commons.json.JsonUtils.getBooleanOrDefault
import com.novaco.luxapi.commons.json.JsonUtils.getDoubleOrDefault
import com.novaco.luxapi.commons.json.JsonUtils.getIntOrDefault
import com.novaco.luxapi.commons.json.JsonUtils.getStringOrDefault
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

data class DummyPayload(val name: String, val count: Int)

class JsonUtilsTest {

    @Test
    fun `test round trip serialization and deserialization`() {
        val payload = DummyPayload("test", 5)

        val json = JsonUtils.toJson(payload)
        val restored = JsonUtils.fromJson<DummyPayload>(json)

        assertEquals(payload, restored)
    }

    @Test
    fun `test fromJson returns null on malformed input`() {
        assertNull(JsonUtils.fromJson<DummyPayload>("{not valid json"))
    }

    @Test
    fun `test parseObject rejects non-object json`() {
        assertNull(JsonUtils.parseObject("[1, 2, 3]"))
        assertNotNull(JsonUtils.parseObject("{\"a\": 1}"))
    }

    @Test
    fun `test parseArray rejects non-array json`() {
        assertNull(JsonUtils.parseArray("{\"a\": 1}"))
        assertNotNull(JsonUtils.parseArray("[1, 2, 3]"))
    }

    @Test
    fun `test safe accessors fall back to defaults when field missing or null`() {
        val obj = JsonUtils.parseObject("""{"name": "lux", "count": 3, "active": true, "ratio": 1.5, "nullField": null}""")!!

        assertEquals("lux", obj.getStringOrDefault("name"))
        assertEquals(3, obj.getIntOrDefault("count"))
        assertTrue(obj.getBooleanOrDefault("active"))
        assertEquals(1.5, obj.getDoubleOrDefault("ratio"))

        assertEquals("fallback", obj.getStringOrDefault("missing", "fallback"))
        assertEquals(42, obj.getIntOrDefault("nullField", 42))
    }
}
