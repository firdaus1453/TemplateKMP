# Kotlin Multiplatform (KMP/CMP) Workspace Rules

Whenever you are working on this KMP/CMP repository (doing development, adding features, debugging, refactoring, or writing tests), you MUST follow these workflow disciplines:

## 1. Planning & Approval Gate (MANDATORY)
Before writing, modifying, or executing any code changes on the user's system, you MUST:
1. Create a detailed, step-by-step **Implementation Plan** outlining exactly which files will be created or modified, which architecture patterns will be applied, and how the changes will be tested.
2. Present this plan to the user and wait for their explicit approval.
3. **NEVER** edit files or execute terminal commands to modify code until the user has reviewed and approved your plan.

---

## 2. File-to-Skill Mapping & Resolution Protocol (MANDATORY)

Before proposing any implementation plan or writing code, you MUST identify all relevant skills and read them using `view_file`. To ensure no skill is missed (even when prompts are brief or implicit), you MUST follow the **Two-Stage Analysis & Skill Resolution Protocol** below.

### 2.1 The Two-Stage Analysis & Skill Resolution Protocol

When a prompt is received (e.g., "add bookmark feature", "fix login bug", "update search screen"):
1. **Phase 1: Code Discovery (Pencarian Berkas)**
   - Do NOT propose a plan or write code immediately.
   - Run codebase searches (`grep_search` or terminal commands) to locate the files related to the mentioned components/features.
   - Read the discovered files to identify the context.
2. **Phase 2: Three-Layer Skill Mapping (Pemetaan Skill 3-Lapis)**
   - Check the discovered files against the **Mapping Matrix** (Section 2.2) using three distinct layers:
     a. **File Path/Name Pattern**: Does the path or filename match a category?
     b. **Package & Imports**: Does the file pack declaration or its imports contain `.domain.`, `.data.`, `.presentation.`, `io.ktor.*`, `org.koin.*`, `androidx.room.*`, `androidx.compose.*`?
     c. **Semantic Indicators**: Does the code content contain specific annotations, keywords, or types (e.g., `koinViewModel()`, `viewModelOf`, `@Composable`, `StateFlow`, `Result<D, E>`, `HttpClient`, `safeCall`, `@Dao`)?
3. **Phase 3: The Loading Ceremony (Pemuatan Skill)**
   - Call `view_file` on the `SKILL.md` file of EVERY mapped skill to load its rules into your active context.
4. **Phase 4: Plan Proposing (Pengajuan Rencana)**
   - Propose your detailed implementation plan to the user, listing the discovered files and confirming the loaded skills to prove compliance.

### 2.2 The Mapping Matrix (KMP & Chirp)

For any file analyzed, referenced, or modified, map it to the corresponding skill using these rules:

| Fact Family | File Path / Name Pattern | Package / Import Indicators | Semantic Content Indicators (Keywords / Annotations / Types) | Skill to Load & Read |
|---|---|---|---|---|
| **KMP Architecture & Structure** | `build.gradle.kts`, `settings.gradle.kts`, `build-logic/**` | `com.android.kotlin.multiplatform.library`, `com.android.application` | Convention plugins, sourceSets, target configurations (`iosArm64`, `desktop`) | [kmp-architecture](skills/kmp-architecture/SKILL.md) |
| **Presentation / MVI** | `*ViewModel.kt`, `*State.kt`, `*Action.kt`, `*Event.kt` | Package `*.presentation.*` | `ViewModel()`, `StateFlow`, `Channel`, `viewModelScope`, MVI State/Action/Event, `ObserveAsEvents` | [kmp-presentation](skills/kmp-presentation/SKILL.md) |
| **Compose Multiplatform UI** | `*Screen.kt`, `core/designsystem/**` | Imports `androidx.compose.*`, `coil3.compose.*` | `@Composable`, `@Preview`, Design Tokens (`AppTheme`), `AsyncImage`, `Scaffold`, UI components | [kmp-compose-ui](skills/kmp-compose-ui/SKILL.md) |
| **Navigation & Routing** | `*Route.kt`, `App.kt`, `NavHost` | Imports `androidx.navigation.*` | `NavController`, `NavHost`, `@Serializable` routes/graphs, `navController.navigate()` | [kmp-navigation](skills/kmp-navigation/SKILL.md) |
| **Data Layer & Ktor Networking** | `*RepositoryImpl.kt`, `*DataSource.kt`, `HttpClientFactory.kt` | Package `*.data.*`, imports `io.ktor.*` | `HttpClient`, `safeCall`, `BearerTokens`, `HttpResponse`, `SessionStorage`, offline-first caching | [kmp-data](skills/kmp-data/SKILL.md) |
| **Database & Room KMP** | `*Dao.kt`, `*Entity.kt`, `*Database.kt` | Imports `androidx.room.*`, `androidx.sqlite.*` | `@Database`, `@Dao`, `@Entity`, `RoomDatabaseConstructor`, `BundledSQLiteDriver` | [kmp-database](skills/kmp-database/SKILL.md) |
| **Dependency Injection (Koin)** | `*Module.kt`, `Modules.kt`, `AppModule.kt` | Imports `org.koin.*` | `module { }`, `viewModelOf`, `singleOf`, `koinViewModel()`, `koinInject()`, `startKoin` | [kmp-di](skills/kmp-di/SKILL.md) |
| **Error Handling & Result** | Any file mapping network/local exceptions | Imports `.core.domain.Result`, `DataError` | `Result<D, E>`, `DataError.Network`, `DataError.Local`, `UiText`, `asEmptyResult()`, `.map`, `.onSuccess` | [kmp-error-handling](skills/kmp-error-handling/SKILL.md) |
| **Testing (Unit, Turbine, Maestro)** | `*Test.kt`, `.maestro/**`, `scripts/**` | Imports `kotlin.test.*`, `app.cash.turbine.*` | `@Test`, `runTest`, `Turbine`, `test {}`, `FakeRepository`, `StandardTestDispatcher`, Maestro YAML flows | [kmp-testing](skills/kmp-testing/SKILL.md) |
| **Security & Secrets** | `local.properties`, `BuildKonfig`, `SessionStorage` | Package `security`, `crypto` | `BuildKonfig`, encrypted session storage, Certificate pinning in Ktor, Proguard rules | [kmp-security](skills/kmp-security/SKILL.md) |
| **Project & Module Setup** | Adding modules, `settings.gradle.kts`, plugins | Any new module setup | Module conventions, clean architecture checklist | [kmp-project](skills/kmp-project/SKILL.md) |
| **Technical Diagrams** | `*.md` | Any technical documentation | Fenced `mermaid` code blocks | [mermaid-standards](skills/mermaid-standards/SKILL.md) |

