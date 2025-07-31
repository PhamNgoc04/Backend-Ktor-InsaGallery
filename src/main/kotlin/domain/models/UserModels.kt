package com.codewithngoc.instagallery.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val userId: Int,
    val username: String,
    val email: String,
    val fullName: String,
    val userType: String,
    val role: String,
    val profilePictureUrl: String? = null,
    val bio: String? = null,
    val website: String? = null,
    val gender: String? = null,
    val dateOfBirth: String? = null,
    val location: String? = null,
    val isVerified: Boolean = false
) {
    // Hàm chuyển đổi từ User sang UserProfileResponse
    fun toUserProfileResponse(): UserProfileResponse = UserProfileResponse(
        userId = this.userId,
        username = this.username,
        fullName = this.fullName,
        profilePictureUrl = this.profilePictureUrl,
        bio = this.bio,
        website = this.website,
        gender = this.gender,
        dateOfBirth = this.dateOfBirth,
        location = this.location,
        isVerified = this.isVerified,
        postCount = 0,
        followerCount = 0,
        followingCount = 0
    )
}

@Serializable
data class UserSimpleResponse(
    val userId: Int,
    val username: String,
    val profilePictureUrl: String?
)

@Serializable
data class UpdateProfileRequest(
    val username: String?,
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
    val fullName: String,
    val profilePictureUrl: String?,
    val bio: String?,
    val website: String? = null,
    val gender: String? = null,
    val dateOfBirth: String? = null,
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
