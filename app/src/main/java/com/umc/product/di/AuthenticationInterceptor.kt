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
            val testToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMDEiLCJpYXQiOjE3NzA1MzE3MjUsImV4cCI6MTc3MDUzNT" +
                    "MyNX0._dLYQPt5KRzzpff5wMgPZ6HmZ63AmFx0vDzPuJZpNF9ZguFVWH4tmh2b4qoPW96tyoazxeXgklmZUFTiFm3rMQ"

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
