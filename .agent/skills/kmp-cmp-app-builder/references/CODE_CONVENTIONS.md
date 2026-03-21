# Code Conventions Reference

Naming rules, file organization, and coding standards for KMP/CMP projects.

## 1. Module Naming

```
core/<layer>/              # e.g. core/data, core/domain, core/presentation, core/designsystem
feature/<feature>/<layer>/ # e.g. feature/chat/data, feature/auth/presentation
```

- Module names are **lowercase**, separated by hyphens if needed
- Feature names are **singular nouns**: `chat`, `auth`, `profile`, `settings`
- Layer names follow Clean Architecture: `domain`, `data`, `presentation`, `database`

---

## 2. Package Naming

```
com.mycompany.myapp.core.data.networking
com.mycompany.myapp.core.domain.auth
com.mycompany.myapp.core.domain.util
com.mycompany.myapp.feature.chat.data.chat
com.mycompany.myapp.feature.chat.domain.models
com.mycompany.myapp.feature.chat.presentation.chatlist
```

- Packages mirror module structure
- Sub-packages group by feature area within a module
- Presentation sub-packages match screen names: `chatlist`, `chatdetail`, `login`

---

## 3. Class / File Naming

### Domain layer

| Type | Convention | Example |
|------|-----------|---------|
| Model | PascalCase noun | `Chat`, `Message`, `User` |
| Repository interface | `*Repository` | `ChatRepository`, `AuthRepository` |
| Service interface | `*Service` | `AuthService`, `NotificationService` |
| Use case | `*UseCase` | `GetChatsUseCase`, `ValidateEmailUseCase` |
| Error types | `DataError.*` | `DataError.Remote`, `DataError.Local` |
| Result type | `Result<D, E>` | `Result<Chat, DataError.Remote>` |

### Data layer

| Type | Convention | Example |
|------|-----------|---------|
| Repository impl | Descriptive prefix + `*Repository` | `OfflineFirstChatRepository` |
| Service impl | `Ktor*Service` | `KtorAuthService`, `KtorChatService` |
| DTO | `*Dto` or `*Serializable` | `ChatDto`, `AuthInfoSerializable` |
| Request body | `*Request` | `LoginRequest`, `RefreshRequest` |
| Response body | `*Response` | `ChatListResponse`, `PaginatedResponse` |
| Mapper file | `*Mappers.kt` | `ChatMappers.kt`, `AuthMappers.kt` |
| Room entity | `*Entity` | `ChatEntity`, `MessageEntity` |
| Room DAO | `*Dao` | `ChatDao`, `MessageDao` |
| Room database | `*Database` | `ChatDatabase` |

### Presentation layer

| Type | Convention | Example |
|------|-----------|---------|
| Screen (stateless) | `*Screen` | `ChatListScreen`, `LoginScreen` |
| Screen root (with VM) | `*ScreenRoot` | `ChatListScreenRoot`, `LoginScreenRoot` |
| ViewModel | `*ViewModel` | `ChatListViewModel`, `LoginViewModel` |
| State | `*State` | `ChatListState`, `LoginState` |
| Action (user input) | `*Action` | `ChatListAction`, `LoginAction` |
| Event (side effect) | `*Event` | `ChatListEvent`, `LoginEvent` |
| Navigation route | `Route.*` | `Route.ChatList`, `Route.Login` |
| Component | Descriptive PascalCase | `ChatBubble`, `UserAvatar`, `AppButton` |

---

## 4. Mapper Conventions

All mapping functions use the `to*()` convention as extension functions:

```kotlin
// ChatMappers.kt
fun ChatDto.toDomain(): Chat { ... }
fun Chat.toEntity(): ChatEntity { ... }
fun ChatEntity.toDomain(): Chat { ... }
fun Chat.toDto(): ChatDto { ... }
fun AuthInfoSerializable.toDomain(): AuthInfo { ... }
fun AuthInfo.toSerializable(): AuthInfoSerializable { ... }
```

Rules:
- **One mapper file per feature** (e.g. `ChatMappers.kt`)
- **Extension functions** — not standalone functions
- **Named by target**: `toDomain()`, `toEntity()`, `toDto()`, `toSerializable()`
- Mapper files live in the `data` module (they bridge between layers)

---

## 5. Koin DI Module Naming

```kotlin
// Pattern: <layer><Feature>Module
val chatDataModule = module {
    singleOf(::OfflineFirstChatRepository).bind<ChatRepository>()
    singleOf(::KtorChatService).bind<ChatService>()
}

val chatPresentationModule = module {
    viewModelOf(::ChatListViewModel)
    viewModelOf(::ChatDetailViewModel)
}

val chatDatabaseModule = module {
    single { getChatDatabase(get()) }
    // Room DAOs are accessed via database instance, no separate DI needed
}

// Core modules
val coreDataModule = module {
    singleOf(::HttpClientFactory)
    singleOf(::DataStoreSessionStorage).bind<SessionStorage>()
    singleOf(::KtorAuthService).bind<AuthService>()
}

val corePresentationModule = module {
    // shared presentation utilities
}
```

