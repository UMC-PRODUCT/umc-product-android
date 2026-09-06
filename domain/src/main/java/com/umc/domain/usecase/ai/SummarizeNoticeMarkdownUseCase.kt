package com.umc.domain.usecase.ai

import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.ai.AiTextRepository
import javax.inject.Inject

/**
 * 복사해 온 공지 전문을 온디바이스 AI로 요약하고 앱 마크다운 형식(iOS 호환 스펙)을 입힌다.
 * 결과는 작성 중인 본문의 커서 위치에 붙여넣는다
 */
class SummarizeNoticeMarkdownUseCase @Inject constructor(
    private val aiTextRepository: AiTextRepository,
) {
    suspend operator fun invoke(
        source: String,
        onDownloadProgress: (percent: Int) -> Unit = {},
    ): ApiState<String> {
        return aiTextRepository.generate(buildPrompt(source), onDownloadProgress)
            .sanitizeMarkdown()
    }

    private fun buildPrompt(source: String): String {
        return """
            당신은 대학 동아리 공지문 편집자입니다. 아래 원문을 공지 본문으로 쓸 수 있게 요약하세요.
            $NOTICE_MARKDOWN_FORMAT_RULES
            내용 규칙:
            - 날짜, 시간, 장소, 인원, 기한, 링크, 준비물 등 실행에 필요한 정보는 반드시 남길 것
            - 원문에 없는 내용을 지어내지 말 것
            - 인사말과 중복되는 문장은 덜어내고 핵심만 남길 것
            - 요약된 공지 본문만 출력할 것 (설명이나 머리말 추가 금지)

            원문:
            $source
        """.trimIndent()
    }
}
