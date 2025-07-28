package com.codewithngoc.instagallery.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val userId: Int,
    val username: String,
    val email: String,
    val fullName: String,
    val userType: String,
    val role: String
)