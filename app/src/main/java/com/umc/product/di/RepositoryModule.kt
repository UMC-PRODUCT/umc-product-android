package com.umc.product.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
//    @Singleton
//    @Binds
//    abstract fun provides머시기머시기(repositoryImpl: RepositoryImpl): Repository
}
