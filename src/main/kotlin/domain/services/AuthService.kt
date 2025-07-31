package com.codewithngoc.instagallery.domain.services

import com.codewithngoc.instagallery.db.entities.UserEntity
import com.codewithngoc.instagallery.domain.models.AuthResponse
import com.codewithngoc.instagallery.domain.models.ChangePasswordRequest
import com.codewithngoc.instagallery.domain.models.LoginRequest
import com.codewithngoc.instagallery.domain.models.LoginResponse
import com.codewithngoc.instagallery.domain.models.RegisterRequest
import com.codewithngoc.instagallery.domain.models.UpdateProfileRequest
import com.codewithngoc.instagallery.domain.models.UserProfileResponse

interface AuthService {

    suspend fun registerUser(registerRequest: RegisterRequest): Result<AuthResponse>

    suspend fun loginUser(request: LoginRequest): Result<LoginResponse>

    suspend fun logout(refreshToken: String): Boolean

    // Lấy thông tin người dùng hiện tại (dựa trên userId từ token)
    suspend fun getUserById(userId: Int): Result<UserProfileResponse>

    // Cập nhật thông tin người dùng
    suspend fun updateUserProfile(userId: Int, updateRequest: UpdateProfileRequest): Result<UserProfileResponse>

    // Xoá người dùng (admin hoặc self-delete)
    suspend fun deleteUserById(userId: Int): Result<Unit>

}