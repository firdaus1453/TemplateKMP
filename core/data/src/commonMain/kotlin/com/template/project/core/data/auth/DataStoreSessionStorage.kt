package com.template.project.core.data.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.template.project.core.domain.auth.AuthInfo
import com.template.project.core.domain.auth.SessionStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DataStoreSessionStorage(
    private val dataStore: DataStore<Preferences>,
) : SessionStorage {

    companion object {
        private val AUTH_INFO_KEY = stringPreferencesKey("auth_info")
    }

    override fun observeAuthInfo(): Flow<AuthInfo?> {
        return dataStore.data.map { prefs ->
            prefs[AUTH_INFO_KEY]?.let { json ->
                try {
                    Json.decodeFromString<AuthInfoSerializable>(json).toDomain()
                } catch (e: Exception) {
                    null
                }
            }
        }
    }

    override suspend fun set(authInfo: AuthInfo?) {
        dataStore.edit { prefs ->
            if (authInfo == null) {
                prefs.remove(AUTH_INFO_KEY)
            } else {
                prefs[AUTH_INFO_KEY] = Json.encodeToString(authInfo.toSerializable())
            }
        }
    }
}
