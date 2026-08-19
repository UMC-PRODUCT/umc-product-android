package com.umc.presentation.community.search

/**
 * 커뮤니티 검색 화면에서 발생하는 일회성 UI 이벤트
 *
 * 이전 화면 이동 및 검색한 스레드 상세 화면 이동을 처리합니다.
 */
sealed interface CommunitySearchEvent {

    data object NavigateBack : CommunitySearchEvent

    data class NavigateToThreadDetail(
        val threadId: String,
    ) : CommunitySearchEvent
}