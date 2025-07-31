package com.codewithngoc.instagallery.di

import com.codewithngoc.instagallery.data.services.AuthServiceImpl
import com.codewithngoc.instagallery.domain.services.AuthService
import io.ktor.server.application.Application
import org.koin.dsl.module

val servicesModule = module {
    single<AuthService> {
        AuthServiceImpl(
            authRepository = get(),
            app = getProperty("application")
        )
    }
}