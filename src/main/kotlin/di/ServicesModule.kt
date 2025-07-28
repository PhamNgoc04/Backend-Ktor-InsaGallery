package com.codewithngoc.instagallery.di

import com.codewithngoc.instagallery.data.repos.AuthRepositoryImpl
import com.codewithngoc.instagallery.data.services.AuthServiceImpl
import com.codewithngoc.instagallery.data.services.PostServiceImpl
import com.codewithngoc.instagallery.domain.repos.AuthRepository
import com.codewithngoc.instagallery.domain.services.AuthService
import com.codewithngoc.instagallery.domain.services.PostService
import org.koin.dsl.module

val servicesModule = module {
    single<AuthService>() {
        AuthServiceImpl(get())
    }

    single<PostService> {
        PostServiceImpl(get())
    }

}