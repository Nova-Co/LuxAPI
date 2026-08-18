package com.novaco.luxapi.commons.config

import com.novaco.luxapi.commons.config.annotation.Comment
import com.novaco.luxapi.commons.config.annotation.Config
import com.novaco.luxapi.commons.config.exception.ConfigException
import com.novaco.luxapi.commons.config.serializer.ConfigTypeSerializerRegistry
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import org.spongepowered.configurate.serialize.TypeSerializer
import java.io.File
import java.lang.reflect.Type

@ConfigSerializable
abstract class BaseTestConfig : LuxConfig() {
    var baseField: String = "base-default"
}

@ConfigSerializable
@Config("leaf.yml")
class LeafTestConfig : BaseTestConfig() {
    var leafField: String = "leaf-default"
}

@ConfigSerializable
@Config("arena.yml")
class ArenaTestConfig : LuxConfig() {
    var name: String = "unnamed"
}

data class Point(val x: Int, val y: Int)

class PointTypeSerializer : TypeSerializer<Point> {
    override fun deserialize(type: Type, node: ConfigurationNode): Point {
        return Point(node.node("x").getInt(), node.node("y").getInt())
    }

    override fun serialize(type: Type, obj: Point?, node: ConfigurationNode) {
        if (obj == null) {
            node.set(null)
            return
        }
        node.node("x").set(obj.x)
        node.node("y").set(obj.y)
    }
}

@ConfigSerializable
@Config("point.yml")
class PointTestConfig : LuxConfig() {
    var location: Point = Point(1, 2)
}

@ConfigSerializable
@Config("bundled.yml", resource = "test-default-config.yml")
class BundledDefaultTestConfig : LuxConfig() {
    var host: String = "code-default-host"
    var port: Int = 1111
}

@ConfigSerializable
@Config("missing-resource.yml", resource = "does-not-exist.yml")
class MissingResourceTestConfig : LuxConfig() {
    var value: String = "x"
}

class ConfigServiceUpliftTest {

    @Test
    fun `reload syncs fields declared on an intermediate superclass`(@TempDir tempDir: File) {
        val config = ConfigService.load(LeafTestConfig::class.java, tempDir)
        config.baseField = "base-changed"
        config.leafField = "leaf-changed"
        config.save()

        val reloaded = LeafTestConfig()
        reloaded.init(File(tempDir, "leaf.yml"))
        reloaded.reload()

        assertEquals("base-changed", reloaded.baseField, "Field from the intermediate superclass must be synced on reload.")
        assertEquals("leaf-changed", reloaded.leafField)
    }

    @Test
    fun `loadAll loads every yml file in a directory as the same config type`(@TempDir tempDir: File) {
        val arenasDir = File(tempDir, "arenas").apply { mkdirs() }
        File(arenasDir, "a.yml").writeText("name: alpha\n")
        File(arenasDir, "b.yml").writeText("name: beta\n")
        File(arenasDir, "ignored.txt").writeText("not a config")

        val arenas = ConfigService.loadAll(ArenaTestConfig::class.java, arenasDir)

        assertEquals(2, arenas.size)
        assertEquals(listOf("alpha", "beta"), arenas.map { it.name }.sorted())
    }

    @Test
    fun `custom type serializer registered via registry is used for load and save`(@TempDir tempDir: File) {
        ConfigTypeSerializerRegistry.register(Point::class.java, PointTypeSerializer())

        val config = ConfigService.load(PointTestConfig::class.java, tempDir)
        config.location = Point(5, 7)
        config.save()

        val reloaded = ConfigService.load(PointTestConfig::class.java, tempDir)
        assertEquals(Point(5, 7), reloaded.location)
    }

    @Test
    fun `bundled default resource is copied in on first load`(@TempDir tempDir: File) {
        val config = ConfigService.load(BundledDefaultTestConfig::class.java, tempDir)

        assertEquals("bundled-default-host", config.host, "Should load values from the bundled resource, not the code default.")
        assertEquals(9999, config.port)
    }

    @Test
    fun `missing bundled resource throws ConfigException`(@TempDir tempDir: File) {
        assertThrows(ConfigException::class.java) {
            ConfigService.load(MissingResourceTestConfig::class.java, tempDir)
        }
    }

    @Test
    fun `malformed yaml throws ConfigException instead of leaking Configurate internals`(@TempDir tempDir: File) {
        val file = File(tempDir, "leaf.yml")
        file.writeText("baseField: [unterminated\n")

        assertThrows(ConfigException::class.java) {
            ConfigService.load(LeafTestConfig::class.java, tempDir)
        }
    }
}
