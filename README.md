# LuxAPI

**A Kotlin developer API for building Minecraft mods and sidemods on top of [Cobblemon](https://cobblemon.com/), across Fabric and NeoForge.**

LuxAPI wraps Cobblemon's internals — storage, spawning, battles, dialogue, NPCs — behind a small, ergonomic surface, so you can build features without reverse-engineering Cobblemon's source every time. It's modeled loosely on [EnvyWare/API](https://github.com/EnvyWare/API) (the equivalent dev API for Pixelmon Reforged) — not a fork or port, just the same idea: give third-party mod developers a stable, well-documented layer instead of raw mod internals.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
![Java 21](https://img.shields.io/badge/Java-21-orange)
![Minecraft 1.21.1](https://img.shields.io/badge/Minecraft-1.21.1-green)
![Kotlin](https://img.shields.io/badge/Kotlin-2.1.10-blueviolet)

---

## Who this is for

You're building a Minecraft mod or a Cobblemon sidemod and don't want to write yet another command framework, GUI builder, or Cobblemon storage-sync workaround from scratch. LuxAPI is a `compileOnly`/`implementation` dependency you build against — not a standalone mod players install by itself.

## Modules

| Module | Purpose |
|---|---|
| `commons` | Platform-agnostic core: command engine, scheduler, GUI builders, database service, Discord webhooks, text/chat utilities, math helpers. No Minecraft classes. |
| `core` | Brigadier/Minecraft command bridge shared by both loaders. |
| `fabric` | Fabric platform bootstrap (`LuxFabricInitializer`). |
| `neoforge` | NeoForge platform bootstrap (`LuxNeoForgeInitializer`). |
| `cobblemon` | The main event — Cobblemon-specific hooks: world bosses, dialogue/NPCs, battle scripting, PC/party storage, dynamic spawning, economy hooks, cinematic FX. Entry point: `LuxCobblemon`. |
| `database` | Optional async database layer (`LuxDatabase`), auto-detected by `cobblemon` at runtime if present. |
| `economy` | Optional standalone economy plugin layer (`LuxEconomy`), separate from the Cobblemon-integrated appraisal system in `cobblemon`. |
| `bukkit` | Early Bukkit/Spigot platform command layer — not part of the Fabric/NeoForge/Cobblemon story above; still maturing. |
| `discord` | Currently an empty stub (tracked for re-scope or removal). |

Everything is optional except `commons` — pull in only what you need.

## What's in the box

The `cobblemon` module is where most of the API surface lives:

- **World Boss Framework** — custom wild Pokémon builder, boss bar/scoreboard sync, aggro/DPS tracking, phase and minion management, event hooks for loot/quests.
- **Dialogue & NPCs** — fluent multi-page dialogue trees (text/choice/input pages, custom speakers, dynamic per-render text), NPC battle animations and initiation helpers.
- **Battle Scripting** — force wild/player/NPC battles, species/level battle rules, health-threshold interceptors for cutscenes.
- **Safe Storage & Transactions** — PC box moves and Party↔PC transfers that go through Cobblemon's real sync path (no client desync), plus a validated IV/EV/nature/held-item property editor.
- **Dynamic Spawning** — timed swarm/outbreak events, region-scoped spawn rate & species control (`AreaSpawnManager`), and a hook into Cobblemon's natural spawn event (`SpawnInterceptor`).
- **Economy Hooks** — pluggable Pokémon appraisal/pricing chain.
- **Cinematic & FX** — Snowstorm particle effects, move particles, sounds, pre-built animation triggers.

The `commons`/`core` modules give you the cross-platform foundation everything else is built on: a Brigadier command wrapper, tick-based scheduler, chest GUI + pagination builders, player abstraction, attribute/metadata storage, Discord webhooks, i18n, and math/cooldown utilities.

Not everything on the roadmap is finished — see [`TODO.md`](TODO.md) for the full phase-by-phase status, including one **known critical gap**: the GTS listing/purchase system in `cobblemon` is currently a non-functional stub with a real dupe risk. Don't ship that part yet.

## Requirements

- Java 21
- Minecraft 1.21.1
- Fabric Loader or NeoForge, depending on your target platform
- Kotlin 2.1.10 (pulled in automatically via the Gradle plugin)

## Adding LuxAPI to your project

LuxAPI is a **library mod**: at runtime, your mod depends on the LuxAPI mod jar being installed alongside it (like Fabric API or Cardinal Components) — it's not something you shade into your own jar.

**⚠️ No remote Maven repo yet** (tracked in [`TODO.md`](TODO.md), Phase 11) — this repo's Gradle build only publishes to a local `build/repo` directory. Until that lands, to build against LuxAPI from a separate mod project you'll need to either:
- build this repo yourself (`./gradlew build`) and point your project at the resulting jars in `*/build/libs/`, or
- include this repo as a Gradle composite build / git submodule and depend on it via `project(":x")`, as shown below.

**1. Compile-time dependency** (from within this repo, or a composite build that includes it):

```kotlin
dependencies {
    implementation(project(":commons"))
    implementation(project(":core"))

    // Cobblemon integration — pulls in Cobblemon 1.7.3+1.21.1 as compileOnly
    implementation(project(":cobblemon"))

    // Optional
    implementation(project(":database"))

    // Pick one platform module
    implementation(project(":fabric"))
    // implementation(project(":neoforge"))
}
```

If you publish LuxAPI artifacts yourself, use group `com.novaco.luxapi`, version `1.2.4`.

**2. Runtime dependency** — declare LuxAPI as a required mod (modId `luxapi`) so the loader enforces it's installed:

```json
// fabric.mod.json
"depends": { "luxapi": "*" }
```

```toml
# neoforge.mods.toml
[[dependencies.yourmodid]]
modId = "luxapi"
type = "required"
versionRange = "[1.2.4,)"
ordering = "AFTER"
side = "BOTH"
```

Your own mod's entrypoint is registered the same way LuxAPI registers its own (see `fabric/src/main/resources/fabric.mod.json` and `neoforge/src/resources/META-INF/neoforge.mods.toml` in this repo for the real, working example) — LuxAPI's classes are then just on the classpath to call directly, no service-locator dance needed.

## Core concept: `LuxPlayer`

Almost every API surface in LuxAPI takes a `LuxPlayer`, not a raw `ServerPlayer` — it's the cross-platform player abstraction that lets the same call work identically on Fabric and NeoForge. You get one from `PlayerManager`:

```kotlin
val luxPlayer: LuxPlayer? = PlayerManager.getPlayer(uuid) // or getPlayer(name)
```

Command handlers and GUI callbacks hand you a `LuxPlayer` directly. If you need the underlying platform player back for a Minecraft-native API, `LuxPlayer.parent` holds it (cast to `ServerPlayer`).

## Quick start

Initialize LuxAPI once during your mod's startup:

```kotlin
LuxAPI.init()
LuxCobblemon.init() // if you're using the cobblemon module
```

For manual in-game testing during development, both platform modules have run tasks:

```bash
./gradlew :fabric:runClient      # or :fabric:runServer
./gradlew :neoforge:runClient    # or :neoforge:runServer
```

From there, the scheduler is a good first thing to reach for:

```kotlin
LuxAPI.getScheduler().runLater(20L) {
    println("Runs 1 second later")
}
```

### A taste of the Cobblemon layer

Editing a caught Pokémon's properties safely (validated, packet-synced, no manual sync code):

```kotlin
PokemonPropertyManager.setIV(pokemon, Stats.SPEED, 31)
pokemon.setNature("adamant")
```

Restricting spawns in a region — say, a safe zone with no wild Pokémon:

```kotlin
AreaSpawnManager.registerArea(
    SpawnArea(
        dimension = Level.OVERWORLD,
        region = Cuboid(Vector3D(0.0, 64.0, 0.0), Vector3D(50.0, 80.0, 50.0)),
        weightMultiplier = 0.0F
    )
)
```

Hooking natural spawns:

```kotlin
SpawnInterceptor.onPokemonSpawn { event ->
    if (event.species == "Mewtwo") event.cancel()
}
```

## Building & testing

```bash
./gradlew build
```

To run a single module's tests (the `cobblemon` module requires **JDK 21** specifically — point `JAVA_HOME` at a JDK 21 install if your default is older):

```bash
./gradlew :cobblemon:test
```

There's no CI build/test gate yet (see `TODO.md`, Phase 11) — run tests locally before opening a PR.

## Project status

LuxAPI is under active development. Phases 1–7 and 9 of the roadmap are complete; Phase 8 (Economy & Trade) has a known critical gap flagged above, and Phases 10–11 (deeper Cobblemon feature coverage, publishing/CI polish) are open. See [`TODO.md`](TODO.md) for the authoritative, up-to-date breakdown — it's kept current after every session, including honest notes on scope corrections and known limitations.

## Contributing

No formal contribution process yet. If you're interested in contributing, open an issue or PR and we'll figure it out from there.

## License

MIT — see [`LICENSE`](LICENSE).
