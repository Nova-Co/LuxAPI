# Changelog

All notable changes to LuxAPI are tracked here, in-repo, so version history is discoverable without needing access to any external chat/notification channel. Format loosely follows [Keep a Changelog](https://keepachangelog.com/); versions match the root `build.gradle.kts` `version` field.

Per-release detail (when a release warrants more than a summary line) lives in `changelogs/<version>.md`. Day-to-day development history and in-progress work is tracked in [`TODO.md`](TODO.md) — this file is the release-facing summary, not a duplicate of it.

## [1.10.1] - Unreleased

- Maven coordinates changed: the group is now `io.github.nova-co` (was `com.novaco.luxapi`). Kotlin/Java source packages (`com.novaco.luxapi.*`) are unchanged, so only dependency declarations in consumer builds need updating.
- Publishing now targets Maven Central through the `com.vanniktech.maven.publish` plugin, with a manual/release-triggered `publish.yml` workflow that builds and tests before publishing.
- README usage and dependency instructions updated for the new coordinates.

## [1.10.0] - 2026-09-08

- Cobblemon 1.8.0 coverage (Phase 16): `TMManager`, Alpha Pokémon support, habitat query/interceptor, `BattleInterceptor.onBattleFlee`, and NPC `PartyCompositionManager`/`useComposition`.
- `cobblemon` module library-quality audit and fix pass (Phase 17): a raw-mutation IV/EV bug in `WildPokemonBuilder`, a thread-safety bug in `SwarmManager`, a dead `BossHpSynchronizer.tick()` wiring gap, a missing `ObservableSubscription` return on `ApricornManager`, a `PartyManager`/`PCStorageManager` duplication, missing `HookManager` unregister methods, and a slf4j logging pass across 8 files.
- See `changelogs/1.10.0.md` for the full detail.

## [1.4.0] - Unreleased

- Added the `discord` module: a JDA-based bot API (lifecycle wrapper, slash command framework, button/modal registry, JDA→`EventBus` bridge, role/user helpers). Primitives only — no bundled linking/role-sync/notification features.
- See `changelogs/1.4.0.md` for the full detail.

## [1.2.5] - Unreleased

- Module cleanup pass (Phase 13): removed the empty `discord` module stub, fixed `bukkit`'s shaded-jar artifact name colliding with `economy`'s real `LuxEcoCore` product name, guarded `LuxConfig.reload()` against clobbering non-persisted subclass fields, added a log warning for `LootManager.distribute()` on an unregistered loot ID, and synced this file's/README's version references.
- See `changelogs/1.2.5.md` for the full detail.

## [1.2.4] and earlier

No per-version changelog was kept before this file was introduced. See `TODO.md` for the authoritative phase-by-phase history (Phases 1–12 complete) and `git log` for commit-level detail.
