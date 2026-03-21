# Feature Modules Reference

Complete patterns for creating feature modules following Clean Architecture.

## Feature module structure

Each feature has up to 4 submodules:

```
feature/<feature-name>/
├── domain/          # Required — models, repository interfaces, use cases
│   └── src/commonMain/kotlin/com/mycompany/myapp/feature/<feature>/domain/
│       ├── model/       # Feature-specific data classes
│       ├── repository/  # Repository interfaces
│       └── usecase/     # Use case classes (optional, for complex business logic)
│
├── data/            # Optional — repository implementations, API DTOs, mappers
│   └── src/
│       ├── commonMain/kotlin/com/mycompany/myapp/feature/<feature>/data/
│       │   ├── dto/         # API request/response DTOs
│       │   ├── mapper/      # DTO-to-domain mappers
│       │   ├── repository/  # Repository implementations
│       │   └── di/          # Koin DI module
│       ├── androidMain/     # Platform-specific data implementations
│       └── iosMain/
│
├── database/        # Optional — Room entities, DAOs, database class
│   └── src/commonMain/kotlin/com/mycompany/myapp/feature/<feature>/database/
│       ├── entity/      # Room @Entity classes
│       ├── dao/         # Room @Dao interfaces
│       ├── converter/   # Room TypeConverters
│       └── AppDatabase.kt
│
└── presentation/    # Required — ViewModel, UI state, screens, DI
    └── src/commonMain/kotlin/com/mycompany/myapp/feature/<feature>/presentation/
        ├── <screen_name>/
        │   ├── <ScreenName>Screen.kt     # @Composable screen
        │   ├── <ScreenName>ViewModel.kt  # ViewModel
        │   ├── <ScreenName>State.kt      # UI state data class
        │   ├── <ScreenName>Action.kt     # User actions sealed interface
        │   └── <ScreenName>Event.kt      # One-time events sealed interface
        ├── components/                    # Feature-specific composables
        └── di/
            └── <Feature>PresentationModule.kt
```

## Example: Auth feature

### feature/auth/domain

#### AuthRepository.kt

```kotlin
package com.mycompany.myapp.feature.auth.domain

import com.mycompany.myapp.core.domain.result.DataError
import com.mycompany.myapp.core.domain.result.EmptyResult

interface AuthRepository {
    suspend fun login(email: String, password: String): EmptyResult<DataError.Network>
    suspend fun register(email: String, password: String): EmptyResult<DataError.Network>
    suspend fun authenticate(): EmptyResult<DataError.Network>
    suspend fun logout()
}
```

#### AuthValidation.kt

```kotlin
package com.mycompany.myapp.feature.auth.domain

data class PasswordValidationState(
    val hasMinLength: Boolean = false,
    val hasUpperCase: Boolean = false,
    val hasLowerCase: Boolean = false,
    val hasDigit: Boolean = false,
) {
    val isValid: Boolean
        get() = hasMinLength && hasUpperCase && hasLowerCase && hasDigit
}

fun validatePassword(password: String): PasswordValidationState {
    return PasswordValidationState(
        hasMinLength = password.length >= 8,
        hasUpperCase = password.any { it.isUpperCase() },
        hasLowerCase = password.any { it.isLowerCase() },
        hasDigit = password.any { it.isDigit() },
    )
}
```

### feature/auth/presentation

#### LoginState.kt

```kotlin
package com.mycompany.myapp.feature.auth.presentation.login

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val canLogin: Boolean = false,
)
```

#### LoginAction.kt

```kotlin
package com.mycompany.myapp.feature.auth.presentation.login

sealed interface LoginAction {
    data class OnEmailChanged(val email: String) : LoginAction
    data class OnPasswordChanged(val password: String) : LoginAction
    data object OnTogglePasswordVisibility : LoginAction
    data object OnLoginClick : LoginAction
    data object OnRegisterClick : LoginAction
}
```

#### LoginEvent.kt

```kotlin
package com.mycompany.myapp.feature.auth.presentation.login

import com.mycompany.myapp.core.presentation.UiText

sealed interface LoginEvent {
    data class ShowError(val error: UiText) : LoginEvent
    data object LoginSuccess : LoginEvent
}
```

#### LoginViewModel.kt

```kotlin
package com.mycompany.myapp.feature.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mycompany.myapp.core.domain.result.Result
import com.mycompany.myapp.core.presentation.toUiText
import com.mycompany.myapp.feature.auth.domain.AuthRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state
        .onStart {
            // Optional: initialization logic
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            LoginState()
        )

    private val _events = Channel<LoginEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.OnEmailChanged -> {
                _state.update {
                    it.copy(
                        email = action.email,
                        canLogin = action.email.isNotBlank() && it.password.isNotBlank()
                    )
                }
            }
            is LoginAction.OnPasswordChanged -> {
                _state.update {
                    it.copy(
                        password = action.password,
                        canLogin = it.email.isNotBlank() && action.password.isNotBlank()
                    )
                }
            }
            LoginAction.OnTogglePasswordVisibility -> {
                _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
            }
            LoginAction.OnLoginClick -> login()
            LoginAction.OnRegisterClick -> {
                // Navigate handled by parent
            }
        }
    }

    private fun login() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            when (val result = authRepository.login(state.value.email, state.value.password)) {
                is Result.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _events.send(LoginEvent.ShowError(result.error.toUiText()))
                }
                is Result.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    _events.send(LoginEvent.LoginSuccess)
                }
            }
        }
    }
}
```

