package com.umc.product.di

import com.umc.domain.repository.AuthRepository
import com.umc.domain.repository.ChallengerRepository
import com.umc.domain.usecase.GetChallengerDetailUseCase
import com.umc.domain.usecase.PostLoginUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    @Singleton
    @Provides
    fun providesPostLoginUseCase(repository: AuthRepository): PostLoginUseCase {
        return PostLoginUseCase(repository)
    }

    @Singleton
    @Provides
    fun providesGetChallengerDetailUseCase(repository: ChallengerRepository): GetChallengerDetailUseCase {
        return GetChallengerDetailUseCase(repository)
    }
}
