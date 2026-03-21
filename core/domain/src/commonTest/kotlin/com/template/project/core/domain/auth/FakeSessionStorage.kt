package com.template.project.core.domain.auth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSessionStorage : SessionStorage {

    private val _authInfo = MutableStateFlow<AuthInfo?>(null)

    override fun observeAuthInfo(): Flow<AuthInfo?> = _authInfo

    override suspend fun set(authInfo: AuthInfo?) {
        _authInfo.value = authInfo
    }
}
