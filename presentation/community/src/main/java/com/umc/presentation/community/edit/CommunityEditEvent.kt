package com.umc.presentation.community.edit

/**
 * 스레드 수정 화면에서 발생하는 일회성 UI 이벤트
 *
 * 화면 이동, 수정/삭제 성공, Toast 등의 이벤트를
 * Route에서 처리하기 위해 사용합니다.
 */
sealed interface CommunityEditEvent {

    /** 이전 화면으로 이동 */
    data object NavigateBack : CommunityEditEvent

    /** 스레드 수정 성공 */
    data object SaveSuccess : CommunityEditEvent

    /** 스레드 삭제 성공 */
    data object DeleteSuccess : CommunityEditEvent

    /** 아이콘 선택 화면으로 이동 */
    data object NavigateToEmojiPicker : CommunityEditEvent

    /** 사용자에게 Toast 메시지 표시 */
    data class ShowToast(
        val message: String,
    ) : CommunityEditEvent

    /** 챌린저 추가/삭제 성공 */
    data object MemberInviteSuccess : CommunityEditEvent
}