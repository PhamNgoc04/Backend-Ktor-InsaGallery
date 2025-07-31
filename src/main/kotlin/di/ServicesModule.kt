package com.codewithngoc.instagallery.di

import com.codewithngoc.instagallery.data.services.AuthServiceImpl
import com.codewithngoc.instagallery.data.services.PostServiceImpl
import com.codewithngoc.instagallery.domain.services.AuthService
import com.codewithngoc.instagallery.domain.services.PostService
import io.ktor.server.application.Application
import org.koin.dsl.module

val servicesModule = module {
    single<AuthService> {
        AuthServiceImpl(
            authRepository = get(),
            app = getProperty("application")
        )
    }

    single<PostService> {
        PostServiceImpl(
            postRepository = get()
        )
    }
}