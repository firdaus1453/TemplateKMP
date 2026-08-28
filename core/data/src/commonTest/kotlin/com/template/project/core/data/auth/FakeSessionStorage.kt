package com.template.project.core.data.auth

import com.template.project.core.domain.auth.AuthInfo
import com.template.project.core.domain.auth.SessionStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeSessionStorage : SessionStorage {
    private val authInfoFlow = MutableStateFlow<AuthInfo?>(null)

    override fun observeAuthInfo(): Flow<AuthInfo?> = authInfoFlow.asStateFlow()

    override suspend fun set(info: AuthInfo?) {
        authInfoFlow.value = info
    }
}
