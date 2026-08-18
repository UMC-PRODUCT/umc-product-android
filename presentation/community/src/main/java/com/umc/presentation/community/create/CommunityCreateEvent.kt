package com.umc.presentation.community.create

/**
 * 스레드 생성 화면에서 발생하는 일회성 UI 이벤트
 *
 * 화면 이동, 생성 성공, Toast 등의 이벤트를
 * Route에서 처리하기 위해 사용합니다.
 */
sealed interface CommunityCreateEvent {

    /** 이전 화면으로 이동 */
    data object NavigateBack : CommunityCreateEvent

    /**
     * 스레드 생성 성공
     *
     * 생성된 threadId를 전달하여
     * 생성 이후 화면 이동에 사용합니다.
     */
    data class CreateSuccess(
        val threadId: String,
    ) : CommunityCreateEvent

    /** 스레드 아이콘 선택 화면으로 이동 */
    data object NavigateToEmojiPicker : CommunityCreateEvent

    /** 사용자에게 Toast 메시지 표시 */
    data class ShowToast(
        val message: String,
    ) : CommunityCreateEvent
}