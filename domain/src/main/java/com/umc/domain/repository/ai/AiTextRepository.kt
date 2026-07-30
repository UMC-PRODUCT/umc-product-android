package com.umc.domain.repository.ai

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.enums.AiFeatureStatus
import com.umc.domain.model.enums.AiTextFeature

/**
 * 온디바이스 AI 텍스트 처리 저장소.
 * 다른 파트에서도 요약/생성이 필요하면 이 인터페이스 위에 UseCase만 추가해서 확장한다.
 * 추론 호출은 필요 시 모델 다운로드까지 내부에서 처리한다
 */
interface AiTextRepository {

    /** 기능 가용 상태 확인 (메뉴 노출 게이트용) */
    suspend fun checkStatus(feature: AiTextFeature): AiFeatureStatus

    /** 텍스트 요약. 결과는 불릿 목록 형태의 문자열 */
    suspend fun summarize(text: String): ApiState<String>

    /** 자유 프롬프트로 텍스트 생성 */
    suspend fun generate(prompt: String): ApiState<String>
}
