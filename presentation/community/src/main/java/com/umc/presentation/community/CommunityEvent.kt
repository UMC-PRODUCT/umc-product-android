package com.umc.presentation.community

/**
 * 커뮤니티 메인 화면에서 발생하는 일회성 UI 이벤트
 *
 * 상세/검색/생성/수정 화면 이동과
 * Toast 메시지 출력을 처리합니다.
 */
sealed interface CommunityEvent {

    data class NavigateToThreadDetail(
        val threadId: String,
    ) : CommunityEvent

    data object NavigateToSearch : CommunityEvent

    data object NavigateToCreateThread : CommunityEvent

    data class NavigateToEditThread(
        val threadId: String,
    ) : CommunityEvent

    data class ShowToast(
        val message: String,
    ) : CommunityEvent
}