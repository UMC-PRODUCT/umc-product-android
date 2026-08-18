package com.umc.presentation.community.model

/**
 * 커뮤니티 스레드의 AI 카테고리 분류 상태
 *
 * 분류 안내, 로딩, 성공, 재분류 필요, 실패 상태를 관리합니다.
 */
enum class CommunityAiState {
    GUIDE,
    LOADING,
    SUCCESS,
    NEEDS_RECLASSIFICATION,
    FAILED,
}