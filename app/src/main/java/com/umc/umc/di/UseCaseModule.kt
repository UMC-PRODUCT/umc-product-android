package com.umc.umc.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

//    @Singleton
//    @Provides
//    fun provides머시기머시기(repository: Repository): UseCase명 {
//        return UseCase명(repository)
//    }
}