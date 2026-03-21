package com.template.project.core.data.auth

import com.template.project.core.domain.auth.AuthInfo

fun AuthInfo.toSerializable(): AuthInfoSerializable {
    return AuthInfoSerializable(
        accessToken = accessToken,
        refreshToken = refreshToken,
        id = userId,
        username = username,
        email = email,
        firstName = firstName,
        lastName = lastName,
        image = image,
    )
}

fun AuthInfoSerializable.toDomain(): AuthInfo {
    return AuthInfo(
        accessToken = accessToken,
        refreshToken = refreshToken,
        userId = id,
        username = username,
        email = email,
        firstName = firstName,
        lastName = lastName,
        image = image,
    )
}
