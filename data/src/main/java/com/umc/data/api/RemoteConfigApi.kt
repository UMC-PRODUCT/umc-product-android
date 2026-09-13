package com.umc.data.api

import com.umc.data.response.remoteconfig.AppConfigResponse
import retrofit2.http.GET
import retrofit2.http.Header

/**
 * 원격 설정 파일 조회
 *
 * API 서버가 아니라 GitHub Pages 에 올라간 정적 JSON 이라 서버 응답 봉투(success/code/result)가 없다.
 */
interface RemoteConfigApi {

    @GET(APP_CONFIG_PATH)
    suspend fun getAppConfig(
        // 네트워크가 안 될 때만 캐시 강제 값을 넘긴다. 평소에는 null 이라 헤더가 붙지 않는다
        @Header("Cache-Control") cacheControl: String?,
    ): AppConfigResponse

    companion object {
        const val APP_CONFIG_PATH = "umc-product-android-config/app-config.json"
    }
}
