package com.template.project.feature.profile.data.mapper

import com.template.project.core.domain.model.User
import com.template.project.feature.profile.data.dto.UserDto

fun UserDto.toDomain(): User {
    return User(
        id = id,
        username = username,
        email = email,
        firstName = firstName,
        lastName = lastName,
        image = image,
        phone = phone ?: "",
        gender = gender ?: "",
        age = age ?: 0,
    )
}
