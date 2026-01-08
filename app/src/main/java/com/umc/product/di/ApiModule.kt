package com.umc.product.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    //    @Singleton
//    @Provides
//    fun provide머시기머시기(retrofit: Retrofit): API Interface {
//        return retrofit.create(API Interface::class.java)
//    }
}
