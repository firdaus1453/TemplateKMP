---
name: kmp-cmp-app-builder
description: >
  Use when building, developing, or shipping Kotlin Multiplatform (KMP) and Compose Multiplatform
  (CMP) applications targeting Android, iOS, and Desktop. Covers the full project lifecycle: initial
  setup with multi-module Clean Architecture, active development (adding features, networking, database,
  authentication, offline-first sync, testing), and production release (ProGuard, signing, CI/CD).
  Use this skill when the user asks to create a KMP or CMP app, add feature modules, implement
  networking or database layers, set up authentication, write tests, prepare release builds, or
  follow multiplatform code conventions — even if they don't explicitly mention "multiplatform" or
  "KMP", such as asking to share code between Android and iOS or build a cross-platform app.
compatibility: >
  Requires Android Studio Ladybug or later (or IntelliJ IDEA with KMP plugin), JDK 17+,
  Xcode 15+ for iOS targets, Kotlin 2.1+, and Compose Multiplatform 1.7+.
metadata:
  author: kmp-expert
  version: "4.0"
---

# KMP/CMP Application Builder

Covers the **complete lifecycle** of a KMP/CMP application: setup → development → production.

## When to use

**Phase 1 — Setup**: Creating a new project, configuring Gradle, convention plugins, core modules.
**Phase 2 — Development**: Adding features, implementing networking/database/auth, offline-first, testing.
**Phase 3 — Production**: ProGuard/R8, signing, CI/CD, performance, release builds.

## Architecture

Use **multi-module Clean Architecture**:

```
root/
├── build-logic/convention/    # Gradle convention plugins
├── composeApp/                # Composition root (all platforms)
├── core/
│   ├── domain/                # Pure Kotlin: models, Result, interfaces
│   ├── data/                  # Ktor, DataStore, session, HttpClientFactory
│   ├── presentation/          # UiText, shared composables
│   └── designsystem/         # Theme, colors, typography, components
├── feature/<name>/
│   ├── domain/                # Feature models + repository interfaces
│   ├── data/                  # Repository implementation
│   ├── database/              # Room entities + DAOs (optional)
│   └── presentation/         # ViewModel (MVI) + screens
├── gradle/libs.versions.toml
└── settings.gradle.kts
```

### Dependency rules (CRITICAL)

```
composeApp → core/* + feature/*/
feature/*/presentation → feature/*/domain + core/presentation + core/designsystem
feature/*/data → feature/*/domain + core/domain + core/data
feature/*/database → feature/*/domain
core/data → core/domain
core/presentation → core/domain + core/designsystem
```

- **domain** = pure Kotlin, ZERO framework imports
- **data** implements domain interfaces
- **presentation** depends on domain only, never on data
- Feature modules NEVER depend on other feature modules
- **composeApp** wires all DI and navigation

---

## Phase 1: Setup

1. **Initialize project** — `settings.gradle.kts`, `libs.versions.toml`, root `build.gradle.kts`, `build-logic/`.
   See [PROJECT_SETUP.md](references/PROJECT_SETUP.md).

2. **Create convention plugins** — `KmpLibrary`, `CmpLibrary`, `CmpFeature`, `CmpApplication`, `Room`, `BuildKonfig`, `Kover`.
   See [CONVENTION_PLUGINS.md](references/CONVENTION_PLUGINS.md).

3. **Set up core modules** — domain (Result, DataError), data (HttpClientFactory, auth), designsystem (AppTheme), presentation (UiText).
   See [CORE_MODULES.md](references/CORE_MODULES.md).

4. **Wire composeApp** — Koin DI graph, NavHost, platform entry points.
   See [APP_WIRING.md](references/APP_WIRING.md).

---

## Phase 2: Development

### Adding a feature module

- [ ] Add module entries to `settings.gradle.kts`
- [ ] Create `feature/<name>/domain/` — models + repository interface
- [ ] Create `feature/<name>/data/` — repository implementation
- [ ] Create `feature/<name>/database/` — Room (if needed)
- [ ] Create `feature/<name>/presentation/` — ViewModel (MVI), State, Action, Event, Screen
- [ ] Create Koin DI module, register in `composeApp`
- [ ] Add navigation route to NavHost
- [ ] Write unit tests (ViewModel + repository) with fakes
- [ ] Validate: `scripts/validate-module.sh feature/<name>`

