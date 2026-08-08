package com.umc.domain.model.notice

/**
 * 공지 작성 화면에 첨부된 투표. 날짜는 UTC ISO8601 문자열
 */
data class NoticeVoteForm(
    val title: String = "",
    val options: List<String> = listOf("", ""),
    val isAnonymous: Boolean = false,
    val allowMultipleChoice: Boolean = false,
    val startsAt: String? = null,
    val endsAt: String? = null,
) {
    val validOptions: List<String>
        get() = options.map { it.trim() }.filter { it.isNotBlank() }

    // 항목 2개 이상 + 시작/종료 필수 + 시작 < 종료 (동일 포맷 UTC 문자열이라 사전순 비교 가능)
    val canSubmit: Boolean
        get() = validOptions.size >= 2 &&
                startsAt != null && endsAt != null && startsAt < endsAt
}
