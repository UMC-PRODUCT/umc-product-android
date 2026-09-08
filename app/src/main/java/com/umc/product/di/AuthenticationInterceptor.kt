package com.umc.product.di

import android.util.Log
import com.umc.domain.repository.AppDataStoreRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthenticationInterceptor @Inject constructor(
    private val appDataStoreRepository: AppDataStoreRepository
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {

        // S3에 직접 전송 시 인증 경로 겹쳐서 문제 발생 (별도로 생성하자니)
        // 이에 요청 주소에 'amazonaws.com'이 포함되어 있는지 확인(S3 주소 체크해서)
        val originalRequest = chain.request()
        if (originalRequest.url.host.contains("amazonaws.com")) {
            // S3 직접 업로드 요청이므로 토큰을 추가하지 않고 그대로 진행
            return chain.proceed(originalRequest)
        }

        val accessToken = runBlocking { appDataStoreRepository.getAccessToken() }

        // addHeader 가 아니라 header 다. addHeader 는 기존 값을 두고 하나 더 붙이므로
        // Authorization 이 이미 있는 요청에서 헤더가 중복될 수 있다.
        val request =
            chain.request().newBuilder()
                .header("Authorization", "Bearer $accessToken").build()

        Log.d(
            "RETROFIT",
            "AuthenticationInterceptor - intercept() called / request header: ${request.headers}",
        )
        return chain.proceed(request)
    }
}