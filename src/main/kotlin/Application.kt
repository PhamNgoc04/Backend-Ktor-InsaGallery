package com.codewithngoc.instagallery

import com.codewithngoc.instagallery.data.services.AuthServiceImpl
import com.codewithngoc.instagallery.db.initDB
import com.codewithngoc.instagallery.di.configureKoin
import com.codewithngoc.instagallery.domain.services.AuthService
import com.codewithngoc.instagallery.domain.services.PostService
import com.codewithngoc.instagallery.routes.authRoutes
import com.codewithngoc.instagallery.routes.interactionRoutes
import com.codewithngoc.instagallery.security.configSecurity
import io.ktor.server.application.*
import org.koin.ktor.ext.get
import postRoutes

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureKoin()
    configureContentNegotiation()
    initDB()
    val authService = get<AuthService>()
    val postService = get<PostService>()

    configSecurity(authService)


    authRoutes(authService)
    postRoutes(postService)
}
