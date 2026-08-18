package com.umc.presentation.community

import com.umc.presentation.community.model.CommunityCategory

/**
 * 커뮤니티 메인 화면에서 발생하는 사용자 액션
 *
 * 카테고리 선택, 스레드 이동, 검색/생성,
 * 스레드 메뉴 및 고정/알림/나가기 동작을 관리합니다.
 */
sealed interface CommunityAction {

    data class OnCategorySelected(
        val category: CommunityCategory,
    ) : CommunityAction

    data class OnThreadClick(
        val threadId: String,
    ) : CommunityAction

    data object OnFilterClick : CommunityAction

    data object OnSearchClick : CommunityAction

    data object OnCreateThreadClick : CommunityAction

    data object OnRetryClick : CommunityAction

    data class OnThreadLongClick(
        val threadId: String,
    ) : CommunityAction

    data object OnDismissThreadMenu : CommunityAction

    data object OnTogglePinClick : CommunityAction

    data object OnToggleNotificationClick : CommunityAction

    data object OnEditThreadClick : CommunityAction

    data object OnLeaveThreadClick : CommunityAction

    data object OnDismissLeaveDialog : CommunityAction

    data object OnConfirmLeaveClick : CommunityAction
}