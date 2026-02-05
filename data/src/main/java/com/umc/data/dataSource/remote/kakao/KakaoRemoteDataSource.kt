package com.umc.data.dataSource.remote.kakao

import com.umc.data.response.KakaoSearchResponse
import com.umc.domain.model.base.ApiState

interface KakaoRemoteDataSource {
    suspend fun searchLocation(query: String): ApiState<KakaoSearchResponse>
}

