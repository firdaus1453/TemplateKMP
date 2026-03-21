package com.template.project.feature.profile.domain

import com.template.project.core.domain.result.DataError
import com.template.project.core.domain.result.Result
import com.template.project.core.domain.model.User

interface ProfileRepository {
    suspend fun getCurrentUser(): Result<User, DataError.Network>
}
