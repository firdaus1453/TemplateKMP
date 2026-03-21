package com.template.project.core.data.auth

import com.template.project.core.data.networking.safePost
import com.template.project.core.data.networking.safeGet
import com.template.project.core.domain.auth.LogoutHandler
import com.template.project.core.domain.auth.SessionStorage
import com.template.project.core.domain.result.DataError
import com.template.project.core.domain.result.EmptyResult
import com.template.project.core.domain.result.Result
import com.template.project.core.domain.result.asEmptyResult
import com.template.project.core.domain.result.map
import com.template.project.core.domain.result.onSuccess
import com.template.project.feature.auth.domain.AuthRepository
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.Serializable

@Serializable
internal data class LoginRequest(
    val username: String,
    val password: String,
)

class DefaultAuthRepository(
    private val httpClient: HttpClient,
    private val sessionStorage: SessionStorage,
) : AuthRepository, LogoutHandler {

    override suspend fun login(
        username: String,
        password: String,
    ): EmptyResult<DataError.Network> {
        return httpClient.safePost<LoginRequest, AuthInfoSerializable>(
            route = "/auth/login",
            body = LoginRequest(
                username = username,
                password = password,
            ),
        ).onSuccess { authInfoDto ->
            sessionStorage.set(authInfoDto.toDomain())
        }.asEmptyResult()
    }

    override suspend fun authenticate(): EmptyResult<DataError.Network> {
        val authInfo = sessionStorage.observeAuthInfo().firstOrNull()
            ?: return Result.Error(DataError.Network.UNAUTHORIZED)

        return httpClient.safeGet<AuthInfoSerializable>(
            route = "/auth/me",
        ).asEmptyResult()
    }

    override suspend fun logout() {
        sessionStorage.set(null)
    }
}
