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

@Serializable
data class UserSimpleResponse(
    val userId: Int,
    val username: String,
    val profilePictureUrl: String?
)

@Serializable
data class UpdateProfileRequest(
    val fullName: String?,
    val bio: String?,
    val profilePictureUrl: String?,
    val location: String?,
    val website: String?,
    val gender: String?,
    val phoneNumber: String?,
    val dateOfBirth: String? // ISO 8601 hoặc yyyy-MM-dd
)


@Serializable
data class UserProfileResponse(
    val userId: Int,
    val username: String,
    val email: String,
    val fullName: String,
    val profilePictureUrl: String?,
    val bio: String?,
    val location: String?,
    val isVerified: Boolean,
    val postCount: Int,
    val followerCount: Int,
    val followingCount: Int
)

@Serializable
data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String
)
