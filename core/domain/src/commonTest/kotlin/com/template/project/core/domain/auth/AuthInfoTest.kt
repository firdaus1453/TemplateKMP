package com.template.project.core.domain.auth

import com.template.project.core.domain.model.User
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AuthInfoTest {

    @Test
    fun userModelPropertiesAreCorrect() {
        val user = User(
            id = 1,
            username = "emilys",
            email = "emilys@example.com",
            firstName = "Emily",
            lastName = "Smith",
            gender = "female",
            image = "https://example.com/image.png"
        )
        assertEquals(1, user.id)
        assertEquals("emilys", user.username)
        assertEquals("emilys@example.com", user.email)
        assertEquals("Emily", user.firstName)
        assertEquals("Smith", user.lastName)
        assertEquals("female", user.gender)
        assertEquals("https://example.com/image.png", user.image)
    }

    @Test
    fun authInfoPropertiesAreCorrect() {
        val authInfo = AuthInfo(
            accessToken = "access_123",
            refreshToken = "refresh_123",
            userId = 1,
            username = "emilys",
            email = "emilys@example.com",
            firstName = "Emily",
            lastName = "Smith",
            image = "https://example.com/image.png"
        )
        assertEquals("access_123", authInfo.accessToken)
        assertEquals("refresh_123", authInfo.refreshToken)
        assertEquals(1, authInfo.userId)
        assertEquals("emilys", authInfo.username)
        assertEquals("emilys@example.com", authInfo.email)
        assertEquals("Emily", authInfo.firstName)
        assertEquals("Smith", authInfo.lastName)
        assertEquals("https://example.com/image.png", authInfo.image)
    }
}
