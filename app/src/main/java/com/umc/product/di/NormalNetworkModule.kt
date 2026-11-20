package com.umc.product.di

import android.util.Log
import com.umc.product.BuildConfig
import com.umc.product.base.BASE_URL
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
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object NormalNetworkModule {
    @Provides
    @Singleton
    @Named("CommunityWebSocketUrl")
    fun provideCommunityWebSocketUrl(): String =
        BASE_URL
            .replaceFirst("https://", "wss://")
            .replaceFirst("http://", "ws://") + "ws/websocket"

    @Provides
    @Singleton
    fun provideHttpClient(loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .readTimeout(120, TimeUnit.SECONDS)
            // TCP 연결 수립 제한. AuthNetworkModule 과 같은 값으로 맞춘다.
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            // HTTP/2 연결 생존 확인(AuthNetworkModule 과 동일).
            // 이 클라이언트로 여는 커뮤니티 채팅 웹소켓에도 같은 주기의 ping 이 적용되어,
            // pong 이 없으면 onFailure 로 떨어져 기존 재연결 루프가 돈다.
            .pingInterval(10, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideConverterFactory(): GsonConverterFactory {
        return GsonConverterFactory.create()
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        // run-umc-app 스킬의 net·verify 가 RETROFIT1 태그의 "--> GET url" / "<-- 200 url" 줄을 읽는다. 태그를 바꾸면 함께 바꾼다.
        val loggingInterceptor = HttpLoggingInterceptor { message -> Log.d("RETROFIT1", "CONNECTION INFO -> $message") }
        // 본문에는 로그인 비밀번호, 토큰, refresh token 이 담기므로 디버그에서도 남기지 않는다(#374).
        // 디버그는 요청·응답 줄과 헤더만 남기고, 릴리스는 아무것도 남기지 않는다.
        // 모든 OkHttp 클라이언트가 이 인터셉터를 함께 쓰므로 여기서 한 번만 정한다.
        loggingInterceptor.level =
            if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.HEADERS else HttpLoggingInterceptor.Level.NONE
        // 디버그 로그에서도 인증 헤더 값(Bearer 토큰, 카카오 REST 키)과 쿠키는 가린다.
        listOf("Authorization", "Cookie", "Set-Cookie").forEach(loggingInterceptor::redactHeader)
        return loggingInterceptor
    }

    @Singleton
    @Provides
    @NormalRetrofit
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(gsonConverterFactory)
            .build()
    }
}
