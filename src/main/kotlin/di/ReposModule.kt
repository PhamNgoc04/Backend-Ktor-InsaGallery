package com.codewithngoc.instagallery.di

import com.codewithngoc.instagallery.data.repos.AuthRepositoryImpl
import com.codewithngoc.instagallery.domain.repos.AuthRepository
import org.koin.dsl.module

val reposModule = module {

    single<AuthRepository> {
        AuthRepositoryImpl()
    }

}