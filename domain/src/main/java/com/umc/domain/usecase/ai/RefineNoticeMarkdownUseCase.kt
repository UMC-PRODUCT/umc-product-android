package com.umc.domain.usecase.ai

import com.umc.domain.model.base.ApiState
import com.umc.domain.repository.ai.AiTextRepository
import javax.inject.Inject

/**
 * 공지 초안을 온디바이스 AI로 다듬고 앱 마크다운 형식(iOS 호환 스펙)을 입힌다.
 * 형식 규칙은 MarkdownVisualTransformation이 렌더링하는 문법과 일치해야 함
 */
class RefineNoticeMarkdownUseCase @Inject constructor(
    private val aiTextRepository: AiTextRepository,
) {
    suspend operator fun invoke(content: String): ApiState<String> {
        return aiTextRepository.generate(buildPrompt(content))
    }

    private fun buildPrompt(content: String): String {
        return """
            당신은 대학 동아리 공지문 편집자입니다. 아래 초안을 자연스럽고 격식 있는 공지문으로 다듬으세요.

            형식 규칙 (아래 마크다운 문법만 사용):
            - 큰 제목: "# 제목", 소제목: "## 소제목"
            - 강조: **굵게**, 밑줄: <u>텍스트</u>, 취소선: ~~텍스트~~
            - 목록: "- 항목"
            - 마커와 내용 사이에 공백을 넣지 말 것 (** 텍스트 ** 금지, **텍스트** 형태만 허용)
            - 위에 없는 다른 마크다운 문법은 사용 금지

            내용 규칙:
            - 원문의 정보(날짜, 장소, 인원 등)를 빠뜨리거나 지어내지 말 것
            - 다듬어진 공지 본문만 출력할 것 (설명이나 인사말 추가 금지)

            초안:
            $content
        """.trimIndent()
    }
}
