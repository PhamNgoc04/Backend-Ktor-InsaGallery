package com.codewithngoc.instagallery.config

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.codewithngoc.instagallery.domain.models.User
import io.ktor.server.application.*
import java.util.*

/**
 * Tạo token JWT khi người dùng đăng nhập hoặc đăng ký thành công.
 */
fun Application.generateToken(user: User): String {
    // Lấy các thông tin cấu hình từ file application.conf
    val jwtSecret = System.getenv("INSTAGALLERY_JWT_SECRET")
        ?: throw RuntimeException("Environment variable INSTAGALLERY_JWT_SECRET not set")

    val jwtIssuer = environment.config.property("jwt.issuer").getString()
    val jwtAudience = environment.config.property("jwt.audience").getString()
    val claimField = environment.config.property("jwt.claimField").getString()
    val expiresIn = environment.config.property("jwt.expiresIn").getString().toLong()

    // Tạo token
    return JWT.create()
        .withIssuer(jwtIssuer)
        .withAudience(jwtAudience)
        .withClaim(claimField, user.userId) // Sử dụng userId từ model User của bạn
        .withExpiresAt(Date(System.currentTimeMillis() + expiresIn))
        .sign(Algorithm.HMAC256(jwtSecret))
}

/**
 * Tạo một JWTVerifier để xác thực token khi client gửi request đến.
 */
fun Application.jwtVerifier(): JWTVerifier {
    // Lấy các thông tin cấu hình
    val jwtSecret = System.getenv("INSTAGALLERY_JWT_SECRET")
        ?: throw RuntimeException("Environment variable INSTAGALLERY_JWT_SECRET not set")
    val jwtIssuer = environment.config.property("jwt.issuer").getString()
    val jwtAudience = environment.config.property("jwt.audience").getString()

    // Tạo và trả về bộ xác thực JWT
    return JWT.require(Algorithm.HMAC256(jwtSecret))
        .withIssuer(jwtIssuer)
        .withAudience(jwtAudience)
        .build()
}