package com.codewithngoc.instagallery.domain.services

import com.codewithngoc.instagallery.domain.models.AuthResponse
import com.codewithngoc.instagallery.domain.models.LoginRequest
import com.codewithngoc.instagallery.domain.models.RegisterRequest
import com.codewithngoc.instagallery.domain.models.User
import com.codewithngoc.instagallery.domain.models.UserProfileResponse

// AuthService.kt
interface AuthService {
    suspend fun registerUser(registerRequest: RegisterRequest): User?
    suspend fun loginUser(request: LoginRequest): User?
    suspend fun getUserById(id: Int): UserProfileResponse?
    suspend fun deleteUserById(id: Int): Boolean
    suspend fun logout(refreshToken: String): Boolean

}
