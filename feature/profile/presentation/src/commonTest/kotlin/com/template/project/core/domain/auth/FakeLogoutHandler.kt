package com.template.project.core.domain.auth

class FakeLogoutHandler : LogoutHandler {

    var logoutCalled = false
        private set

    override suspend fun logout() {
        logoutCalled = true
    }
}
