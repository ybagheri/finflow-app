# ADR-001: MVVM + Clean Architecture with a single `:app` module

Date: 2026-09-12 · Status: Accepted

## Context
FinFlow needs clear testability and phased delivery by a small team without multi-module build overhead.

## Decision
- Layers: `data` (Room/DataStore) → `domain` (models + repository contracts) → `presentation` (Compose + ViewModels).
- ViewModels depend only on `domain/repository` interfaces; Hilt binds Room impls.
- Entity↔domain mapping lives in pure `data/mapper` functions.
- Stay with a single `:app` module until build times or team size force extraction.

## Consequences
+ Simple Gradle setup, fast CI, easy navigation for contributors.
+ Domain is unit-testable without Android.
- Feature boundaries are by package, not enforced by Gradle; discipline required.
