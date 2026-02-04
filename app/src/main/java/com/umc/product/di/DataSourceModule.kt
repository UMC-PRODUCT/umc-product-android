package com.umc.product.di

import com.umc.data.dataSource.AuthRemoteDataSource
import com.umc.data.dataSource.ChallengerRemoteDataSource
import com.umc.data.dataSource.remote.AuthRemoteDataSourceImpl
import com.umc.data.dataSource.remote.ChallengerRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {
    @Singleton
    @Binds
    abstract fun providesAuthDataSource(dataSourceImpl: AuthRemoteDataSourceImpl): AuthRemoteDataSource

    @Singleton
    @Binds
    abstract fun bindsChallengerRemoteDataSource(dataSourceImpl: ChallengerRemoteDataSourceImpl): ChallengerRemoteDataSource
}
