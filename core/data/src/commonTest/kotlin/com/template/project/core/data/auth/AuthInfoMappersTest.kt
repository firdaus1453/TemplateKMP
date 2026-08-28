package com.template.project.core.data.auth

import com.template.project.core.domain.auth.AuthInfo
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthInfoMappersTest {

    @Test
    fun toSerializableMapsAllFieldsCorrectly() {
        val authInfo = AuthInfo(
            accessToken = "access_token_123",
            refreshToken = "refresh_token_123",
            userId = 42,
            username = "johndoe",
            email = "john@example.com",
            firstName = "John",
            lastName = "Doe",
            image = "https://example.com/john.jpg"
        )

        val serializable = authInfo.toSerializable()

        assertEquals("access_token_123", serializable.accessToken)
        assertEquals("refresh_token_123", serializable.refreshToken)
        assertEquals(42, serializable.id)
        assertEquals("johndoe", serializable.username)
        assertEquals("john@example.com", serializable.email)
        assertEquals("John", serializable.firstName)
        assertEquals("Doe", serializable.lastName)
        assertEquals("https://example.com/john.jpg", serializable.image)
    }

    @Test
    fun toDomainMapsAllFieldsCorrectly() {
        val serializable = AuthInfoSerializable(
            accessToken = "access_token_456",
            refreshToken = "refresh_token_456",
            id = 99,
            username = "janedoe",
            email = "jane@example.com",
            firstName = "Jane",
            lastName = "Doe",
            image = "https://example.com/jane.jpg"
        )

        val domain = serializable.toDomain()

        assertEquals("access_token_456", domain.accessToken)
        assertEquals("refresh_token_456", domain.refreshToken)
        assertEquals(99, domain.userId)
        assertEquals("janedoe", domain.username)
        assertEquals("jane@example.com", domain.email)
        assertEquals("Jane", domain.firstName)
        assertEquals("Doe", domain.lastName)
        assertEquals("https://example.com/jane.jpg", domain.image)
    }
}
