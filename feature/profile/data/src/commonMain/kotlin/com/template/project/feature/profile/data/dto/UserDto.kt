package com.template.project.feature.profile.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: Int,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val image: String,
    val phone: String? = null,
    val gender: String? = null,
    val age: Int? = null,
)
