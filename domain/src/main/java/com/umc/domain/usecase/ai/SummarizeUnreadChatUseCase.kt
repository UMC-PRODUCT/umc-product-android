package com.umc.domain.usecase.ai

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.FailState
import com.umc.domain.model.community.thread.CommunityMessageType
import com.umc.domain.model.community.thread.CommunityThreadMessage
import com.umc.domain.repository.ai.AiTextRepository
import javax.inject.Inject

/** 읽지 않은 채팅 메시지를 Gemini Nano로 요약한다. */
class SummarizeUnreadChatUseCase @Inject constructor(
    private val aiTextRepository: AiTextRepository,
) {
    suspend operator fun invoke(
        messages: List<CommunityThreadMessage>,
        onDownloadProgress: (percent: Int) -> Unit = {},
    ): ApiState<String> {
        val source = messages
            .asSequence()
            .filter { it.type == CommunityMessageType.TEXT }
            .mapNotNull { message ->
                message.content
                    ?.trim()
                    ?.takeIf(String::isNotBlank)
                    ?.let { content ->
                        val sender = message.senderName.orEmpty().ifBlank { "참여자" }
                        "$sender: $content"
                    }
            }
            .joinToString("\n")
            .take(MAX_SOURCE_LENGTH)

        if (source.isBlank()) {
            return ApiState.Fail(
                FailState(
                    code = EMPTY_MESSAGES_CODE,
                    message = "요약할 텍스트 메시지가 없어요.",
                ),
            )
        }

        val prompt = """
            다음은 사용자가 읽지 않은 채팅 메시지입니다. 대화의 핵심을 한국어로 요약하세요.

            규칙:
            - 중요한 결정, 일정, 할 일, 질문을 우선 정리할 것
            - 대화에 없는 내용을 지어내지 말 것
            - 핵심 내용과 할 일이 있다면 구분할 것
            - 불필요한 인사와 반복 표현은 제외할 것
            - 각 요약 항목은 새 줄에서 "- "로 시작할 것
            - 간결한 요약 결과만 출력할 것

            읽지 않은 메시지:
            $source
        """.trimIndent()

        return aiTextRepository.generate(prompt, onDownloadProgress)
    }

    private companion object {
        const val MAX_SOURCE_LENGTH = 12_000
        const val EMPTY_MESSAGES_CODE = "EMPTY_UNREAD_MESSAGES"
    }
}
