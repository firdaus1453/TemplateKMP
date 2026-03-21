package com.template.project.feature.auth.domain

import com.template.project.core.domain.result.DataError
import com.template.project.core.domain.result.EmptyResult

interface AuthRepository {
    suspend fun login(username: String, password: String): EmptyResult<DataError.Network>
    suspend fun authenticate(): EmptyResult<DataError.Network>
    suspend fun logout()
}
