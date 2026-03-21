package com.template.project.core.domain.auth

data class AuthInfo(
    val accessToken: String,
    val refreshToken: String,
    val userId: Int,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val image: String,
)
