package com.codewithngoc.instagallery.domain.models

import io.ktor.server.auth.Principal


data class AuthPrincipal(
    val userId: Int,
    val role: String // hoặc Role nếu bạn muốn dùng enum trực tiếp
) : Principal
