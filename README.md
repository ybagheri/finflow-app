# FinFlow — Modern Personal Finance Tracker

> **Status: Phase 1 — Foundation complete.** Database, DI, navigation skeleton and reactive totals are live. See [ROADMAP.md](ROADMAP.md).

FinFlow is an offline-first Android personal finance tracker built with Kotlin, Jetpack Compose + Material 3, Room, Hilt and Navigation Compose.

## Features

| Area | Phase 1 | Planned |
|---|---|---|
| Transactions (income/expense, amount, category, date, note, payment method) | DB + repository + skeleton add/list screens | Full add/edit UI, sorting, search & filter (Phase 2) |
| Charts (pie + line/bar, income & expenses) | Vico dependency wired | Phase 3 |
| Reports (monthly/yearly, category totals, net balance, top categories, CSV/PDF export) | Placeholder screen | Phase 3 |
| Budgets per category + overspend alerts | Entity + DAO + repository | UI + workers Phase 4 |
| Financial goals + progress | Entity + DAO + repository | UI Phase 4 |
| Recurring transactions | Rule entity + DAO | Scheduler Phase 4 |
| Smart insights | — | Phase 4 |
| Dark/Light theme, onboarding, biometric lock, widget, animations | Theme skeleton | Phase 5 |
| CI (debug APK artifact) + release | This repo builds via GitHub Actions | Signed release Phase 6 |
| Multi-currency (IRR + USD) | `CurrencyUtils` static 42,000 rate + formatter | Editable rate Phase 5 |

## Screenshots

> Placeholders — real screenshots land in Phase 5 polish.

| Home | Transactions | Reports |
|---|---|---|
| `docs/screenshots/home.png` | `docs/screenshots/transactions.png` | `docs/screenshots/reports.png` |

## Tech Stack

- **Language:** Kotlin 2.0.20
- **UI:** Jetpack Compose (BOM 2024.09.00) + Material 3
- **Architecture:** MVVM + Clean (data / domain / presentation)
- **DB:** Room 2.6.1 (offline-first)
- **Charts:** Vico 1.13.1 (compose + compose-m3)
- **DI:** Hilt 2.51.1 (KSP)
- **Navigation:** Navigation Compose 2.7.7
- **Async:** Coroutines 1.8.1 + Flow
- **Settings:** DataStore Preferences
- **Min SDK 26 · Target/Compile 35 · Java 17 · Gradle Kotlin DSL + version catalog**

## Architecture Overview

```
com.finflow.app
├── data/
│   ├── local/entity/  Transaction, Category, Budget, Goal, RecurringRule
│   ├── local/dao/     TransactionDao, CategoryDao, BudgetDao, GoalDao, RecurringRuleDao
│   ├── local/db/      FinFlowDatabase (v1)
│   ├── mapper/        Entity <-> domain pure functions
│   └── repository/    Room-backed impls + default category seeds
├── domain/
│   ├── model/         Transaction, Category, Budget, Goal, RecurringRule, Sort
│   └── repository/    Interfaces (single source of truth contracts)
├── di/                DatabaseModule, RepositoryModule, CoroutineModule
├── core/util/         DateUtils, CurrencyUtils
└── presentation/
    ├── navigation/    Routes + FinFlowNavGraph (bottom bar + FAB)
    ├── theme/         FinFlowTheme (M3 light/dark skeleton)
    ├── components/    EmptyState
    └── screens/       home, transactions, addedit, categories, reports*, settings*
```

Decisions are recorded in [docs/ADR](docs/ADR).

## How to Build

Requirements: JDK 17, Android SDK (API 35), no local Gradle install needed (wrapper).

```bash
git clone https://github.com/ybagheri/finflow-app.git
cd finflow-app
./gradlew assembleDebug        # APK -> app/build/outputs/apk/debug/
./gradlew test                 # JVM unit tests
./gradlew connectedCheck        # on emulator/device
```

CI builds the same APK on every push to `main` and uploads it as an artifact.

## Project Structure

- `app/` — single Android module
- `gradle/libs.versions.toml` — centralized dependency catalog
- `docs/ADR/` — architecture decision records
- `ROADMAP.md` — phased delivery plan + checklists

## Roadmap

See [ROADMAP.md](ROADMAP.md) — 6 phases from foundation to CI/CD release.

## License

TBD (MIT recommended for Phase 6).
