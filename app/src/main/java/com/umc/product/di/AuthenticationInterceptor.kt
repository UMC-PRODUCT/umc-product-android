package com.umc.product.di

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

// TODO 토큰 관련 Repository 필요
@Singleton
class AuthenticationInterceptor
    @Inject
    constructor() : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {

            // S3에 직접 전송 시 인증 경로 겹쳐서 문제 발생 (별도로 생성하자니 문제)
            // 요청 주소에 'amazonaws.com'이 포함되어 있는지 확인
            val originalRequest = chain.request()
            if (originalRequest.url.host.contains("amazonaws.com")) {
                // S3 직접 업로드 요청이므로 토큰을 추가하지 않고 그대로 진행
                return chain.proceed(originalRequest)
            }

            // val accessToken = runBlocking { repository.getAccessToken().first() }
            val testToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMDIiLCJpYXQiOjE3NzA2NTA4MDYsImV4cCI6MTc3MDY1NDQwN" +
                    "n0.tX75j8P_kU8wytOZKe4BI2oBF3nkrWfQwnSf22xQfPQY36DDpriUWz61Bav4_l7eSeKQWfvT_xfUNiY7H7vbew"

            val request =
                chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer ${testToken}").build()

            Log.d(
                "RETROFIT",
                "AuthenticationInterceptor - intercept() called / request header: ${request.headers}",
            )
            return chain.proceed(request)
        }
    }