#### LoginScreen.kt

```kotlin
package com.mycompany.myapp.feature.auth.presentation.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mycompany.myapp.core.presentation.ObserveAsEvents
import org.koin.compose.viewmodel.koinViewModel

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
                // Show snackbar or toast
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Welcome Back",
            style = MaterialTheme.typography.headlineMedium,
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = state.email,
            onValueChange = { onAction(LoginAction.OnEmailChanged(it)) },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = { onAction(LoginAction.OnPasswordChanged(it)) },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (state.isPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onAction(LoginAction.OnLoginClick) },
            enabled = state.canLogin && !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                )
            } else {
                Text("Sign In")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { onAction(LoginAction.OnRegisterClick) }) {
            Text("Don't have an account? Register")
        }
    }
}
```

### Koin DI module for feature

#### AuthPresentationModule.kt

```kotlin
package com.mycompany.myapp.feature.auth.presentation.di

import com.mycompany.myapp.feature.auth.presentation.login.LoginViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authPresentationModule = module {
    viewModelOf(::LoginViewModel)
}
```

---

## Example: Feature with Room database (chat)

### feature/chat/database

#### MessageEntity.kt

```kotlin
package com.mycompany.myapp.feature.chat.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val senderId: String,
    val receiverId: String,
    val content: String,
    val timestamp: Long,
    val isRead: Boolean = false,
)
```

#### MessageDao.kt

```kotlin
package com.mycompany.myapp.feature.chat.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mycompany.myapp.feature.chat.database.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Upsert
    suspend fun upsertMessages(messages: List<MessageEntity>)

    @Query("SELECT * FROM messages WHERE (senderId = :userId OR receiverId = :userId) ORDER BY timestamp DESC")
    fun getMessagesForUser(userId: String): Flow<List<MessageEntity>>

    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)

    @Query("DELETE FROM messages")
    suspend fun clearAll()
}
```

#### ChatDatabase.kt

```kotlin
package com.mycompany.myapp.feature.chat.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mycompany.myapp.feature.chat.database.dao.MessageDao
import com.mycompany.myapp.feature.chat.database.entity.MessageEntity

@Database(
    entities = [MessageEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class ChatDatabase : RoomDatabase() {
    abstract val messageDao: MessageDao
}
```

#### DatabaseFactory.kt (expect/actual pattern)

```kotlin
// commonMain
package com.mycompany.myapp.feature.chat.database

import androidx.room.RoomDatabase

expect class DatabaseFactory {
    fun create(): RoomDatabase.Builder<ChatDatabase>
}
```

```kotlin
// androidMain
package com.mycompany.myapp.feature.chat.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

actual class DatabaseFactory(
    private val context: Context
) {
    actual fun create(): RoomDatabase.Builder<ChatDatabase> {
        val dbFile = context.getDatabasePath("chat.db")
        return Room.databaseBuilder<ChatDatabase>(
            context = context,
            name = dbFile.absolutePath
        )
    }
}
```

```kotlin
// iosMain
package com.mycompany.myapp.feature.chat.database

import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.NSHomeDirectory

actual class DatabaseFactory {
    actual fun create(): RoomDatabase.Builder<ChatDatabase> {
        val dbFile = NSHomeDirectory() + "/chat.db"
        return Room.databaseBuilder<ChatDatabase>(
            name = dbFile,
        )
    }
}
```

```kotlin
// desktopMain
package com.mycompany.myapp.feature.chat.database

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

actual class DatabaseFactory {
    actual fun create(): RoomDatabase.Builder<ChatDatabase> {
        val dbDir = File(System.getProperty("user.home"), ".myapp")
        if (!dbDir.exists()) dbDir.mkdirs()
        val dbFile = File(dbDir, "chat.db")
        return Room.databaseBuilder<ChatDatabase>(
            name = dbFile.absolutePath,
        )
    }
}
```

### feature/chat/data — WebSocket example

#### ChatDataSource.kt

```kotlin
package com.mycompany.myapp.feature.chat.data

import com.mycompany.myapp.feature.chat.domain.model.Message
import kotlinx.coroutines.flow.Flow

interface ChatDataSource {
    fun observeMessages(chatId: String): Flow<Message>
    suspend fun sendMessage(chatId: String, content: String)
    suspend fun disconnect()
}
```

### Navigation routes — Type-safe

```kotlin
package com.mycompany.myapp.feature.auth.presentation

import kotlinx.serialization.Serializable

// Define routes as @Serializable objects/classes
@Serializable
data object LoginRoute

@Serializable
data object RegisterRoute
```

```kotlin
package com.mycompany.myapp.feature.chat.presentation

import kotlinx.serialization.Serializable

@Serializable
data object ChatListRoute

@Serializable
data class ChatDetailRoute(val chatId: String)
```
