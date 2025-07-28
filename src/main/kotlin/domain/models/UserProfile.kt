package com.codewithngoc.instagallery.domain.models

import com.codewithngoc.instagallery.db.tables.Role
import io.ktor.server.auth.*
import kotlinx.serialization.Serializable

/** Dùng cho việc đăng ký, đăng nhập */
@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val fullName: String,
    val userType: String,
    val role: String = Role.USER.name
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
data class AuthResponse(val user: User, val token: String)

/** Dùng khi cần hiển thị thông tin user một cách đơn giản, gọn nhẹ (ví dụ: trong comment, like) */
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
    val location: String?
)

/** Dùng để trả về thông tin đầy đủ của một user profile */
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


data class UserIdPrincipalForUser(val userId: Int) : Principal