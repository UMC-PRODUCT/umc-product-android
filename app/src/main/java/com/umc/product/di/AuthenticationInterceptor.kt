package com.umc.product.di

import android.util.Log
import com.umc.data.dataSource.local.AppDataStore
import com.umc.domain.repository.AppDataStoreRepository
import com.umc.presentation.util.ULog
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
            val accessToken = runBlocking { appDataStoreRepository.getAccessToken() }
            val testToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxMDEiLCJpYXQiOjE3NzAzOTg2MDAsImV4cCI6MTc3MDQwMjIwMH0.WYCX4j" +
                    "Gxa3mLiiL6JFgGE8kEVBOKW1awJ0Zir-15Q8yhg3dntYaCmux408_8ybRjbQ-4JUAErIVGguT3Yse93Q"

            //TODO 확인용 지워야 함
            ULog.d("실제 헤더 : $accessToken")
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
