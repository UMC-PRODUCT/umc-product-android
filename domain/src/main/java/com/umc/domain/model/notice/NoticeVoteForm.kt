package com.umc.domain.model.notice

import com.umc.domain.model.UDomainFormat

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

    // 항목 2개 이상 + 시작/종료 필수 + Instant 기준 시작 < 종료 (소수초 유무가 달라도 동일 순간 구분)
    val canSubmit: Boolean
        get() = validOptions.size >= 2 &&
                UDomainFormat.isDateTimeBefore(startsAt, endsAt)
}
