package com.umc.domain.model.enums

/** 온디바이스 AI 텍스트 기능 종류 */
enum class AiTextFeature {
    SUMMARIZATION,  // 요약 (ML Kit GenAI Summarization)
    GENERATION,     // 자유 프롬프트 생성 (ML Kit GenAI Prompt)
}

/** 온디바이스 AI 기능 가용 상태 */
enum class AiFeatureStatus {
    AVAILABLE,      // 바로 사용 가능
    DOWNLOADABLE,   // 모델 다운로드 후 사용 가능
    DOWNLOADING,    // 모델 다운로드 중
    UNAVAILABLE;    // 이 기기에서 사용 불가

    /** 기능 진입점(메뉴)을 노출해도 되는 상태인지 */
    val isUsable: Boolean
        get() = this != UNAVAILABLE
}
