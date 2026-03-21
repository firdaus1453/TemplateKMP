package com.template.project.core.domain.model

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val image: String,
    val phone: String = "",
    val gender: String = "",
    val age: Int = 0,
)
