package com.umc.data.dataSource.remote.kakao

import com.umc.data.api.KakaoSearchApi
import com.umc.data.response.KakaoSearchResponse
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.FailState
import javax.inject.Inject

class KakaoRemoteDataSourceImpl @Inject constructor(
    private val kakaoSearchApi: KakaoSearchApi
) : KakaoRemoteDataSource {

    override suspend fun searchLocation(query: String): ApiState<KakaoSearchResponse> {
        return fetch { kakaoSearchApi.getKeywordSearch(query = query) }
    }


    private suspend fun <T> fetch(call: suspend () -> T): ApiState<T> {
        return try {
            ApiState.Success(call())
        } catch (e: Exception) {
            ApiState.Fail(FailState(false, "UNKNOWN", e.message ?: "알 수 없는 오류"))
        }
    }
}