package com.umc.data.dataSource.remote.kakao

import com.umc.data.api.KakaoSearchApi
import com.umc.data.dataSource.base.externalApiCall
import com.umc.data.response.KakaoSearchResponse
import com.umc.domain.model.base.ApiState
import javax.inject.Inject

class KakaoRemoteDataSourceImpl @Inject constructor(
    private val kakaoSearchApi: KakaoSearchApi
) : KakaoRemoteDataSource {

    // 카카오 API 는 ApiResponse 래퍼가 없어 apiCall 대신 externalApiCall 로 감싼다.
    override suspend fun searchLocation(query: String): ApiState<KakaoSearchResponse> {
        return externalApiCall { kakaoSearchApi.getKeywordSearch(query = query) }
    }
}
