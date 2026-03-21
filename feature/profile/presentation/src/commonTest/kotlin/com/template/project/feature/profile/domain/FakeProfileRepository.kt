package com.template.project.feature.profile.domain

import com.template.project.core.domain.model.User
import com.template.project.core.domain.result.DataError
import com.template.project.core.domain.result.Result

class FakeProfileRepository : ProfileRepository {

    var currentUserResult: Result<User, DataError.Network> =
        Result.Error(DataError.Network.UNKNOWN)

    var fetchCallCount = 0
        private set

    override suspend fun getCurrentUser(): Result<User, DataError.Network> {
        fetchCallCount++
        return currentUserResult
    }
}
