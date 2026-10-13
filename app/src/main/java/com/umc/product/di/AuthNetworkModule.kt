package com.umc.product.di

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

@Module
@InstallIn(SingletonComponent::class)
object AuthNetworkModule {
    @Provides
    @Singleton
    @AuthOkHttpClient
    fun provideHttpClient(
        authenticator: TokenAuthenticator,
        authenticationInterceptor: AuthenticationInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .readTimeout(120, TimeUnit.SECONDS)
            // TCP 연결 수립 제한. 응답 대기(read)·업로드 전송(write)과는 별개라 짧게 둔다.
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            // HTTP/2 연결 생존 확인. 10초마다 ping 을 보내고 다음 주기까지 pong 이 없으면 연결을 닫는다.
            // 풀에서 재사용한 연결이면 진행 중이던 요청(POST 등 포함)을 OkHttp 가 새 연결로 다시 보낸다.
            // 그 호출이 직접 만든 연결이면 재전송 없이 네트워크 오류로 끝난다.
            // 네트워크가 잠깐 끊겼다 붙은 뒤 죽은 연결에 요청이 실려 read timeout(120초)까지 멈추던 문제를 막는다.
            // 업로드 호스트(S3)는 HTTP/1.1 이라 ping 대상이 아니다(2026-10 확인).
            .pingInterval(10, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authenticationInterceptor)
            .authenticator(authenticator)
            .build()
    }

    @Singleton
    @Provides
    @AuthRetrofit
    fun provideAuthRetrofit(
        @AuthOkHttpClient okHttpClient: OkHttpClient,
        gsonConverterFactory: GsonConverterFactory,
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(gsonConverterFactory)
            .build()
    }
}
