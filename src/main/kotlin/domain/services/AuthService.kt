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

    // Đăng ký người dùng mới
    suspend fun registerUser(registerRequest: RegisterRequest): Result<AuthResponse>

    // Đăng nhập người dùng
    suspend fun loginUser(request: LoginRequest): Result<LoginResponse>

    // Đăng xuất người dùng
    suspend fun logout(refreshToken: String): Result<Boolean>

    // Lấy thông tin người dùng hiện tại (dựa trên userId từ token)
    suspend fun getUserById(userId: Int): Result<UserProfileResponse>

    // Cập nhật thông tin người dùng
    suspend fun updateUserProfile(userId: Int, updateRequest: UpdateProfileRequest): Result<UserProfileResponse>

    // Xoá người dùng (admin hoặc self-delete)
    suspend fun deleteUserById(userId: Int): Result<Unit>

    // Lấy thông tin công khai của người dùng theo username
    suspend fun getPublicProfileByUsername(username: String): Result<UserProfileResponse>

}