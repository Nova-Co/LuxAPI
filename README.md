# LuxAPI

**A Kotlin developer API for building Minecraft mods and sidemods on top of [Cobblemon](https://cobblemon.com/), across Fabric and NeoForge.**

LuxAPI wraps Cobblemon's internals — storage, spawning, battles, dialogue, NPCs — behind a small, ergonomic surface, so you can build features without reverse-engineering Cobblemon's source every time. The goal: give third-party mod developers a stable, well-documented layer instead of raw mod internals.

[![Build](https://github.com/Nova-Co/LuxAPI/actions/workflows/build.yml/badge.svg)](https://github.com/Nova-Co/LuxAPI/actions/workflows/build.yml)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/4b03f29fdb4f4e278523332a0861fef8)](https://app.codacy.com/gh/Nova-Co/LuxAPI/dashboard?utm_source=gh&utm_medium=referral&utm_content=&utm_campaign=Badge_grade)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
![Java 21](https://img.shields.io/badge/Java-21-orange)
![Minecraft 1.21.1](https://img.shields.io/badge/Minecraft-1.21.1-green)
![Kotlin](https://img.shields.io/badge/Kotlin-2.1.10-blueviolet)

---

## Table of contents

- [Who this is for](#who-this-is-for)
- [Modules](#modules)
- [What's in the box](#whats-in-the-box)
- [Requirements](#requirements)
- [Adding LuxAPI to your project](#adding-luxapi-to-your-project)
- [Core concept: `LuxPlayer`](#core-concept-luxplayer)
- [Quick start](#quick-start)
- [Usage by module](#usage-by-module)
- [Building & testing](#building--testing)
- [Project status](#project-status)
- [Contributing](#contributing)
- [License](#license)

## Who this is for

You're building a Minecraft mod or a Cobblemon sidemod and don't want to write yet another command framework, GUI builder, or Cobblemon storage-sync workaround from scratch. LuxAPI is a `compileOnly`/`implementation` dependency you build against — **not a standalone mod players install by itself.**

## Modules

| Module | Purpose |
|---|---|
| `commons` | Platform-agnostic core: annotation-driven command engine, scheduler, GUI builders, economy/database service contracts, Discord webhooks, i18n, math/text helpers. No Minecraft classes. |
| `core` | Minecraft-facing utilities shared by both loaders: Brigadier command bridge, `ItemBuilder`, boss bars, scoreboards, particles/sounds, loot. |
| `fabric` | Fabric platform bootstrap (`LuxFabricInitializer`) — wires `commons`/`core` providers to Fabric implementations. |
| `neoforge` | NeoForge platform bootstrap (`LuxNeoForgeInitializer`) — same job as `fabric`, for NeoForge. |
| `cobblemon` | The main event — Cobblemon-specific hooks: world bosses, dialogue/NPCs, battle scripting, PC/party storage, dynamic spawning, economy hooks, cinematic FX. Entry point: `LuxCobblemon`. |
| `database` | Optional async database layer (`LuxDatabase`) and `PersistentAttribute` player-data framework, auto-detected by `cobblemon` at runtime if present. |
| `economy` | Standalone Bukkit-side plugin (`LuxEconomy`, ships as `LuxEcoCore`) exposing the `LuxEconomyCore` contract for a bridge plugin to connect Vault to a Cobblemon-adjacent currency source. Deliberately has no `commons`/`core`/Cobblemon coupling. |
| `bukkit` | Bukkit/Spigot platform layer (`LuxBukkitBridge`) — player/GUI/event/scheduler wrappers plus the annotation-driven command engine, mirroring what `fabric`/`neoforge` provide. A library module (no `plugin.yml` of its own), meant to be shaded into a consumer plugin. |

Everything is optional except `commons` — pick only the modules your mod actually needs.

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

Not everything on the roadmap is finished — see [`TODO.md`](TODO.md) for the full phase-by-phase status. No known critical gaps are currently open; the prior GTS listing/purchase dupe risk and the Fabric/NeoForge permission-bypass bug have both been fixed (see `TODO.md`'s CRITICAL section for the incident writeups).

## Requirements

- Java 21
- Minecraft 1.21.1
- Fabric Loader or NeoForge, depending on your target platform
- Kotlin 2.1.10 (pulled in automatically via the Gradle plugin)

## Adding LuxAPI to your project

LuxAPI is a **compile-time library, not a separately-installed mod**. Players don't download a `LuxAPI.jar` and drop it in `/mods` next to your mod — you pull LuxAPI in at build time and shade/embed the modules you use directly into your own mod's final jar, the same way you'd embed any other library. (Note that `fabric/build.gradle.kts` and `neoforge/build.gradle.kts` in this repo only shade `commons` into LuxAPI's own built platform jars — `core` and `cobblemon` are compile-time-only there too, which is exactly why this repo can't currently produce one complete standalone artifact for you to install separately even if you wanted to.)

**⚠️ No remote Maven repo yet** (tracked in [`TODO.md`](TODO.md), Phase 15) — this repo's Gradle build only publishes to a local `build/repo` directory. Until that lands, to build against LuxAPI from a separate mod project you'll need to either:
- build this repo yourself (`./gradlew build`) and point your project at the resulting jars in `*/build/libs/`, or
- include this repo as a Gradle composite build / git submodule and depend on it via `project(":x")`, as shown below.

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

Then shade all of those into your own mod's jar (e.g. via the `com.gradleup.shadow` plugin, same as this repo uses internally) so the classes ship inside your single distributed jar. If you publish LuxAPI artifacts yourself in the meantime, use group `com.novaco.luxapi`, version `1.2.5`.

This repo's own `fabric.mod.json`/`neoforge.mods.toml` and the `:fabric:runClient`/`:neoforge:runClient` tasks exist so LuxAPI can be booted standalone *for developing and testing LuxAPI itself* — they aren't a template for how your mod should depend on it.

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

## Usage by module

A minimal, working example for each module. All snippets assume `LuxAPI.init()` has already run.

### `commons`

Declare a command with annotations, register it, and reach for the scheduler:

```kotlin
@Command(name = "heal", aliases = ["hp"], permission = "mymod.heal")
class HealCommand {
    @SubCommand(name = "self", permission = "mymod.heal.self")
    fun healSelf(sender: CommandSender) {
        val player = sender.asPlayer() ?: return
        // ... apply healing via your platform's player object
    }
}

// during startup, on whichever platform's CommandManager you're using:
commandManager.register(HealCommand())

// tick-based scheduler, available everywhere via LuxAPI
LuxAPI.getScheduler().runRepeating(0L, 20L) {
    println("Runs every second")
}
```

### `core`

Build a Minecraft `ItemStack` (1.21+ Data Components) and register a Brigadier-backed command without touching the vanilla dispatcher directly:

```kotlin
val stack = ItemBuilder(Items.NETHER_STAR)
    .name("&bLegendary Core")
    .lore("&7Right-click to activate")
    .customModelData(1001)
    .unbreakable()
    .build()

BrigadierCommandAdapter.register(dispatcher, "mymod", permission = "mymod.use") { sender, args ->
    sender.sendMessage("You ran mymod with args: ${args.joinToString(" ")}")
}
```

### `fabric`

`LuxFabricInitializer` wires everything up automatically on `onInitialize()` — you don't call it yourself. To hook your own setup logic into that startup sequence without editing LuxAPI, implement `InitializationTask` and declare it under the `luxapi:init` entrypoint in your own `fabric.mod.json`:

```kotlin
class MyModInit : InitializationTask {
    override fun run() {
        println("MyMod is ready — LuxAPI has finished initializing.")
    }
}
```

```json
{
  "entrypoints": {
    "luxapi:init": ["com.example.mymod.MyModInit"]
  }
}
```

### `neoforge`

Same idea as Fabric — `LuxNeoForgeInitializer` runs automatically. `NeoForgeInitScanner` discovers `InitializationTask` implementations across loaded mods without any manifest entry needed on NeoForge; just make sure your class has a no-arg constructor and implements the interface:

```kotlin
class MyModInit : InitializationTask {
    override fun run() {
        println("MyMod is ready on NeoForge — LuxAPI has finished initializing.")
    }
}
```

### `cobblemon`

Editing a caught Pokémon's properties safely (validated, packet-synced, no manual sync code needed):

```kotlin
PokemonPropertyManager.setIV(pokemon, Stats.SPEED, 31)
pokemon.setNature("adamant")
```

Moving Pokémon between a player's party and PC through Cobblemon's real sync path:

```kotlin
luxPlayer.depositPokemonToPC(partySlot = 1)
luxPlayer.withdrawPokemonFromPC(box = 0, slot = 0, toPartySlot = 1)

val shinies = luxPlayer.getShinyPokemonInPC()
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

### `database`

Define a `PersistentAttribute` for per-player data and let `AttributeManager` handle load/save on join/quit:

```kotlin
class PlayerWallet(uuid: UUID) : PersistentAttribute(uuid) {
    var coins: Long = 0

    override fun loadData(service: DatabaseService) {
        // query `service`, populate `coins`
    }

    override fun saveData(service: DatabaseService) {
        // persist `coins` via `service`
    }
}

// during startup:
LuxDatabase.init()
AttributeManager.registerAttribute(PlayerWallet::class.java)

// anywhere you have a LuxPlayer:
val wallet = luxPlayer.getAttribute<PlayerWallet>()
```

### `economy`

`economy` ships as its own Bukkit plugin (`LuxEcoCore`). A bridge plugin implements `LuxEconomyCore` against its real economy backend (e.g. Vault) and registers it once:

```kotlin
LuxEconomyManager.registerProvider(object : LuxEconomyCore {
    override fun getBalance(playerUuid: UUID): Double = vaultEconomy.getBalance(playerUuid)
    override fun deposit(playerUuid: UUID, amount: Double) = vaultEconomy.depositAsync(playerUuid, amount)
    override fun withdraw(playerUuid: UUID, amount: Double) = vaultEconomy.withdrawAsync(playerUuid, amount)
})

// anywhere else that needs it:
val balance = LuxEconomyManager.getProvider().getBalance(playerUuid)
```

### `bukkit`

One call from your plugin's `onEnable()` wires up the player/GUI/event/scheduler bridges:

```kotlin
class MyPlugin : JavaPlugin() {
    override fun onEnable() {
        val playerManager = LuxBukkitBridge.initialize(this, initPackage = "com.example.myplugin.init")
    }
}
```

Any `InitializationTask` under `com.example.myplugin.init` is auto-discovered and run via classpath scanning — no manifest entry needed on Bukkit.

## Building & testing

```bash
./gradlew build
```

To run a single module's tests (the `cobblemon` module requires **JDK 21** specifically — point `JAVA_HOME` at a JDK 21 install if your default is older):

```bash
./gradlew :cobblemon:test
```

CI (`.github/workflows/build.yml`) runs `./gradlew build` on every push to `main` and every PR — still run tests locally before opening one, CI is a backstop, not a substitute.

## Project status

LuxAPI is under active development. Phases 1–12 of the roadmap are complete. Phase 13 (module fixes & process cleanup) is in progress; Phases 14 (test coverage) and 15 (distribution) are open. See [`TODO.md`](TODO.md) for the authoritative, up-to-date breakdown — it's kept current after every session, including honest notes on scope corrections and known limitations.

## Contributing

No formal contribution process yet. If you're interested in contributing, open an issue or PR and we'll figure it out from there.

## License

MIT — see [`LICENSE`](LICENSE).
