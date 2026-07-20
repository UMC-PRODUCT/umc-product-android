package com.umc.domain.model.enums

/** 공지 투표 상태 (서버 스펙: NOT_STARTED / OPEN / CLOSED) */
enum class NoticeVoteStatus {
    NOT_STARTED, OPEN, CLOSED;

    companion object {
        /** 알 수 없는 값은 OPEN으로 처리 (투표 시도 시 서버가 최종 판단) */
        fun from(value: String): NoticeVoteStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: OPEN
        }
    }
}
