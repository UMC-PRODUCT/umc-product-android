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