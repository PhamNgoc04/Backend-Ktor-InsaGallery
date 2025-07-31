package com.codewithngoc.instagallery

import com.codewithngoc.instagallery.db.initDB
import com.codewithngoc.instagallery.di.configureKoin
import com.codewithngoc.instagallery.domain.services.AuthService
import com.codewithngoc.instagallery.routes.authRoutes
import com.codewithngoc.instagallery.security.configSecurity
import io.ktor.server.application.*
import org.koin.ktor.ext.get

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {

    configureKoin()
    configureContentNegotiation()
    initDB()

    val authService = get<AuthService>()
    configSecurity(authService)

    authRoutes(authService)
}
