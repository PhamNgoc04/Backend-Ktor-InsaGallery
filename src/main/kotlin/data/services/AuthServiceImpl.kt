package com.codewithngoc.instagallery.data.services

import com.codewithngoc.instagallery.config.generateToken
import com.codewithngoc.instagallery.db.entities.UserEntity
import com.codewithngoc.instagallery.db.tables.UserSessionsTable
import com.codewithngoc.instagallery.db.utils.dbQuery
import com.codewithngoc.instagallery.domain.models.AuthResponse
import com.codewithngoc.instagallery.domain.models.LoginRequest
import com.codewithngoc.instagallery.domain.models.LoginResponse
import com.codewithngoc.instagallery.domain.models.RegisterRequest
import com.codewithngoc.instagallery.domain.models.UpdateProfileRequest
import com.codewithngoc.instagallery.domain.models.UserProfileResponse
import com.codewithngoc.instagallery.domain.repos.AuthRepository
import com.codewithngoc.instagallery.domain.services.AuthService
import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.insert
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class AuthServiceImpl(
    private val authRepository: AuthRepository,
    private val app: Application // Giả sử bạn cần truy cập vào ứng dụng để lấy jwtService
): AuthService {

    // ✅ Đăng ký người dùng mới
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

    // ✅ Đăng nhập người dùng
    override suspend fun loginUser(request: LoginRequest): Result<LoginResponse> {
        return try {
            val user = authRepository.loginUser(request)
            if (user != null) {
                // 🔑 Sinh JWT token
                val token = app.generateToken(user) // giả sử bạn có jwtService

                // 🔑 Sinh refresh token
                val refreshToken = UUID.randomUUID().toString()
                val now = Instant.now()
                val refreshExpiredAt  = now.plus(7, ChronoUnit.DAYS) // refresh token có hạn 7 ngày

                // ✅ Lưu refresh token vào bảng UserSessionsTable
                dbQuery {
                    UserSessionsTable.insert {
                        it[userId] = user.userId
                        it[deviceInfo] = "Unknown device" // có thể lấy từ User-Agent
                        it[ipAddress] = "0.0.0.0"         // có thể lấy từ call.request.origin.remoteHost
                        it[UserSessionsTable.refreshToken] = refreshToken
                        it[createdAt] = now
                        it[expiredAt] = refreshExpiredAt
                    }
                }

                val response = LoginResponse(
                    userId = user.userId,
                    username = user.username,
                    token = token,
                    refreshToken = refreshToken
                )
                Result.success(response)
            } else {
                Result.failure(Exception("Invalid email or password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Đăng xuất
    override suspend fun logout(refreshToken: String): Result<Boolean> {
        return try {
           val success = authRepository.logout(refreshToken)
            if (success) {
                Result.success(true)
            } else {
                Result.failure(Exception("Refresh token not found or could not be invalidated"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ✅ Hàm lấy thông tin người dùng theo ID
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

    // ✅ Hàm cập nhật thông tin người dùng
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

    // ✅ Hàm xóa người dùng theo ID
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

    // ✅ Lấy thông tin công khai của người dùng theo username
    override suspend fun getPublicProfileByUsername(username: String): Result<UserProfileResponse> {
        return try {
            val user = authRepository.getPublicProfileByUsername(username)
            if (user != null) {
                Result.success(user.toUserProfileResponse())
            } else {
                Result.failure(Exception("❌ User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}