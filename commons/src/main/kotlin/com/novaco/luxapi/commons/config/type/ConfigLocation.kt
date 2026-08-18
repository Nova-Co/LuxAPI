package com.novaco.luxapi.commons.config.type

import com.novaco.luxapi.commons.math.Vector3D
import org.spongepowered.configurate.objectmapping.ConfigSerializable

/**
 * A serializable 3D world location — world name, coordinates, and rotation — for config sections
 * like warps, spawn points, or region markers. Flat fields (not a nested [Vector3D]) so it maps
 * cleanly onto YAML without needing Configurate support on [Vector3D] itself.
 */
@ConfigSerializable
class ConfigLocation {

    var worldName: String = "world"
    var x: Double = 0.0
    var y: Double = 0.0
    var z: Double = 0.0
    var yaw: Float = 0f
    var pitch: Float = 0f

    fun toVector3D(): Vector3D = Vector3D(x, y, z)

    companion object {
        fun builder(): Builder = Builder()
    }

    class Builder internal constructor() {
        private val location = ConfigLocation()

        fun worldName(worldName: String): Builder = apply { location.worldName = worldName }
        fun position(vector: Vector3D): Builder = apply {
            location.x = vector.x
            location.y = vector.y
            location.z = vector.z
        }
        fun x(x: Double): Builder = apply { location.x = x }
        fun y(y: Double): Builder = apply { location.y = y }
        fun z(z: Double): Builder = apply { location.z = z }
        fun yaw(yaw: Float): Builder = apply { location.yaw = yaw }
        fun pitch(pitch: Float): Builder = apply { location.pitch = pitch }

        fun build(): ConfigLocation = location
    }
}
