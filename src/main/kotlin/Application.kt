package com.codewithngoc.instagallery

import com.codewithngoc.instagallery.db.initDB
import com.codewithngoc.instagallery.di.configureKoin
import com.codewithngoc.instagallery.domain.services.AuthService
import com.codewithngoc.instagallery.domain.services.PostService
import com.codewithngoc.instagallery.routes.authRoutes
import com.codewithngoc.instagallery.routes.postRoutes
import com.codewithngoc.instagallery.security.configSecurity
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.get

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {

    configureKoin()
//    configureContentNegotiation()
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }
    initDB()

    val authService = get<AuthService>()
    val postService = get<PostService>()

    configSecurity(authService)

    authRoutes(authService)
    postRoutes(postService)
}
