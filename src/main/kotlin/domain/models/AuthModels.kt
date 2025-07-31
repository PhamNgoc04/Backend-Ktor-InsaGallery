package com.codewithngoc.instagallery.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val fullName: String,
)
@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class LoginResponse(
    val userId: Int,
    val username: String,
    val token: String
)

@Serializable
data class AuthResponse(
    val user: User,
    val token: String
)
