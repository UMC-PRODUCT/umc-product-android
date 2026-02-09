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

            // S3에 직접 전송 시 인증 경로 겹쳐서 문제 발생 (별도로 생성하자니)
            // 이에 요청 주소에 'amazonaws.com'이 포함되어 있는지 확인(S3 주소 체크해서)
            val originalRequest = chain.request()
            if (originalRequest.url.host.contains("amazonaws.com")) {
                // S3 직접 업로드 요청이므로 토큰을 추가하지 않고 그대로 진행
                return chain.proceed(originalRequest)
            }

            // val accessToken = runBlocking { repository.getAccessToken().first() }
            val testToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMDIiLCJpYXQiOjE3NzA2NjMwNDQsImV4cCI6MTc3MDY2NjY0NH0.UdqmkhjQ" +
                    "BXqp2IOpw4CTzqM26Qs91gda-tVtIlq6f-G-gCUTy_an-GlCo7ThleGkU2xaK23J1Gz3in5qE3RcFA"

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
