package com.codewithngoc.instagallery.security

import com.codewithngoc.instagallery.config.jwtVerifier
import com.codewithngoc.instagallery.domain.models.AuthPrincipal
import com.codewithngoc.instagallery.domain.services.AuthService
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond
import io.ktor.http.HttpStatusCode

fun Application.configSecurity(authService: AuthService) {
    val jwtIssuer = environment.config.property("jwt.issuer").getString()
    val jwtAudience = environment.config.property("jwt.audience").getString()
    val claimField = environment.config.property("jwt.claimField").getString()
    val jwtRealm = environment.config.property("jwt.realm").getString()

    val verifier = jwtVerifier()

    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtRealm
            verifier(verifier)
            validate { cred ->
                val userId = cred.payload.getClaim(claimField).asInt()
                val role = cred.payload.getClaim("role").asString() ?: "USER"

                if (userId != null) {
                    AuthPrincipal(userId = userId, role = role)
                } else null
            }

            // Trả Json khi token sai hoặc hết hạn
            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("message" to "Token không hợp lệ hoặc đã hết hạn, vui lòng đăng nhập lại")
                )
            }
        }
    }
}