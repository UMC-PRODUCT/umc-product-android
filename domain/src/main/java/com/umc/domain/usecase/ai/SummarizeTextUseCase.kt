package com.umc.domain.usecase.ai

import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.ai.AiTextRepository
import javax.inject.Inject

/** 텍스트를 온디바이스 AI로 요약. 모델이 없으면 다운로드하며 진행률을 알린다 */
class SummarizeTextUseCase @Inject constructor(
    private val aiTextRepository: AiTextRepository,
) {
    suspend operator fun invoke(
        text: String,
        onDownloadProgress: (percent: Int) -> Unit = {},
    ): ApiState<String> {
        return aiTextRepository.summarize(text, onDownloadProgress)
    }
}
