package com.umc.data.repository.kakao

import com.umc.data.api.KakaoSearchApi
import com.umc.data.dataSource.remote.kakao.KakaoRemoteDataSource
import com.umc.domain.model.base.ApiResponse
import com.umc.domain.repository.kakao.KakaoSearchRepository
import javax.inject.Inject
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.FailState
import com.umc.domain.model.base.map
import com.umc.domain.model.base.mapSuccessData
import com.umc.domain.model.home.LocationItem

/**일단 여기서 dataSource꺼 같이 정의 = 차후 분할
 *
 * 카카오의 Response 형 (KakaoSearchResponse) -> ApiState<List<LocationItem>>
 *
 * **/
class KakaoSearchRepositoryImpl @Inject constructor(
    private val kakaoRemoteDataSource: KakaoRemoteDataSource
) : KakaoSearchRepository {
    override suspend fun searchLocation(query: String): ApiState<List<LocationItem>> {
        // DataSource에서 받아온 DTO를 Domain 모델로 변환
        return kakaoRemoteDataSource.searchLocation(query).map { response ->
            response.documents.map { doc ->
                LocationItem(
                    title = doc.placeName,
                    address = doc.roadAddressName.ifEmpty { doc.addressName },
                    latitude = doc.y.toDoubleOrNull() ?: 0.0,
                    longitude = doc.x.toDoubleOrNull() ?: 0.0
                )
            }
        }
    }

}