---

## 3. The Unified Development Lifecycle (MANDATORY)

For every engineering task (Feature, Bugfix, or Refactor), the AI MUST follow this structured 5-phase lifecycle sequentially:

### Phase 1: Discovery & Skill Resolution (Pencarian & Pemetaan)
1. **Analyze the Request**: Parse the user's prompt (even if brief or implicit, e.g., "fix search pagination").
2. **Find Code Context**: Search the codebase (using `grep_search`, `list_dir`, `view_file`) to locate all related files and modules.
3. **Map and Load Skills**: Cross-reference the analyzed files against the **Mapping Matrix (Section 2.2)** by file path, package/imports, and semantics. Call `view_file` on each resolved `SKILL.md` to load guidelines.

### Phase 2: Intent-Specific Alignment (Penyelarasan Maksud)
Perform prep work based on the type of task:
- **For Features**: Trigger the **[grill-me](skills/grill-me/SKILL.md)** loop to clarify requirements, and read **[kmp-architecture](skills/kmp-architecture/SKILL.md)** to establish layer boundaries.
- **For Bugfixes**: Trigger the **[diagnosing-bugs](skills/diagnosing-bugs/SKILL.md)** loop. Identify the root cause and write a reproducing failing unit test (TDD approach from **[tdd](skills/tdd/SKILL.md)**).
- **For Refactoring**: Read **[kmp-error-handling](skills/kmp-error-handling/SKILL.md)** and **[kmp-architecture](skills/kmp-architecture/SKILL.md)** to verify boundaries.

### Phase 3: Planning & Approval Gate (Rencana & Persetujuan)
1. Formulate a detailed, step-by-step **Implementation Plan** listing:
   - Discovered files to create or modify.
   - Architecture patterns to apply (Clean Architecture, MVI, Koin DI, Ktor, Room).
   - Testing strategy (unit tests with Turbine/FakeRepository and Maestro E2E flows).
   - Mapped skills that will guide the implementation.
2. Present the plan to the user and wait for their explicit approval.
3. **NEVER** edit files or execute terminal commands to modify code until the user has approved the plan.

### Phase 4: Execution & Implementation (Eksekusi)
1. Modify or create files following the approved plan and the guidelines of the loaded skills (Clean Architecture, Chirp MVI, Koin DI, Compose UI, etc.).
2. Adhere to code hygiene rules (pure Kotlin domain, Result error wrappers, no magic strings, lean ViewModels ≤ 400 lines).

### Phase 5: Verification & Quality Gate (Gerbang Kualitas - MANDATORY)
Before declaring the task complete, the AI **MUST** execute the following verification steps:
1. **Unit Test Execution**: Run `./gradlew desktopTest` (or `./gradlew allTests`) to verify that all unit tests pass cleanly. Fix any failures.
2. **Build Verification**: Run `./gradlew :androidApp:assembleDebug` to ensure all multiplatform dependencies compile.
3. **Automated UI / E2E Verification (When applicable)**: Run `./scripts/run_maestro_tests.sh` or `./gradlew maestroTest` for end-to-end flow validation.
4. Report the verification outcomes alongside the final implementation summary.

---

## 4. Diagram & Standards Compliance
Sebelum membuat, mengedit, atau merapikan diagram Mermaid (flowchart & class diagram) pada file dokumentasi teknis, Anda MUST membaca skill **[mermaid-standards](skills/mermaid-standards/SKILL.md)** terlebih dahulu untuk memastikan kepatuhan penuh terhadap standar bentuk geometris dan semantik diagram alur.
