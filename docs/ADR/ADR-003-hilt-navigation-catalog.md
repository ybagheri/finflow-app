# ADR-003: Hilt (KSP) + Navigation Compose + version catalog

Date: 2026-09-12 · Status: Accepted

## Context
Need standard DI, type-safe-ish navigation, and reproducible dependency versions across CI and dev machines.

## Decision
- DI: Hilt with KSP (`hilt-compiler`), app-level `@SingletonComponent` modules.
- Navigation: Navigation Compose with a `Routes` object; single `MainActivity`.
- Versions centralized in `gradle/libs.versions.toml`; Kotlin 2.0.20 + AGP 8.5.2 + Java 17.

## Consequences
+ Google-recommended stack, strong docs, CI-friendly.
- Hilt/KSP bumps require coordinated Kotlin/AGP upgrades; catalog makes that one edit.
