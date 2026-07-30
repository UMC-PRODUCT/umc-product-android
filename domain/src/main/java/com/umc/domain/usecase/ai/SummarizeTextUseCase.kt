package com.umc.domain.usecase.ai

import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.ai.AiTextRepository
import javax.inject.Inject

/** 텍스트를 온디바이스 AI로 요약. 필요 시 모델 다운로드까지 수행됨 */
class SummarizeTextUseCase @Inject constructor(
    private val aiTextRepository: AiTextRepository,
) {
    suspend operator fun invoke(text: String): ApiState<String> {
        return aiTextRepository.summarize(text)
    }
}
