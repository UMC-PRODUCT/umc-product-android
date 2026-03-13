package com.umc.domain.usecase.kakao

import com.umc.domain.repository.kakao.KakaoSearchRepository
import javax.inject.Inject

class GetSearchLocationUseCase @Inject constructor(
    private val searchRepository: KakaoSearchRepository
) {
    suspend operator fun invoke(query: String) = searchRepository.searchLocation(query)
}