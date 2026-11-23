package com.umc.presentation.community

/**
 * 커뮤니티 메인 화면에서 발생하는 일회성 UI 이벤트
 *
 * 상세/검색/생성/수정 화면 이동과
 * 토스트 메시지 출력을 처리합니다.
 */
sealed interface CommunityEvent {

    data class NavigateToThreadDetail(
        val threadId: String,
        val isKicked: Boolean = false,
    ) : CommunityEvent

    data object NavigateToSearch : CommunityEvent

    data object NavigateToCreateThread : CommunityEvent

    data class NavigateToEditThread(
        val threadId: String,
    ) : CommunityEvent

    /** [isError]가 false면 성공 안내다. VM이 UI 컴포넌트 타입에 묶이지 않도록 Boolean으로 싣는다. */
    data class ShowToast(
        val message: String,
        val isError: Boolean = true,
    ) : CommunityEvent
}