package com.template.project.feature.profile.data.repository

import com.template.project.core.data.networking.safeGet
import com.template.project.core.domain.model.User
import com.template.project.core.domain.result.DataError
import com.template.project.core.domain.result.Result
import com.template.project.core.domain.result.map
import com.template.project.feature.profile.data.dto.UserDto
import com.template.project.feature.profile.data.mapper.toDomain
import com.template.project.feature.profile.domain.ProfileRepository
import io.ktor.client.HttpClient

class DefaultProfileRepository(
    private val httpClient: HttpClient,
) : ProfileRepository {

    override suspend fun getCurrentUser(): Result<User, DataError.Network> {
        return httpClient.safeGet<UserDto>(
            route = "/auth/me",
        ).map { it.toDomain() }
    }
}
