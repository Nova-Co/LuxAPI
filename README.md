# LuxAPI

Cross-platform Kotlin API toolkit for Minecraft **1.21.1** mods on **Fabric** and **NeoForge**.

- Version: `1.1.1`
- Java: `21`

## What You Get

- Unified command framework (annotation-based)
- GUI and paginated GUI builders
- Scheduler (sync/async/repeating tasks)
- Event bus and service registry
- Optional modules for database and Cobblemon integration

## Modules (Quick View)

- `commons` - Core API (`LuxAPI`, events, services, scheduler, GUI abstractions)
- `core` - Brigadier/Minecraft command bridge
- `database` - Database layer (`LuxDatabase.init()`)
- `cobblemon` - Cobblemon hooks (`LuxCobblemon.init()`)
- `fabric` - Fabric platform bootstrap (`LuxFabricInitializer`)
- `neoforge` - NeoForge platform bootstrap (`LuxNeoForgeInitializer`)ฟ

## Requirements

- Java 21
- Minecraft 1.21.1
- Fabric Loader or NeoForge (based on your target platform)

## Add Dependencies

Use only what your project needs.

```kotlin
dependencies {
	implementation(project(":commons"))
	implementation(project(":core"))

	// Optional
	implementation(project(":database"))
	implementation(project(":cobblemon"))

	// Pick one platform module for platform-specific code
	implementation(project(":fabric"))
	// implementation(project(":neoforge"))
}
```

If you publish artifacts yourself, use group `com.novaco.luxapi` and version `1.1.1`.

## Quick Start

Initialize LuxAPI during your mod startup:

```kotlin
LuxAPI.init()
```

Enable optional modules only when needed:

```kotlin
LuxCobblemon.init() // Optional
LuxDatabase.init()  // Optional
```

After platform setup is complete, use the API:

```kotlin
LuxAPI.getScheduler().runLater(20L) {
	println("Run after 1 second")
}
```

## Build

```powershell
./gradlew build
```

## License

See `LICENSE`.


