package com.codewithngoc.instagallery.data.services

import com.codewithngoc.instagallery.config.generateToken
import com.codewithngoc.instagallery.db.entities.UserEntity
import com.codewithngoc.instagallery.db.utils.dbQuery
import com.codewithngoc.instagallery.domain.models.AuthResponse
import com.codewithngoc.instagallery.domain.models.ChangePasswordRequest
import com.codewithngoc.instagallery.domain.models.LoginRequest
import com.codewithngoc.instagallery.domain.models.LoginResponse
import com.codewithngoc.instagallery.domain.models.RegisterRequest
import com.codewithngoc.instagallery.domain.models.UpdateProfileRequest
import com.codewithngoc.instagallery.domain.models.UserProfileResponse
import com.codewithngoc.instagallery.domain.repos.AuthRepository
import com.codewithngoc.instagallery.domain.services.AuthService
import io.ktor.server.application.Application

class AuthServiceImpl(
    private val authRepository: AuthRepository,
    private val app: Application // Giả sử bạn cần truy cập vào ứng dụng để lấy jwtService
): AuthService {

    override suspend fun registerUser(registerRequest: RegisterRequest): Result<AuthResponse> {
        return try {
            val user = authRepository.registerUser(registerRequest)
            if (user != null) {
                // 🔑 Sinh JWT token
                val token = app.generateToken(user) // giả sử bạn có jwtService

                val response = AuthResponse(
                    user = user,
                    token = token
                )
                Result.success(response)
            } else {
                Result.failure(Exception("Username or email already exists"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun loginUser(request: LoginRequest): Result<LoginResponse> {
        return try {
            val user = authRepository.loginUser(request)
            if (user != null) {
                // 🔑 Sinh JWT token
                val token = app.generateToken(user) // giả sử bạn có jwtService

                val response = LoginResponse(
                    userId = user.userId,
                    username = user.username,
                    token = token
                )
                Result.success(response)
            } else {
                Result.failure(Exception("Invalid email or password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(refreshToken: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun getUserById(userId: Int): Result<UserProfileResponse> {
        return try {
            val userEntity = authRepository.getUserById(userId)
            if (userEntity != null) {
                Result.success(userEntity.toUserProfileResponse())
            } else {
                Result.failure(Exception("❌ User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(
        userId: Int,
        updateRequest: UpdateProfileRequest
    ): Result<UserProfileResponse> {
        return runCatching {
            // Gọi Repository để cập nhật thông tin người dùng trong transaction
            val updatedUser = authRepository.updateUserProfile(userId, updateRequest)
                ?: throw IllegalStateException("❌ Failed to update user: user not found")

            // Reload lại user bằng Exposed DAO, BỌC TRONG dbQuery để tránh lỗi transaction
            val reloaded: UserEntity = dbQuery {
                UserEntity.findById(userId)
                    ?: throw IllegalStateException("❌ User not found after update")
            }

            // Chuyển sang response trả về client
            reloaded.toUserProfileResponse()
        }
    }

    override suspend fun deleteUserById(userId: Int): Result<Unit> {
        return try {
            val deleted = authRepository.deleteUserById(userId)
            if (deleted) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("❌ User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}