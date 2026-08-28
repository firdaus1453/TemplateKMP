---
name: kmp-presentation
description: >
  Presentation layer, ViewModels, and MVI state machine guidelines for Kotlin Multiplatform & Compose Multiplatform based on Chirp best practices. Trigger when: creating or modifying ViewModels, defining MVI State (data class), Action (sealed interface), or Event (sealed interface) structures, managing coroutine scopes inside ViewModels (viewModelScope), or implementing one-time UI events.
---

# Presentation Layer (Chirp MVI Architecture)

This document defines conventions for the Model-View-Intent (MVI) architecture, Koin ViewModels, Compose Screen setups, UI state design, `UiText` mapping, and one-off UI events.

---

## 🏗️ MVI Architecture Components

Every screen presentation is strictly structured as follows:
1. **State**: A single immutable `data class` representing the UI state.
2. **Action** (Intent): A `sealed interface` representing all user interactions and actions from the UI.
3. **Event**: A `sealed interface` representing one-time UI side-effects (navigation, snackbar, toast).
4. **ViewModel**: Holds `StateFlow<State>`, processes `Action` via `onAction()`, and emits `Event` via a coroutine `Channel`.

### Standard Setup (`feature/{feature}/presentation/{screen}/`)

```kotlin
// 1. State
data class LoginState(
    val username: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val canLogin: Boolean = false,
)

// 2. Action (User Intent)
sealed interface LoginAction {
    data class OnUsernameChanged(val value: String) : LoginAction
    data class OnPasswordChanged(val value: String) : LoginAction
    data object OnTogglePasswordVisibility : LoginAction
    data object OnLoginClick : LoginAction
    data object OnRegisterClick : LoginAction
}

// 3. Event (One-off Side Effects)
sealed interface LoginEvent {
    data object LoginSuccess : LoginEvent
    data class ShowError(val error: UiText) : LoginEvent
}
```

---

## 🧠 Chirp ViewModel Pattern

ViewModels inherit from `androidx.lifecycle.ViewModel` and are injected via Koin (`koinViewModel()` / `viewModelOf`):

```kotlin
class LoginViewModel(
    private val authRepository: AuthRepository,
    private val sessionStorage: SessionStorage,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    private val _events = Channel<LoginEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.OnUsernameChanged -> {
                _state.update {
                    it.copy(
                        username = action.value,
                        canLogin = action.value.isNotBlank() && it.password.isNotBlank()
                    )
                }
            }
            is LoginAction.OnPasswordChanged -> {
                _state.update {
                    it.copy(
                        password = action.value,
                        canLogin = it.username.isNotBlank() && action.value.isNotBlank()
                    )
                }
            }
            LoginAction.OnTogglePasswordVisibility -> {
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
            LoginAction.OnLoginClick -> performLogin()
            LoginAction.OnRegisterClick -> { /* handled at root navigation level */ }
        }
    }

    private fun performLogin() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            authRepository.login(
                username = _state.value.username,
                password = _state.value.password
            )
                .onSuccess { authInfo ->
                    _state.update { it.copy(isLoading = false) }
                    _events.send(LoginEvent.LoginSuccess)
                }
                .onFailure { error ->
                    _state.update { it.copy(isLoading = false) }
                    _events.send(LoginEvent.ShowError(error.asUiText()))
                }
        }
    }
}
```

---

## 🎨 Compose Screen Root vs Dumb Screen

To ensure testability and preview support:
1. **`{Screen}Root` (Smart Composable)**: Injects ViewModel via Koin, collects state with lifecycle, observes one-off events, and routes external navigation.
2. **`{Screen}` (Dumb Composable)**: Purely renders UI from `State` and delegates all interactions to `onAction: (Action) -> Unit`.

```kotlin
@Composable
fun LoginScreenRoot(
    viewModel: LoginViewModel = koinViewModel(),
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            LoginEvent.LoginSuccess -> onLoginSuccess()
            is LoginEvent.ShowError -> {
                // Display error snackbar or toast using event.error.asString()
            }
        }
    }

    LoginScreen(
        state = state,
        onAction = { action ->
            when (action) {
                LoginAction.OnRegisterClick -> onRegisterClick()
                else -> viewModel.onAction(action)
            }
        }
    )
}

@Composable
private fun LoginScreen(
    state: LoginState,
    onAction: (LoginAction) -> Unit,
) {
    // Pure Compose UI layout
}
```

---

## ⚡ UI State Optimization Rules

1. **Flat Map for Dynamic Inputs**: Avoid deeply nested mutable lists in state. For form inputs or list items with dynamic input, store values in flat maps: `val answers: Map<String, String>`.
2. **Lean ViewModels (≤ 400 lines)**: ViewModels orchestrate state and actions. Move complex mapping logic to separate `*Mapper.kt` files and business calculations to domain use cases.
3. **No Exceptions**: Use Chirp `Result<D, E>` extension handlers (`.onSuccess`, `.onFailure`) directly inside coroutines.
