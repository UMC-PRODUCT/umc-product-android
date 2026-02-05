package com.umc.product.di

import com.umc.data.api.KakaoSearchApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object KakaoNetworkModule {

    // 카카오 로컬 API용 Base URL
    private const val KAKAO_BASE_URL = "https://dapi.kakao.com/"

    @Provides
    @Singleton
    @KakaoOkHttpClient
    fun provideKakaoHttpClient(
        kakaoInterceptor: KakaoInterceptor,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .readTimeout(10, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(kakaoInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideKakaoSearchApi(
        @KakaoOkHttpClient okHttpClient: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory
    ): KakaoSearchApi {
        return Retrofit.Builder()
            .baseUrl(KAKAO_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(KakaoSearchApi::class.java)
    }

}