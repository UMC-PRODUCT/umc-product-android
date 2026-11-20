package com.umc.product.di

import com.umc.domain.repository.AppDataStoreRepository
import com.umc.product.base.BASE_URL
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthenticationInterceptor @Inject constructor(
    private val appDataStoreRepository: AppDataStoreRepository
) : Interceptor {

    // 토큰을 붙여도 되는 호스트는 API 서버 하나뿐이다.
    private val apiHost = BASE_URL.toHttpUrl().host

    override fun intercept(chain: Interceptor.Chain): Response {
        // 허용 목록으로 거른다. presigned 업로드처럼 서버가 내려준 주소(@Url)로 가는 요청에 토큰을 붙이면
        // 업로드 호스트가 S3 가 아닌 곳(CloudFront·자체 도메인 등)으로 바뀌는 순간 액세스 토큰이 외부로 나간다.
        val originalRequest = chain.request()
        if (originalRequest.url.host != apiHost) {
            return chain.proceed(originalRequest)
        }

        val accessToken = runBlocking { appDataStoreRepository.getAccessToken() }

        // addHeader 가 아니라 header 다. addHeader 는 기존 값을 두고 하나 더 붙이므로
        // Authorization 이 이미 있는 요청에서 헤더가 중복될 수 있다.
        // 헤더 값에 토큰이 들어 있으므로 로그로 남기지 않는다(#374, HttpLoggingInterceptor 도 가린다).
        val request =
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $accessToken").build()

        return chain.proceed(request)
    }
}
