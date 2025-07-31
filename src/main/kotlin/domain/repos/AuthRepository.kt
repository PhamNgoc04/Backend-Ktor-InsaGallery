package com.codewithngoc.instagallery.domain.repos

import com.codewithngoc.instagallery.db.entities.UserEntity
import com.codewithngoc.instagallery.domain.models.LoginRequest
import com.codewithngoc.instagallery.domain.models.LoginResponse
import com.codewithngoc.instagallery.domain.models.RegisterRequest
import com.codewithngoc.instagallery.domain.models.UpdateProfileRequest
import com.codewithngoc.instagallery.domain.models.User

// AuthRepository.kt
interface AuthRepository {
    // ✅ Tạo người dùng mới (hash password trước khi gọi hàm này)
    suspend fun registerUser(registerRequest: RegisterRequest): User?

    // ✅ Xác thực đăng nhập → trả về User nếu đúng
    suspend fun loginUser(request: LoginRequest): User?

    // ✅ Lấy thông tin người dùng theo username
    suspend fun getUserById(userId: Int): UserEntity?

    // ✅ Xác thực user khi đăng nhập
    suspend fun authenticate(request: LoginRequest): User?

    // ✅ Xóa người dùng theo ID
    suspend fun deleteUserById(id: Int): Boolean

    // ✅ Đăng xuất
    suspend fun logout(refreshToken: String): Boolean

    // ✅ Cập nhật thông tin người dùng
    suspend fun updateUserProfile(userId: Int, updateRequest: UpdateProfileRequest): User?
}
