# Offline-First Reference

Complete patterns for building offline-first KMP/CMP applications using Room as the single source of truth with network synchronization.

## Core Principle

> **Room database is the single source of truth.** UI always observes database Flows. Network calls fetch fresh data and upsert into the database. The UI reactively updates from the database.

```
┌──────────┐     observe (Flow)     ┌──────────┐     fetch + upsert     ┌──────────┐
│   UI /   │ ◄──────────────────── │   Room   │ ◄───────────────────── │  Network │
│ ViewModel│                        │ Database │                        │   (Ktor) │
└──────────┘                        └──────────┘                        └──────────┘
```

## 1. Offline-First Repository Pattern

### Domain interface (feature/chat/domain)

```kotlin
package com.mycompany.myapp.feature.chat.domain

import com.mycompany.myapp.core.domain.util.DataError
import com.mycompany.myapp.core.domain.util.EmptyResult
import com.mycompany.myapp.core.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    /** Observe chats from local database — always returns cached data */
    fun getChats(): Flow<List<Chat>>

    /** Observe single chat info from local database */
    fun getChatInfoById(chatId: String): Flow<ChatInfo>

    /** Fetch chats from network and sync to local database */
    suspend fun fetchChats(): Result<List<Chat>, DataError.Remote>

    /** Fetch single chat from network and sync to local database */
    suspend fun fetchChatById(chatId: String): EmptyResult<DataError.Remote>

    /** Create a new chat via network, then sync to local */
    suspend fun createChat(participantIds: List<String>): Result<Chat, DataError.Remote>
}
```

### Implementation (feature/chat/data)

```kotlin
package com.mycompany.myapp.feature.chat.data

import com.mycompany.myapp.core.domain.util.DataError
import com.mycompany.myapp.core.domain.util.EmptyResult
import com.mycompany.myapp.core.domain.util.Result
import com.mycompany.myapp.core.domain.util.asEmptyResult
import com.mycompany.myapp.core.domain.util.onSuccess
import com.mycompany.myapp.feature.chat.database.ChatDatabase
import com.mycompany.myapp.feature.chat.domain.Chat
import com.mycompany.myapp.feature.chat.domain.ChatInfo
import com.mycompany.myapp.feature.chat.domain.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineFirstChatRepository(
    private val chatService: ChatService,
    private val db: ChatDatabase,
) : ChatRepository {

    /**
     * OBSERVE: Always read from Room database.
     * UI subscribes to this Flow and gets automatic updates.
     */
    override fun getChats(): Flow<List<Chat>> {
        return db.chatDao.getAllChats()
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }

    override fun getChatInfoById(chatId: String): Flow<ChatInfo> {
        return db.chatDao.getChatInfoById(chatId)
            .map { it.toDomain() }
    }

    /**
     * SYNC: Fetch from network → upsert into Room.
     * The Flow in getChats() will automatically emit the updated data.
     */
    override suspend fun fetchChats(): Result<List<Chat>, DataError.Remote> {
        return chatService
            .getChats()
            .onSuccess { chats ->
                // Upsert fetched data into local database
                db.chatDao.upsertChats(
                    chats.map { it.toEntity() }
                )
            }
    }

    override suspend fun fetchChatById(chatId: String): EmptyResult<DataError.Remote> {
        return chatService
            .getChatById(chatId)
            .onSuccess { chat ->
                db.chatDao.upsertChat(chat.toEntity())
            }
            .asEmptyResult()
    }

    override suspend fun createChat(participantIds: List<String>): Result<Chat, DataError.Remote> {
        return chatService
            .createChat(participantIds)
            .onSuccess { chat ->
                db.chatDao.upsertChat(chat.toEntity())
            }
    }
}
```

---

## 2. ViewModel with Offline-First

```kotlin
class ChatListViewModel(
    private val chatRepository: ChatRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ChatListState())
    val state = _state
        .onStart {
            // 1. Observe database (offline data appears instantly)
            observeChats()
            // 2. Fetch fresh data from network (syncs to DB automatically)
            fetchChats()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ChatListState())

    private fun observeChats() {
        chatRepository.getChats()
            .onEach { chats ->
                _state.update {
                    it.copy(chats = chats, isLoading = false)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun fetchChats() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            chatRepository.fetchChats()
                .onError { error ->
                    _state.update { it.copy(isLoading = false) }
                    _events.send(ChatListEvent.ShowError(error.toUiText()))
                }
            // Note: onSuccess is handled in repository — DB Flow will update automatically
        }
    }

    fun onAction(action: ChatListAction) {
        when (action) {
            ChatListAction.OnRefresh -> fetchChats()
            // ...
        }
    }
}
```

---

## 3. Safe Database Update Utility

```kotlin
package com.mycompany.myapp.core.data.database

import androidx.room.RoomDatabase
import kotlinx.coroutines.CancellationException

/**
 * Wraps database operations that may fail due to threading/lifecycle.
 * Catches non-cancellation exceptions and logs them.
 */
suspend fun <T> safeDatabaseUpdate(block: suspend () -> T): T? {
    return try {
        block()
    } catch (e: Exception) {
        if (e is CancellationException) throw e
        e.printStackTrace()
        null
    }
}
```