See [FEATURE_MODULES.md](references/FEATURE_MODULES.md) for complete patterns.

### Security & authentication

- [ ] Configure `HttpClientFactory` with Bearer auth + automatic token refresh
- [ ] Store API keys via BuildKonfig from `local.properties`
- [ ] Implement `SessionStorage` with DataStore
- [ ] Create `AuthService` interface → `KtorAuthService` implementation

See [SECURITY.md](references/SECURITY.md).

### Offline-first features

- [ ] Implement `OfflineFirst*Repository` — Room DB as source of truth + network sync
- [ ] UI observes Room `Flow`, NOT network responses
- [ ] Add `ConnectivityObserver` (expect/actual per platform)
- [ ] Add `ConnectionRetryHandler` for exponential backoff

See [OFFLINE_FIRST.md](references/OFFLINE_FIRST.md).

### Testing & coverage

- [ ] Create `Fake*` stubs for all interfaces
- [ ] Write ViewModel tests with `runTest` + `Turbine`
- [ ] Apply Kover to testable modules
- [ ] Run: `./gradlew allTests koverHtmlReport`
- [ ] Integrate Chucker for Android debug HTTP inspection

See [TESTING.md](references/TESTING.md).

### Code conventions

Follow naming rules and patterns throughout development.
See [CODE_CONVENTIONS.md](references/CODE_CONVENTIONS.md).

### Debugging quick reference

| Problem | Solution |
|---------|----------|
| HTTP issues | Chucker (Android), Ktor Logging (all) |
| DI resolution fails | Core modules must load before features in Koin |
| Database migration error | Check Room schema dir, verify migration steps |
| Flow not emitting | Check `SharingStarted.WhileSubscribed(5_000)` |
| Platform crash | Separate `expect`/`actual`, use Kermit for logging |

---

## Phase 3: Production

Configure ProGuard, signing, CI/CD, optimize performance, and run the pre-release checklist.
See [PRODUCTION.md](references/PRODUCTION.md).

### Validation before release

1. `./gradlew build` — compiles without errors
2. `./gradlew allTests` — all tests pass
3. `./gradlew koverVerify` — coverage ≥ 60%
4. `./gradlew :composeApp:assembleRelease` — release APK builds
5. `./gradlew :composeApp:bundleRelease` — release AAB for Play Store
6. Test release build on physical device

---

## Gotchas

- Token refresh must skip auth endpoints — otherwise infinite loop when refresh token expires.
- Always call `BearerAuthProvider.clearToken()` on logout — cached tokens persist.
- API keys go in `local.properties` only — never commit secrets.
- Offline-first UI must observe Room `Flow`, not network responses.
- Never catch `CancellationException` — always re-throw.
- Chucker is Android-only — use `no-op` variant for release builds.
- `TYPESAFE_PROJECT_ACCESSORS` must be enabled in `settings.gradle.kts`.
- DataStore file path needs `expect`/`actual` — each platform stores differently.
- Desktop target needs `kotlinx-coroutines-swing` for coroutine dispatching.
- Navigation Compose routes must be `@Serializable` data objects/classes.
- ProGuard must keep `@Serializable` classes — JSON parsing breaks in release.
- Always use `bundleRelease` (AAB) for Play Store, not `assembleRelease` (APK).
- Kover `koverVerify` will fail CI if coverage drops below minimum bound.
- Use `Dispatchers.setMain(testDispatcher)` in `@BeforeTest` and `resetMain()` in `@AfterTest`.
- Never use `println()` or `Log.d()` — use Kermit (multiplatform) or Timber (Android-only).

## Technology stack

| Technology | Purpose | Version |
|-----------|---------|---------|
| Kotlin | Language | 2.1+ |
| Compose Multiplatform | UI framework | 1.7+ |
| Koin | DI | 4.0+ |
| Ktor | HTTP + WebSockets | 3.0+ |
| Room | Database (offline-first) | 2.7+ |
| Navigation Compose | Navigation | Aligned with CMP |
| DataStore | Session storage | 1.1+ |
| Kover | Test coverage | 0.9+ |
| Turbine | Flow testing | 1.2+ |
| Chucker | HTTP debugging (Android) | 4.1+ |
| BuildKonfig | Build constants | 0.15+ |
| Kermit | Multiplatform logging | 2.0+ |
| Coil | Image loading | 3.0+ |
| MOKO Permissions | Permissions | 0.18+ |
