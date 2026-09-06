package com.umc.domain.usecase.ai

import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.ai.AiTextRepository
import javax.inject.Inject

class ClassifyCommunityThreadUseCase @Inject constructor(
    private val aiTextRepository: AiTextRepository,
) {
    suspend operator fun invoke(
        title: String,
        description: String,
        onDownloadProgress: (Int) -> Unit = {},
    ): ApiState<String> {
        // 신호가 뚜렷한 스레드는 규칙으로 확정해 추론 자체를 건너뛴다.
        // 미지원 기기에서도 동작하고, 같은 입력에 같은 답이 나오며, 대기 시간도 없다.
        CommunityCategoryRules.classify(title, description)?.let { return ApiState.Success(it) }

        val prompt = """
            아래 스레드의 제목과 설명을 보고 가장 적절한 카테고리 하나를 골라주세요.

            가능한 카테고리:
            STUDY
            PROJECT
            QNA
            FREE

            제목: $title
            설명: $description

            반드시 STUDY, PROJECT, QNA, FREE 중 하나만 출력하세요.
        """.trimIndent()

        return aiTextRepository.generate(
            prompt = prompt,
            onDownloadProgress = onDownloadProgress,
        )
    }
}