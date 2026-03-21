package com.template.project.feature.auth.domain

import com.template.project.core.domain.result.DataError
import com.template.project.core.domain.result.EmptyResult
import com.template.project.core.domain.result.Result

class FakeAuthRepository : AuthRepository {

    var loginResult: EmptyResult<DataError.Network> =
        Result.Error(DataError.Network.UNKNOWN)

    var authenticateResult: EmptyResult<DataError.Network> =
        Result.Error(DataError.Network.UNKNOWN)

    var logoutCalled = false
        private set

    var loginCallCount = 0
        private set

    override suspend fun login(
        username: String,
        password: String,
    ): EmptyResult<DataError.Network> {
        loginCallCount++
        return loginResult
    }

    override suspend fun authenticate(): EmptyResult<DataError.Network> {
        return authenticateResult
    }

    override suspend fun logout() {
        logoutCalled = true
    }
}
