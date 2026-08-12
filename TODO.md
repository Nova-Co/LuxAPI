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

## 🟢 Phase 4: Story-Driven Dialogue & NPC Wrapper (Completed)
- [x] **DialogueSequenceAPI:** `DialogueBuilder` — fluent multi-page dialogue trees with text/choice/input/auto-advance pages, custom speakers (player-skin, NPC-model, and Pokémon-species/`addPokemonSpeaker` portraits), dynamic per-render text (`addDynamicPage`), custom timeout/escape/init actions (`onTimeout`, `onEscape`, `onInitialize`, page-level `escapeAction`), a code↔JSON-registry bridge (`buildAndRegister`/`openRegistered`), and background/gibber customization. Implemented — closed all 6 gaps found against Cobblemon `1.7.3`'s `api/dialogue` surface. **Known fragility:** touching these files surfaced 4 latent (pre-existing, not introduced this session) compiler warnings — `Cannot access class 'com.bedrockk.molang.Expression'... may be forbidden soon` — same root cause as the AI-task-wrapper classpath gap (`com.bedrockk.molang` not on the compile classpath, `isTransitive = false`). Currently harmless (build succeeds), but worth a real fix (see Phase 11) before a future Kotlin compiler version turns it into a hard error.
- [x] **NPC Action Wrapper:** `NPCExtensions.initiateBattle()` covers battle initiation; `NPCFxExtensions.kt` adds `NPCEntity.playAnimation(BattleAnimation)` (enum-safe wrap of Cobblemon's own `SEND_OUT`/`RECALL`/`WIN`/`LOSE` battle animations) and `NPCEntity.playBattleTheme()`. Note: no separate `playAnimation(String, ...)`/`playSound(...)` wrappers were added — `NPCEntity` already exposes both as public members (`PosableEntity.playAnimation`, vanilla `Entity.playSound`) with identical signatures, so a same-signature extension would be dead, compiler-shadowed code; call those directly on the entity instead. Implemented.
- [x] **Condition-based Dialogue Routing:** `ChoicePageBuilder.option(isVisible, isSelectable, ...)` accepts arbitrary predicate lambdas over `ActiveDialogue`/player state — developers can already route on inventory, active Pokémon, or custom tags this way. Implemented.

## 🟡 Phase 5: Battle Scripting & Interceptor API (Partially Complete)
- [x] **CustomBattleBuilder:** `LuxBattleBuilder` (`battle/BattleBuilder.kt`) + `BattleExtensions.kt` — wild/player/NPC forced battles, double-battle toggle. Implemented.
- [ ] **BattleRuleManager:** Still missing. `LuxBattleBuilder` only exposes `setDoubleBattle`/`setSpectatorAllowed` — no item restrictions, level capping, legendary bans, or weather constraints.
- [ ] **Battle Event Interceptors:** Still missing as originally scoped. `npc/battle/BattleEventListener.kt` only does *post-battle* result routing (victory/defeat → `BattleRegistry` callback) via `CobblemonEvents.BATTLE_VICTORY`/`BATTLE_FAINTED` — there's no mid-battle interception to forcefully end battles or trigger cutscenes at health thresholds. The event-subscription foundation exists and can be extended.

## 🟡 Phase 6: Safe Storage & Party Transactions (Partially Complete)
- [ ] **PokemonTransactionAPI:** Partially implemented — `PCStorageManager`/`StorageExtensions.kt` cover PC box-to-box moves (now packet-synced, fixed this session), but there is no wrapper for transferring a Pokémon between a Player's Party and PC.
- [x] **Anti-Duplication Guards (PC-side):** `PCStorageManager.move()` now delegates to `PCStore.swap()` instead of raw box mutation, closing the client-desync dupe-risk found this session. Note: a separate, more serious duplication/data-loss gap exists in the *GTS* system — see Phase 8.
- [ ] **Property Manipulation Wrapper:** Still missing. No safe, sync-aware method exists for altering a caught Pokémon's IVs/EVs/nature/held item post-catch.

## 🟡 Phase 7: Dynamic Spawning & Area Controllers (Partially Complete)
- [ ] **AreaSpawnManager:** Still missing. No API to restrict/boost spawn rates in a defined region via code.
- [x] **DynamicEncounterAPI:** `SwarmManager`/`SwarmEvent` — timed swarm/outbreak events with species, level range, radius, and active-entity cap, driven by a self-cancelling scheduled task. Implemented.
- [ ] **Spawn Event Interception:** Still missing. `PokemonSpawner.kt` is a direct-spawn helper, not a hook into natural Cobblemon spawn events.

## 🔴 Phase 8: Economy & Trade Hooks (Largely Incomplete — Critical Gap Found)
- [ ] **TradeInterceptorAPI:** Still missing. No hook into Cobblemon's native player-to-player `TradeManager`.
- [x] **GTS/Economy Helpers (appraisal):** `PokemonPriceCalculator` + `LuxPokemonEconomy`'s pluggable `PokemonAppraiser` chain for dynamic valuation are implemented and solid.
- [ ] ⚠️ **GTS Listing/Purchase is a non-functional stub with a live dupe/data-loss bug:** `GlobalTradeManager.listPokemon()` serializes to a literal placeholder string (`"mock_base64_data_for_now"`) and its own comments admit the seller's Pokémon is never actually removed from their party — anyone using this as-is could list a Pokémon and keep it. `purchaseListing()` mirrors this: it returns `Pokemon()` (a blank default), not the Pokémon that was actually listed — buyers would receive nothing. This needs real serialization (see `serialization/PokemonSerializer.kt`, already in the module) and actual party-removal/grant logic before this system is safe to ship, not just "more features."
- [ ] **Trade Tax Injection:** Still missing.

## 🟢 Phase 9: Cinematic & MoLang FX Wrapper (Mostly Complete)
- [ ] **MoLangCinematicAPI:** Still missing — `BossFxWrapper` sends direct packets for pre-defined enum effects, it does not wrap Cobblemon's `ActionEffectTimeline`/MoLang system generically.
- [x] **Pre-built Animation Triggers:** `BossFxWrapper.GenericAnimation` + `playAnimation()`/`playCry()`/`playEntityEvent()`. Implemented.
- [x] **Particle & Sound Utilities:** `playSnowstormEffect()`, `playMoveParticle()`, `playSound()`, `spawnParticles()` — comprehensive wrapper around Cobblemon's Snowstorm packets and vanilla particles/sounds. Implemented.

## 🟡 Phase 10: Cobblemon Feature Coverage Gaps
*Verified against Cobblemon `1.7.3` source — LuxAPI currently has zero wrapper for these, even though the underlying Cobblemon API exists. (Breeding was checked and does not exist in Cobblemon 1.7.3 — not a gap.)*
- [ ] **Pasture/Daycare Wrapper:** Cobblemon exposes a full pasture API (`api/pasture/PastureLink`, `PastureLinkManager`, `PasturePermissionController(s)`, plus `PastureBlock`/`PokemonPastureBlockEntity`) — LuxAPI has no wrapper for depositing/managing Pokémon in pastures.
- [ ] **Riding API Wrapper:** Cobblemon has a full riding system (`api/riding/behaviour/RidingBehaviour(s)`, `RidingController`, `RidePokemonEvent`) — no LuxAPI wrapper exists to query or hook into ridden Pokémon.
- [ ] **Regional/Multi-Dex Pokédex Support:** `PokedexManager` currently only checks CAUGHT/SEEN by string-matching a single flat knowledge record and estimates completion against a hardcoded `totalAvailableSpecies = 1025` default. Cobblemon's actual Pokédex API (`api/pokedex/def/PokedexDef`, `Dexes`, `PokedexEntryProgress`, `FormDexRecord`) supports multiple named/regional dexes and per-form progress — LuxAPI doesn't expose any of that depth.

## 🟡 Phase 11: Distribution & Production Readiness
*The real gap between LuxAPI and EnvyWare/API-tier maturity is not Cobblemon feature coverage — it's publishing, testing, and process discipline.*
- [ ] **Remote Maven Publishing:** The `maven-publish` plugin is already wired up repo-wide (see root `build.gradle.kts`) and produces a working `MavenPublication` per module — but it only publishes to a local build directory (`build/repo`), never to a real remote host. Add a real remote repository target (e.g., a self-hosted Reposilite/Nexus instance or GitHub Packages) with credentials, matching EnvyWare's `maven.envyware.co.uk` model.
- [ ] **CI Build/Test Gate:** The only existing workflow (`.github/workflows/discord_notify.yml`) posts a Discord changelog embed on push — it does not run `./gradlew build`/`test`. Add an actual CI workflow that compiles and runs tests on every push/PR, so regressions like this session's `isPerfect()` bug and the GTS stub don't reach `main` unnoticed.
- [ ] **Real Test Coverage:** The entire `cobblemon` module has exactly one test file (`EvolutionHookManagerTest.kt`), and it's `@Disabled`. Effectively 0% enforced coverage — this is how the `isPerfect()`/PC-desync bugs shipped undetected. Prioritize tests for `PCStorageManager`, `PokemonExtension`, and especially `GlobalTradeManager` once Phase 8's stub is fixed.
- [ ] **Discord Module — Re-scope or Remove:** `discord/src` currently has zero source files (only a stale `.iml` and leftover local `build/` artifacts up to v1.2.4, suggesting it had real content before). `commons` already ships `DiscordWebHook`/`DiscordEmbed` (Phase 2). Before rebuilding this module, decide what it should contain that isn't already covered by `commons` — otherwise remove the empty stub from `settings.gradle.kts` rather than leaving a dead module.
- [ ] **Version/Changelog Sync:** `README.md` states `Version: 1.1.1`, but root `build.gradle.kts` is actually at `1.2.4` — docs are stale relative to the build. Establish a single source of truth and a changelog process (EnvyWare uses a Discord `#api-changelog` channel + LGPL-documented releases).

---
*Roadmap is subject to change based on Cobblemon's upstream API updates.*