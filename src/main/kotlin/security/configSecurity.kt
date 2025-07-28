package com.codewithngoc.instagallery.security

import com.codewithngoc.instagallery.config.jwtVerifier
import com.codewithngoc.instagallery.domain.services.AuthService
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.jwt.jwt

fun Application.configSecurity(authService: AuthService) {
    // Lấy các thông tin cấu hình từ file application.conf
    val jwtSecret = System.getenv("jwt_secret")
    val jwtIssuer = environment.config.property("jwt.issuer").getString()
    val jwtAudience = environment.config.property("jwt.audience").getString()
    val claimField = environment.config.property("jwt.claimField").getString()
    val jwtRealm = environment.config.property("jwt.realm").getString()

    // Tạo JWTVerifier để xác thực token JWT
    val verifier = jwtVerifier()

    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtRealm
            verifier(verifier)
            validate { cred ->
                cred.payload.getClaim(claimField).asInt()?.let { userId ->
                    val user = authService.getUserById(userId)
                    if (user != null) {
                        UserIdPrincipal(userId.toString()) // ✅ Trả về principal
                    } else {
                        null // ❌ User không tồn tại → fail auth
                    }
                }
            }
        }
    }
}
