# ADR-002: Room as offline-first source of truth, Vico for charts

Date: 2026-09-12 · Status: Accepted

## Context
The app must work fully offline; charts must be pure-Compose and Material 3 friendly.

## Decision
- Room is the single source of truth (Flow streams to ViewModels). No network layer in v1.
- `exportSchema = true`; migrations are explicit and non-destructive in release.
- Charts: Vico (`compose`, `views`, `compose-m3`) — pure Compose, M3 theming, pie + line/bar coverage.
- PDF export later uses Android's `PdfDocument` (no extra dependency); CSV is hand-rolled.

## Consequences
+ Zero-backend, instant local reads, simple backup story.
+ Vico avoids WebView/native chart bridges.
- Future sync will need a sync-status column and conflict policy (deferred to post-v1).
