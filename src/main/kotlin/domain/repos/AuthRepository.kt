package com.codewithngoc.instagallery.domain.repos

import com.codewithngoc.instagallery.domain.models.LoginRequest
import com.codewithngoc.instagallery.domain.models.RegisterRequest
import com.codewithngoc.instagallery.domain.models.User

// AuthRepository.kt
interface AuthRepository {
    suspend fun registerUser(registerRequest: RegisterRequest): User?
    suspend fun loginUser(request: LoginRequest): User?
    suspend fun getUserById(id: Int): User?

    // ✅ Xác thực user khi đăng nhập
    suspend fun authenticate(request: LoginRequest): User?

    // ✅ Hash mật khẩu trước khi lưu vào DB
    fun hashPassword(password: String): String

    // ✅ So sánh mật khẩu người dùng nhập với mật khẩu đã hash
    fun verifyPassword(plainPassword: String, hashedPassword: String): Boolean
    suspend fun deleteUserById(id: Int): Boolean

    // ✅ Đăng xuất
    suspend fun logout(refreshToken: String): Boolean
}
