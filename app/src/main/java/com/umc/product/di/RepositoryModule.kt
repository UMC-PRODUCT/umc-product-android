package com.umc.product.di

import com.umc.data.repository.AppDataStoreRepositoryImpl
import com.umc.domain.repository.AppDataStoreRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
//    @Singleton
//    @Binds
//    abstract fun provides머시기머시기(repositoryImpl: RepositoryImpl): Repository

    /**아래는 Local DataStore를 사용하기 위한 세팅**/
    @Binds
    @Singleton
    abstract fun bindAppDataStoreRepository(
        appDataStoreRepositoryImpl: AppDataStoreRepositoryImpl
    ): AppDataStoreRepository


}
