package com.codewithngoc.instagallery.domain.auth

import io.ktor.server.auth.*

data class UserIdPrincipalForUser(val userId: Int) : Principal