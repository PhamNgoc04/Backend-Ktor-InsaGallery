package com.codewithngoc.instagallery.data.services

import com.codewithngoc.instagallery.domain.models.*
import com.codewithngoc.instagallery.domain.repos.AuthRepository
import com.codewithngoc.instagallery.domain.services.AuthService

class AuthServiceImpl(
    private val authRepository: AuthRepository
) : AuthService {

    override suspend fun registerUser(registerRequest: RegisterRequest): User? {
        return authRepository.registerUser(registerRequest)
    }

    override suspend fun loginUser(request: LoginRequest): User? {
        return authRepository.loginUser(request)
    }

    override suspend fun getUserById(id: Int): UserProfileResponse? {
        // Lấy user domain từ repository
        val user = authRepository.getUserById(id) ?: return null

        // Chuyển domain User -> UserProfileResponse
        return UserProfileResponse(
            userId = user.userId,
            username = user.username,
            email = user.email,
            fullName = user.fullName,
            profilePictureUrl = null, // chưa hỗ trợ avatar
            bio = null,               // chưa hỗ trợ bio
            location = null,          // chưa hỗ trợ location
            isVerified = false,
            postCount = 0,
            followerCount = 0,
            followingCount = 0
        )
    }

    override suspend fun deleteUserById(id: Int): Boolean {
        return authRepository.deleteUserById(id)
    }

    override suspend fun logout(refreshToken: String): Boolean {
        return authRepository.logout(refreshToken)
    }
}
