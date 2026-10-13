package com.umc.product.di

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.umc.product.base.BASE_URL
import com.umc.product.extension.isJsonArray
import com.umc.product.extension.isJsonObject
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
        val loggingInterceptor =
            HttpLoggingInterceptor { message ->
                when {
                    !message.isJsonObject() && !message.isJsonArray() ->
                        Log.d("RETROFIT1", "CONNECTION INFO -> $message")
                    else ->
                        try {
                            Log.d(
                                "RETROFIT2",
                                GsonBuilder().setPrettyPrinting().create().toJson(
                                    JsonParser().parse(message),
                                ),
                            )
                        } catch (m: JsonSyntaxException) {
                            Log.d("RETROFIT3", message)
                        }
                }
            }
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
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
