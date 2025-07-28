package com.codewithngoc.instagallery.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.codewithngoc.instagallery.domain.models.User
import java.util.*

class JwtProvider(
    private val jwtSecret: String,
    private val jwtIssuer: String,
    private val jwtAudience: String,
    private val claimField: String,
    private val expiresIn: Long
) {
    fun generateToken(user: User): String {
        return JWT.create()
            .withIssuer(jwtIssuer)
            .withAudience(jwtAudience)
            .withClaim(claimField, user.userId)
            .withExpiresAt(Date(System.currentTimeMillis() + expiresIn))
            .sign(Algorithm.HMAC256(jwtSecret))
    }
}
