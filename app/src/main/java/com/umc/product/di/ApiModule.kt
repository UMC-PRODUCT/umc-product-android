package com.umc.product.di

import com.umc.data.api.AuthApi
import com.umc.data.api.MemberApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**여기에다가 data->api에서 정의한 API를 retrofit을 이용해 연결**/

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
    fun provideMemberApi(@AuthRetrofit retrofit: Retrofit): MemberApi {
        return retrofit.create(MemberApi::class.java)
    }

}
