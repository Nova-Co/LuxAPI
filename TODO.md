# LuxAPI Development Roadmap

## 🟢 Phase 1: Multi-loader Architecture & Core Infrastructure (Completed)
*The baseline foundation that allows LuxAPI to run seamlessly on both Fabric and NeoForge.*
- [x] **Platform-Agnostic Bridge:** Unified `commons` and `core` modules mapping directly to `fabric` and `neoforge` implementations.
- [x] **Command Engine API:** Advanced Brigadier wrapper (`CommandEngine`, `InjectorRegistry`, `TabRegistry`) for creating complex server commands effortlessly.
- [x] **Universal Scheduler:** Cross-platform tick-based task scheduling (`LuxScheduler`, `TaskData`).
- [x] **Interactive GUI Builder:** Fluent API for chest GUIs and paginated menus (`GuiBuilder`, `PaginatedGuiBuilder`, `LuxMenu`).
- [x] **Player Abstraction:** `LuxPlayer` and `PlayerManager` for cross-platform player handling.

## 🟢 Phase 2: Data, Utility & External Hooks (Completed)
*Essential utilities for server-side developers.*
- [x] **Database & Metadata API:** Built-in `HikariDatabaseProvider` and `DatabaseService` for async database queries.
- [x] **Persistent Attributes:** `AttributeManager` and `MetadataContainer` for safely attaching custom data to players and entities.
- [x] **Discord Integration:** Out-of-the-box `DiscordWebHook` and `DiscordEmbed` builders for seamless server-to-Discord logging and events.
- [x] **Text & Chat Utilities:** `SmartMessage`, `PlaceholderManager`, and `ChatPaginator` for deep chat formatting and i18n (`LanguageManager`).
- [x] **Item & Inventory API:** `ItemBuilder` and `InventoryUtils` for safe, cross-platform item manipulation.
- [x] **Math & Cooldowns:** Built-in `CooldownManager`, `TimeGateManager`, `Cuboid` region math, and `RandomWeightedSet` tools.

## 🟢 Phase 3: Cobblemon World Boss Framework (Completed)
*The first fully realized Cobblemon-specific module.*
- [x] **WildPokemonBuilder API:** Fluent builder pattern for instantiating customized wild Pokémon (custom IVs/EVs, scale, tags, max health overrides).
- [x] **Boss UI & Scoreboard Integration:** Real-time Boss Bar synchronization (`BossHpSynchronizer`) and dynamic DPS scoreboard (`ScoreboardManager`) rendering without client-side mods.
- [x] **Smart Aggro & Targeting System:** `BossAggroManager` and `BossDamageListener` to track player DPS and manage entity threat tables.
- [x] **Minion & Phase Management:** `BossPhaseManager` for health-threshold triggers and `BossMinionManager` for dynamic add-spawns (Totem calls).
- [x] **Boss Event Hooks:** `LuxBossHooks` registry (`onBossSpawned`, `onPhaseChanged`, `onBossDefeated`) for external plugin integrations (e.g., loot distribution, quest progression).

## 🟡 Phase 4: Story-Driven Dialogue & NPC Wrapper
- [ ] **DialogueSequenceAPI:** A fluent builder to create complex, multi-branching NPC dialogue trees programmatically.
- [ ] **NPC Action Wrapper:** Simplified API to trigger specific NPC animations, sounds, or battle initiations without touching raw JSON configurations.
- [ ] **Condition-based Dialogue Routing:** Allow developers to easily route dialogues based on player inventory, active Pokémon, or custom permissions/tags.

## 🟡 Phase 5: Battle Scripting & Interceptor API
- [ ] **CustomBattleBuilder:** Abstraction for the native `BattleManager` to easily initiate forced battles between players and custom entities/NPCs.
- [ ] **BattleRuleManager:** API to inject custom battle constraints (e.g., item restrictions, level capping, legendary bans, or specific weather conditions).
- [ ] **Battle Event Interceptors:** Simplified hooks to intercept battle actions, allowing developers to forcefully end battles or trigger cutscenes at specific health thresholds.

## 🟡 Phase 6: Safe Storage & Party Transactions
- [ ] **PokemonTransactionAPI:** A highly secure wrapper for moving Pokémon between the Player's Party and PC.
- [ ] **Anti-Duplication Guards:** Built-in validation checks to prevent data loss or cloning during complex entity manipulations.
- [ ] **Property Manipulation Wrapper:** Easy-to-use methods for safely altering a caught Pokémon's properties (IVs, EVs, nature, held items) with automatic client synchronization.

## 🟡 Phase 7: Dynamic Spawning & Area Controllers
- [ ] **AreaSpawnManager:** API to dynamically restrict, boost, or alter Pokémon spawn rates in mathematically defined regions via code instead of datapacks.
- [ ] **DynamicEncounterAPI:** Tools to create temporary "Swarm" or "Mass Outbreak" events with specified species, shiny rates, and duration limits.
- [ ] **Spawn Event Interception:** Easy hooks to modify spawned Pokémon attributes right before they enter the world.

## 🟡 Phase 8: Economy & Trade Hooks
- [ ] **TradeInterceptorAPI:** Expose hooks for the native Cobblemon trading system (`TradeManager`) to allow developers to cancel trades based on custom logic (e.g., preventing legendaries from being traded).
- [ ] **GTS/Economy Helpers:** Utility classes for calculating dynamic Pokémon value/pricing based on IVs, EVs, Hidden Abilities, and Shiny status.
- [ ] **Trade Tax Injection:** Methods to inject item or currency costs into the standard player-to-player trade screen.

## 🟡 Phase 9: Cinematic & MoLang FX Wrapper
- [ ] **MoLangCinematicAPI:** High-level abstraction for Cobblemon's `ActionEffectTimeline` and `MoLang` systems.
- [ ] **Pre-built Animation Triggers:** Easy methods to force entities to play specific animations (sleep, roar, faint, eat) without developers needing to know the exact MoLang string paths.
- [ ] **Particle & Sound Utilities:** A clean wrapper around `SnowstormParticleEffect` for triggering complex move animations dynamically in the overworld.

---
*Roadmap is subject to change based on Cobblemon's upstream API updates.*