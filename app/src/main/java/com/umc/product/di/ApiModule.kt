package com.umc.product.di

import com.umc.data.api.AuthApi
import com.umc.data.api.ChallengerApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Singleton
    @Provides
    fun provideAuthApi(@NormalRetrofit retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Singleton
    @Provides
    fun provideChallengerApi(@AuthRetrofit retrofit: Retrofit): ChallengerApi {
        return retrofit.create(ChallengerApi::class.java)
    }
}