### DI Module file location

```
feature/chat/data/src/commonMain/kotlin/.../di/ChatDataModule.kt
feature/chat/presentation/src/commonMain/kotlin/.../di/ChatPresentationModule.kt
core/data/src/commonMain/kotlin/.../di/CoreDataModule.kt
```

---

## 6. Screen Pattern (ScreenRoot vs Screen)

Every screen follows this two-composable pattern:

```kotlin
/**
 * ScreenRoot: Connects ViewModel to Screen.
 * Used in NavHost. Contains side-effect handling.
 */
@Composable
fun ChatListScreenRoot(
    viewModel: ChatListViewModel = koinViewModel(),
    onNavigateToChat: (chatId: String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ChatListEvent.NavigateToChat -> onNavigateToChat(event.chatId)
            is ChatListEvent.ShowError -> { /* show snackbar */ }
        }
    }

    ChatListScreen(
        state = state,
        onAction = viewModel::onAction,
    )
}

/**
 * Screen: Pure, stateless composable.
 * Receives state and action callback. Easy to preview.
 */
@Composable
fun ChatListScreen(
    state: ChatListState,
    onAction: (ChatListAction) -> Unit,
) {
    // UI implementation
}
```

---

## 7. MVI Naming Rules

### State — data class, all UI state in one object

```kotlin
data class ChatListState(
    val chats: List<Chat> = emptyList(),
    val isLoading: Boolean = false,
    val searchQuery: String = "",
)
```

### Action — sealed interface, user-initiated events

```kotlin
sealed interface ChatListAction {
    data object OnRefresh : ChatListAction
    data class OnSearchQueryChange(val query: String) : ChatListAction
    data class OnChatClick(val chatId: String) : ChatListAction
}
```

### Event — sealed interface, one-time side effects

```kotlin
sealed interface ChatListEvent {
    data class NavigateToChat(val chatId: String) : ChatListEvent
    data class ShowError(val message: UiText) : ChatListEvent
}
```

### ViewModel — processes actions, emits state + events

```kotlin
class ChatListViewModel(
    private val chatRepository: ChatRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ChatListState())
    val state = _state
        .onStart { /* initial loading */ }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChatListState())

    private val _events = Channel<ChatListEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: ChatListAction) {
        when (action) {
            ChatListAction.OnRefresh -> { /* handle */ }
            is ChatListAction.OnSearchQueryChange -> {
                _state.update { it.copy(searchQuery = action.query) }
            }
            is ChatListAction.OnChatClick -> {
                viewModelScope.launch {
                    _events.send(ChatListEvent.NavigateToChat(action.chatId))
                }
            }
        }
    }
}
```

---

## 8. Navigation Route Convention

```kotlin
// Sealed class with @Serializable routes
sealed interface Route {

    // Screens (no args)
    @Serializable data object Login : Route
    @Serializable data object Register : Route
    @Serializable data object ChatList : Route

    // Screens (with args)
    @Serializable data class ChatDetail(val chatId: String) : Route
    @Serializable data class UserProfile(val userId: String) : Route

    // Nested navigation graphs
    @Serializable data object AuthGraph : Route
    @Serializable data object MainGraph : Route
}
```

---

## 9. General Coding Rules

1. **Never use `println()`** — Use multiplatform logger (Kermit) or Timber on Android
2. **Never use `Log.d()`** — Use Timber instead: `Timber.d("message")`
3. **Never hardcode strings** — Use `UiText` with string resources
4. **Never use `GlobalScope`** — Always use `viewModelScope` or structured concurrency
5. **Never catch `CancellationException`** — Always re-throw it:
   ```kotlin
   try {
       // work
   } catch (e: Exception) {
       if (e is CancellationException) throw e
       // handle error
   }
   ```
6. **Result type over try-catch** — Functions return `Result<D, E>`, not throw exceptions
7. **`suspend` functions return `Result`** — Network / DB calls wrap errors in `Result.Error`
8. **`Flow` for observation** — Database and real-time data use `Flow`, not callbacks
9. **`expect`/`actual` for platform code** — Never use `if (Platform.isAndroid)` checks
10. **`internal` visibility** — Data layer classes should be `internal`, only interfaces are public
11. **One class per file** — Exception: small sealed interface hierarchies
12. **No wildcard imports** — Use explicit imports only
13. **Trailing commas** — Always use trailing commas in parameter lists and collections

---

## 10. Resource Prefix Convention

Convention plugin auto-generates `resourcePrefix` from module path to avoid resource name collisions:

```kotlin
// In KmpLibraryConventionPlugin
android {
    resourcePrefix = project.path
        .removePrefix(":")
        .replace(":", "_")
        .replace("-", "_") + "_"
}
// Result for :feature:chat:presentation → "feature_chat_presentation_"
```

This means all Android resources in that module must be prefixed:
- `feature_chat_presentation_ic_send.xml`
- `feature_chat_presentation_chat_list_title` (string resource)
