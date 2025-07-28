package com.codewithngoc.instagallery.di

import com.codewithngoc.instagallery.data.repos.AuthRepositoryImpl
import com.codewithngoc.instagallery.data.repos.PostRepositoryImpl
import com.codewithngoc.instagallery.domain.repos.AuthRepository
import com.codewithngoc.instagallery.domain.repos.PostRepository
import com.codewithngoc.instagallery.domain.services.AuthService
import org.koin.dsl.module

val reposModule = module {
    single<AuthRepository>() {
        AuthRepositoryImpl()
    }

    single<PostRepository> {
        PostRepositoryImpl()
    }

}