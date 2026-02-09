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
            // val accessToken = runBlocking { repository.getAccessToken().first() }
            val testToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMDIiLCJpYXQiOjE3NzA2MzkwNzgsImV4" +
                    "cCI6MTc3MDY0MjY3OH0.I5Q5Y-sKNwPge3F2SNyY_vznw0uS_hT1a0y-W5diZd3QsyiU5xBbxK99rUN3bszQ8OBl99BK7567c8UcAdmJ0Q"

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
