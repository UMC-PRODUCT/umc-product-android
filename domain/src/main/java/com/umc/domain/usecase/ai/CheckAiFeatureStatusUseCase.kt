package com.umc.domain.usecase.ai

import com.umc.domain.model.enums.AiFeatureStatus
import com.umc.domain.model.enums.AiTextFeature
import com.umc.domain.repository.ai.AiTextRepository
import javax.inject.Inject

/** 온디바이스 AI 기능 가용 상태 확인. 메뉴/버튼 노출 여부 판단에 사용 */
class CheckAiFeatureStatusUseCase @Inject constructor(
    private val aiTextRepository: AiTextRepository,
) {
    suspend operator fun invoke(feature: AiTextFeature): AiFeatureStatus {
        return aiTextRepository.checkStatus(feature)
    }
}
