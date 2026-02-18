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

            // S3에 직접 전송 시 인증 경로 겹쳐서 문제 발생 (별도로 생성하자니)
            // 이에 요청 주소에 'amazonaws.com'이 포함되어 있는지 확인(S3 주소 체크해서)
            val originalRequest = chain.request()
            if (originalRequest.url.host.contains("amazonaws.com")) {
                // S3 직접 업로드 요청이므로 토큰을 추가하지 않고 그대로 진행
                return chain.proceed(originalRequest)
            }

            val accessToken = runBlocking { appDataStoreRepository.getAccessToken() }
            val testToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzcxNDAyMzIxLCJleHAiOjE3NzE0MT" +
                    "MxMjF9.7KO-yzAlkZhODXe56VA2O5MRuvOEA2Q_ueUQs4LK2Nw6Cq_rWRp2RdlXcn4490_HlMXdGH3uK0iKEHFs35UkaQ"

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
