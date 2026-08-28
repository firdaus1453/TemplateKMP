package com.template.project.feature.profile.data

import com.template.project.feature.profile.data.dto.UserDto
import com.template.project.feature.profile.data.mapper.toDomain
import kotlin.test.Test
import kotlin.test.assertEquals

class ProfileMappersTest {

    @Test
    fun toDomainMapsAllUserDtoFields() {
        val dto = UserDto(
            id = 1,
            username = "emilys",
            email = "emilys@example.com",
            firstName = "Emily",
            lastName = "Smith",
            gender = "female",
            image = "https://example.com/emily.png",
            phone = "+1234567890",
            age = 28
        )

        val user = dto.toDomain()

        assertEquals(1, user.id)
        assertEquals("emilys", user.username)
        assertEquals("emilys@example.com", user.email)
        assertEquals("Emily", user.firstName)
        assertEquals("Smith", user.lastName)
        assertEquals("female", user.gender)
        assertEquals("https://example.com/emily.png", user.image)
        assertEquals("+1234567890", user.phone)
        assertEquals(28, user.age)
    }

    @Test
    fun toDomainHandlesNullOptionalFields() {
        val dto = UserDto(
            id = 2,
            username = "alex",
            email = "alex@example.com",
            firstName = "Alex",
            lastName = "Jones",
            gender = null,
            image = "https://example.com/alex.png",
            phone = null,
            age = null
        )

        val user = dto.toDomain()

        assertEquals("", user.gender)
        assertEquals("", user.phone)
        assertEquals(0, user.age)
    }
}
