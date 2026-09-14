# FinFlow — Modern Personal Finance Tracker

All 6 phases complete: foundation, transactions, reports, advanced features, polish and CI/CD. See [ROADMAP.md](ROADMAP.md).

FinFlow is an offline-first Android personal finance tracker built with Kotlin, Jetpack Compose + Material 3, Room, Hilt and Navigation Compose.

## Features

| Area | Shipped |
|---|---|
| Transactions (income/expense, amount, category, date, note, payment method) | Full add/edit UI, sorting, search, type + category filters, swipe-to-delete |
| Charts & reports | Category donut, monthly trends, monthly/yearly summary, net balance, top categories, CSV/PDF export |
| Budgets | Monthly per-category caps, progress bars, one-shot overspend notifications |
| Goals | CRUD, deposits, progress bars, optional deadlines |
| Recurring transactions | Rules CRUD, active toggles, next-due dates, daily WorkManager materialization + manual run |
| Smart insights | Home card: month-over-month movers, 30-day daily average, logging streak |
| Personalization & security | System/light/dark theme + dynamic color, onboarding, biometric app lock |
| Widget | Glance home-screen widget: balance + today's spending, tap to open |
| Multi-currency | IRR/USD display currency with user-editable rate (settings → home + widget) |
| CI/CD | Lint + unit tests + debug APK on every push; release APK attached to `v*` tags |

## Screenshots

> Screenshots are captured on a device/emulator during release QA and stored under `docs/screenshots/`.

| Home | Transactions | Reports |
|---|---|---|
| `docs/screenshots/home.png` | `docs/screenshots/transactions.png` | `docs/screenshots/reports.png` |

## Tech Stack

- **Language:** Kotlin 2.0.20
- **UI:** Jetpack Compose (BOM 2024.09.00) + Material 3
- **Architecture:** MVVM + Clean (data / domain / presentation)
- **DB:** Room 2.6.1 (offline-first)
- **Charts:** Hand-rolled Canvas charts (see `docs/ADR/ADR-002-room-vico.md` addendum)
- **DI:** Hilt 2.51.1 (KSP)
- **Navigation:** Navigation Compose 2.7.7
- **Async:** Coroutines 1.8.1 + Flow
- **Settings:** DataStore Preferences
- **Background:** WorkManager 2.9.0 (recurring transactions)
- **Security:** AndroidX Biometric (optional app lock)
- **Widget:** Glance 1.1.1
- **Min SDK 26 · Target/Compile 35 · Java 17 · Gradle Kotlin DSL + version catalog**

## Architecture Overview

```
com.finflow.app
├── data/
│   ├── local/entity/  Transaction, Category, Budget, Goal, RecurringRule
│   ├── local/dao/     TransactionDao, CategoryDao, BudgetDao, GoalDao, RecurringRuleDao
│   ├── local/db/      FinFlowDatabase (v1)
│   ├── mapper/        Entity <-> domain pure functions
│   ├── prefs/         UserPreferences (DataStore: theme, currency, lock, onboarding)
│   ├── work/          RecurringScheduler + RecurringWorker (WorkManager)
│   └── repository/    Room-backed impls + default category seeds
├── domain/
│   ├── model/         Transaction, Category, Budget, Goal, RecurringRule, Sort
│   └── repository/    Interfaces (single source of truth contracts)
├── di/                DatabaseModule, RepositoryModule, CoroutineModule
├── core/util/         DateUtils, CurrencyUtils, ReportUtils, InsightsUtils, Notifications
└── presentation/
    ├── navigation/    Routes + FinFlowNavGraph (bottom bar + FAB + onboarding)
    ├── theme/         FinFlowTheme (mode + dynamic color)
    ├── components/    EmptyState, TransactionRow
    ├── widget/        BalanceWidget (Glance)
    └── screens/       home, transactions, addedit, categories, reports,
                       budgets, goals, recurring, more, onboarding, settings
```

Decisions are recorded in [docs/ADR](docs/ADR).

## How to Build

Requirements: JDK 17, Android SDK (API 35), no local Gradle install needed (wrapper).

```bash
git clone https://github.com/ybagheri/finflow-app.git
cd finflow-app
./gradlew assembleDebug        # APK -> app/build/outputs/apk/debug/
./gradlew testDebugUnitTest    # JVM unit tests
./gradlew lintDebug            # Android Lint (report: app/build/reports/)
./gradlew connectedCheck        # on emulator/device
```

CI runs lint + unit tests + the debug build on every push to `main` and uploads the APK as an artifact. Pushing a `v*` tag builds the release APK and attaches it to the GitHub Release.

### Signed release builds

Release signing is optional and never committed. Provide credentials **either** as a git-ignored `keystore.properties` file:

```properties
storeFile=/absolute/path/finflow-release.jks
storePassword=***
keyAlias=finflow
keyPassword=***
```

**or** as environment variables / CI secrets: `KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.

Then:

```bash
./gradlew assembleRelease      # APK -> app/build/outputs/apk/release/
```

Without credentials the release build is unsigned (suitable for Play App Signing upload flows).

## Store Listing (draft)

- **Name:** FinFlow — Personal Finance Tracker
- **Short description:** Offline-first money tracker: budgets, goals and smart insights.
- **Full description:** FinFlow keeps your money organized without an account or a connection. Log income and expenses in seconds, cap spending with monthly budgets and overspend alerts, save towards goals, automate repeats, and see where your money goes with charts, reports and CSV/PDF export. Optional biometric lock, dark theme with dynamic color, IRR/USD support, and a home-screen widget.
- **Category:** Finance · **Content rating:** Everyone · **Price:** Free

## Project Structure

- `app/` — single Android module
- `gradle/libs.versions.toml` — centralized dependency catalog
- `docs/ADR/` — architecture decision records
- `ROADMAP.md` — phased delivery plan + checklists

## Roadmap

See [ROADMAP.md](ROADMAP.md) — 6 phases from foundation to CI/CD release (all complete).

## License

MIT — see [LICENSE](LICENSE).
