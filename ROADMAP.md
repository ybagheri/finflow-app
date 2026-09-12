# FinFlow Roadmap

Delivery is split into 6 phases. Each phase ends with a conventional commit, a push to `main`, and a checkbox update here.

## Phase 1 — Foundation ✅
- [x] Gradle Kotlin DSL + version catalog (AGP 8.5.2, Kotlin 2.0.20, Java 17, SDK 26/35)
- [x] Dependencies: Compose BOM, Material 3, Navigation, Room, Hilt (KSP), Coroutines, DataStore, Vico, Biometric, Glance
- [x] Room v1: Transaction, Category, Budget, Goal, RecurringRule entities + DAOs + FinFlowDatabase
- [x] Clean Architecture: domain models + repository contracts, data mappers + Room impls, Hilt modules
- [x] Navigation skeleton (bottom bar + FAB), theme skeleton, Home/Transactions/AddEdit/Categories screens + view-models
- [x] Currency/Date utils, default category seeds, unit-test smoke, launcher icon, docs (README/ROADMAP/ADR)
- [x] Commit + push: `feat(foundation): phase 1 ...`

## Phase 2 — Core UI & Transactions ✅
- [x] Home balance card + recent-transactions preview
- [x] Full Add/Edit screen (type toggle, category picker, date picker, amount validation, payment method)
- [x] Transaction list: sort (date/amount/category asc/desc), search, type + category filters, swipe-to-delete
- [x] Category management dialog (create/rename/recolor/delete custom)
- [x] Empty states, loading/error states
- [x] Commit + push: `feat(transactions): phase 2 ...`

## Phase 3 — Charts & Reports ⬜
- [ ] Vico pie (category breakdown, income vs expense toggle) + line/bar (monthly trends)
- [ ] Reports screen: monthly/yearly summary, category totals, net balance, top categories
- [ ] Export CSV + PDF (Storage Access Framework share)
- [ ] Commit + push: `feat(reports): phase 3 ...`

## Phase 4 — Advanced Features ⬜
- [ ] Budgets: monthly caps, progress bars, overspend alerts (notifications)
- [ ] Goals: CRUD + progress tracking + deposits
- [ ] Recurring transactions: rules CRUD + WorkManager materialization
- [ ] Smart insights: MoM deltas ("32% more on Food"), daily average, streaks
- [ ] Commit + push: `feat(advanced): phase 4 ...`

## Phase 5 — Polish & Extras ⬜
- [ ] Dark/Light (+ dynamic color) theme settings
- [ ] Onboarding flow + beautiful empty states
- [ ] Biometric app lock
- [ ] Glance home-screen widget (balance + today's spending)
- [ ] Haptics, micro-interactions, motion
- [ ] Multi-currency settings (IRR/USD editable rate)
- [ ] Real screenshots in README
- [ ] Commit + push: `feat(polish): phase 5 ...`

## Phase 6 — CI/CD & Release ⬜
- [ ] GitHub Actions: build debug APK on push to main + upload artifact (already live from Phase 1, extend with lint/unit tests)
- [ ] Signed release build + versioning + release tag
- [ ] Final README (build instructions, screenshots, store listing draft)
- [ ] Commit + push: `chore(release): phase 6 ...`
