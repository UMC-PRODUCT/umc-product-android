package com.umc.product.di

import com.umc.data.repository.AuthRepositoryImpl
import com.umc.data.repository.ChallengerRepositoryImpl
import com.umc.domain.repository.AuthRepository
import com.umc.domain.repository.ChallengerRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Singleton
    @Binds
    abstract fun providesAuthRepository(repositoryImpl: AuthRepositoryImpl): AuthRepository

    @Singleton
    @Binds
    abstract fun bindsChallengerRepository(repositoryImpl: ChallengerRepositoryImpl): ChallengerRepository
}
