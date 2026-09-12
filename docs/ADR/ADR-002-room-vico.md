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

## Addendum (Phase 3, 2026-09-12)
Charts shipped hand-rolled on Compose Canvas (`ReportCharts.kt`: donut +
grouped bars) instead of Vico. Rationale: Vico 1.x → 2.x API churn made the
declared 1.13.1 chart API a compile risk with no local SDK to verify against;
Canvas covers exactly the two charts the roadmap needs (category breakdown +
monthly trends) with M3 colors and zero extra API surface. The Vico
dependencies stay declared in the catalog so a future migration is a pure UI
swap with no data-layer changes.
