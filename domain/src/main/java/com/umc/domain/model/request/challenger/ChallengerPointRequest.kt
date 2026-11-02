package com.umc.domain.model.request.challenger

import com.umc.domain.model.enums.PointType

data class ChallengerPointRequest (
    val pointType: PointType, // 신규 부여에는 BEST_WORKBOOK 대신 BEST_WORKBOOK_V2 를 쓴다
    val description: String, // 사유. 서버는 공백 불가 · 200자 이하
    val pointValue: Int? = null, // CUSTOM 만 보낸다(0 불가). 고정 유형은 null 로 두어 서버 기본 배점을 쓴다
)
