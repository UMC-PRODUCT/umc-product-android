package com.umc.product.di

import android.content.Context
import com.umc.data.api.RemoteConfigApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RemoteConfigNetworkModule {

    // 원격 설정 저장소(UMC-PRODUCT/umc-product-android-config)가 올라가는 GitHub Pages
    private const val REMOTE_CONFIG_BASE_URL = "https://umc-product.github.io/"

    private const val CACHE_DIR = "remote_config"
    private const val CACHE_SIZE_BYTES = 1L * 1024 * 1024

    /**
     * 설정 파일 전용 클라이언트
     *
     * - API 서버용 클라이언트를 쓰면 로그인 토큰이 GitHub 으로 함께 나가므로 따로 둔다
     * - 디스크 캐시를 붙여 GitHub Pages 의 Cache-Control(10분)과 ETag 를 그대로 따른다.
     *   10분 안에는 네트워크를 쓰지 않고, 그 뒤에는 바뀐 게 없으면 304 로 끝난다
     */
    @Provides
    @Singleton
    @RemoteConfigOkHttpClient
    fun provideRemoteConfigHttpClient(
        @ApplicationContext context: Context,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .cache(Cache(File(context.cacheDir, CACHE_DIR), CACHE_SIZE_BYTES))
            .readTimeout(10, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRemoteConfigApi(
        @RemoteConfigOkHttpClient okHttpClient: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory
    ): RemoteConfigApi {
        return Retrofit.Builder()
            .baseUrl(REMOTE_CONFIG_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(gsonConverterFactory)
            .build()
            .create(RemoteConfigApi::class.java)
    }
}
