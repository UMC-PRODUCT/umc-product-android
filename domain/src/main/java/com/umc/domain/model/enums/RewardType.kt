package com.umc.domain.model.enums

enum class RewardType(val label: String, val score: Int) {
    //상점
    BLOG_CHALLENGE("블로그 챌린지", 3),
    BEST_WORKBOOK("베스트 워크북 선정", 2),
    BEST_WORKBOOK_V2("베스트 워크북 V2", 2),
    UMC_EVENT_REVIEW("행사 리뷰어", 1),
    PEER_REVIEW_SUBMISSION("PeerReview 작성", 1),

    //벌점
    NO_WORKBOOK_MISSION("과제 미수행", -4),
    STUDY_LATE("스터디 무단 지각", -2),
    STUDY_ABSENT("스터디 무단 불참", -4),
    EVENT_LATE("행사 무단 지각", -2),
    EVENT_EARLY_LEAVE("행사 중도 퇴실", -2),
    EVENT_LATE_CANCEL("행사 기간 외 취소", -4),
    EVENT_NO_SHOW("노쇼(무단 결석)", -10),

    PART_LEAD_FEEDBACK_LATE("기간 외 피드백", -4),

    SCHOOL_CORE_MEETING_ABSENT("회의 무단 불참", -4),
    SCHOOL_CORE_TASK_NOT_COMPLETED("업무 무단 불이행", -4),

    WARNING("경고", 0),
    OUT("제명", 0),
    CUSTOM("직접 입력", 0);

    companion object {
        //상점 항목만 필터링
        fun getBonusList() = entries.filter { it.score > 0 }

        //벌점 항목만 필터링
        fun getPenaltyList() = entries.filter { it.score < 0 }

        //이름(String)으로 Enum을 찾아주는 안전한 함수
        fun fromName(name: String): RewardType? =
            runCatching { valueOf(name) }.getOrNull()
    }

}