---

## 4. Connectivity Observer (expect/actual)

### Interface (commonMain)

```kotlin
package com.mycompany.myapp.feature.chat.data.network

import kotlinx.coroutines.flow.Flow

interface ConnectivityObserver {
    fun observe(): Flow<Boolean>
}
```

### Android implementation

```kotlin
package com.mycompany.myapp.feature.chat.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class AndroidConnectivityObserver(
    private val context: Context,
) : ConnectivityObserver {

    override fun observe(): Flow<Boolean> = callbackFlow {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                trySend(false)
            }

            override fun onCapabilitiesChanged(
                network: Network,
                capabilities: NetworkCapabilities
            ) {
                val hasInternet = capabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_INTERNET
                )
                trySend(hasInternet)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)

        // Check initial state
        val activeNetwork = connectivityManager.activeNetwork
        val isConnected = connectivityManager.getNetworkCapabilities(activeNetwork)
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        trySend(isConnected)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
}
```

### iOS implementation

```kotlin
package com.mycompany.myapp.feature.chat.data.network

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_get_status
import platform.Network.nw_path_status_satisfied
import platform.darwin.dispatch_get_main_queue

class IosConnectivityObserver : ConnectivityObserver {

    override fun observe(): Flow<Boolean> = callbackFlow {
        val monitor = nw_path_monitor_create()
        nw_path_monitor_set_queue(monitor, dispatch_get_main_queue())

        nw_path_monitor_set_update_handler(monitor) { path ->
            val isConnected = nw_path_get_status(path) == nw_path_status_satisfied
            trySend(isConnected)
        }

        nw_path_monitor_start(monitor)

        awaitClose {
            nw_path_monitor_cancel(monitor)
        }
    }
}
```

### Desktop implementation

```kotlin
package com.mycompany.myapp.feature.chat.data.network

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.net.InetSocketAddress
import java.net.Socket

class DesktopConnectivityObserver : ConnectivityObserver {

    override fun observe(): Flow<Boolean> = flow {
        while (true) {
            val isConnected = try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress("8.8.8.8", 53), 1500)
                    true
                }
            } catch (e: Exception) {
                false
            }
            emit(isConnected)
            delay(5_000) // Poll every 5 seconds
        }
    }
}
```

---

## 5. Connection Retry Handler

```kotlin
package com.mycompany.myapp.feature.chat.data.network

import kotlinx.coroutines.delay
import kotlin.math.min
import kotlin.math.pow

class ConnectionRetryHandler {

    private var retryCount = 0
    private val maxRetryDelay = 30_000L // 30 seconds max

    suspend fun retryWithBackoff(block: suspend () -> Unit) {
        val delay = min(
            (2.0.pow(retryCount.toDouble()) * 1000).toLong(),
            maxRetryDelay
        )
        delay(delay)
        retryCount++
        block()
    }

    fun reset() {
        retryCount = 0
    }
}
```

---

## 6. WebSocket Connection Management

```kotlin
package com.mycompany.myapp.feature.chat.data.network

import com.mycompany.myapp.core.domain.auth.SessionStorage
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.Json

class WebSocketConnectionClient(
    private val httpClient: HttpClient,
    private val sessionStorage: SessionStorage,
    private val connectivityObserver: ConnectivityObserver,
    private val retryHandler: ConnectionRetryHandler,
) {
    private var session: WebSocketSession? = null

    fun connect(chatId: String): Flow<IncomingWebSocketMessage> = flow {
        // Auto-reconnect on network changes
        connectivityObserver.observe().collect { isConnected ->
            if (isConnected && session?.isActive != true) {
                retryHandler.retryWithBackoff {
                    try {
                        session = httpClient.webSocketSession(
                            urlString = "wss://api.yourapp.com/ws/chat/$chatId"
                        )
                        retryHandler.reset()

                        session?.incoming?.receiveAsFlow()
                            ?.map { frame ->
                                when (frame) {
                                    is Frame.Text -> {
                                        Json.decodeFromString<IncomingWebSocketMessage>(
                                            frame.readText()
                                        )
                                    }
                                    else -> null
                                }
                            }
                            ?.collect { message ->
                                message?.let { emit(it) }
                            }
                    } catch (e: Exception) {
                        // Will retry with backoff
                    }
                }
            }
        }
    }

    suspend fun sendMessage(message: String) {
        session?.send(Frame.Text(message))
    }

    suspend fun disconnect() {
        session?.close()
        session = null
    }
}
```

---

## Summary: Offline-First Data Flow

```
1. App starts
   ├── ViewModel observes Room Flow → UI shows cached data instantly
   └── ViewModel calls fetchXxx() → Network request
       ├── onSuccess → upsert into Room → Flow emits → UI updates
       └── onError → show error toast, cached data remains visible

2. User creates/updates data
   ├── Network call first (optimistic or pessimistic)
   └── onSuccess → upsert into Room → Flow emits → UI updates

3. Network becomes available (ConnectivityObserver)
   └── Trigger sync: re-fetch latest data from server → upsert into Room
```
