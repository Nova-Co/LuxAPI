# Changelog

All notable changes to LuxAPI are tracked here, in-repo, so version history is discoverable without needing access to any external chat/notification channel. Format loosely follows [Keep a Changelog](https://keepachangelog.com/); versions match the root `build.gradle.kts` `version` field.

Per-release detail (when a release warrants more than a summary line) lives in `changelogs/<version>.md`. Day-to-day development history and in-progress work is tracked in [`TODO.md`](TODO.md) — this file is the release-facing summary, not a duplicate of it.

## [1.2.5] - Unreleased

- Module cleanup pass (Phase 13): removed the empty `discord` module stub, fixed `bukkit`'s shaded-jar artifact name colliding with `economy`'s real `LuxEcoCore` product name, guarded `LuxConfig.reload()` against clobbering non-persisted subclass fields, added a log warning for `LootManager.distribute()` on an unregistered loot ID, and synced this file's/README's version references.
- See `changelogs/1.2.5.md` for the full detail.

## [1.2.4] and earlier

No per-version changelog was kept before this file was introduced. See `TODO.md` for the authoritative phase-by-phase history (Phases 1–12 complete) and `git log` for commit-level detail.
