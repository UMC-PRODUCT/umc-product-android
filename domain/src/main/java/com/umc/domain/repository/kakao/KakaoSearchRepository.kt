package com.umc.domain.repository.kakao

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.home.LocationItem

interface KakaoSearchRepository {
    suspend fun searchLocation(query: String): ApiState<List<LocationItem>>
}