package com.novaco.luxapi.commons.config.serializer

import com.novaco.luxapi.commons.config.ConfigService
import com.novaco.luxapi.commons.config.LuxConfig
import com.novaco.luxapi.commons.config.annotation.Config
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.spongepowered.configurate.objectmapping.ConfigSerializable
import java.io.File

interface Shape : PolymorphicConfigEntry {
    fun area(): Double
}

@ConfigSerializable
class Circle : Shape {
    var radius: Double = 1.0
    override fun id(): String = "circle"
    override fun area(): Double = Math.PI * radius * radius
}

@ConfigSerializable
class Square : Shape {
    var side: Double = 1.0
    override fun id(): String = "square"
    override fun area(): Double = side * side
}

@ConfigSerializable
@Config("shape.yml")
class ShapeHolderConfig : LuxConfig() {
    var shape: Shape? = null
}

class PolymorphicConfigRegistryTest {

    @Test
    fun `polymorphic entry round-trips through the discriminator field`(@TempDir tempDir: File) {
        val registry = PolymorphicConfigRegistry<Shape>()
        registry.register("circle", Circle::class.java)
        registry.register("square", Square::class.java)
        ConfigTypeSerializerRegistry.register(Shape::class.java, registry.serializer(Shape::class.java))

        val config = ConfigService.load(ShapeHolderConfig::class.java, tempDir)
        config.shape = Square().apply { side = 4.0 }
        config.save()

        val reloaded = ConfigService.load(ShapeHolderConfig::class.java, tempDir)

        assertNotNull(reloaded.shape)
        assertEquals("square", reloaded.shape?.id())
        assertEquals(16.0, reloaded.shape?.area())
    }
}
