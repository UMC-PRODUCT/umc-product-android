package com.umc.presentation.community.edit

/**
 * 스레드 수정 화면에서 발생하는 사용자 액션
 *
 * 입력 변경, 버튼 클릭, 챌린저 관리 등의 동작을
 * ViewModel로 전달하기 위해 사용합니다.
 */
sealed interface CommunityEditAction {

    /** 뒤로가기 버튼 클릭 */
    data object OnBackClick : CommunityEditAction

    /** 스레드 수정 완료 버튼 클릭 */
    data object OnSaveClick : CommunityEditAction

    /** 챌린저 명단 카드 클릭 */
    data object OnChallengerCardClick : CommunityEditAction

    /** 챌린저 관리 BottomSheet 닫기 */
    data object OnDismissChallengerBottomSheet : CommunityEditAction

    /** 스레드 제목 변경 */
    data class OnTitleChanged(
        val title: String,
    ) : CommunityEditAction

    /** 스레드 특징 변경 */
    data class OnDescriptionChanged(
        val description: String,
    ) : CommunityEditAction

    /** AI 카테고리 재분류 요청 */
    data object OnRetryClassificationClick : CommunityEditAction

    /** 스레드 아이콘 변경 */
    data object OnChangeEmojiClick : CommunityEditAction

    /** 스레드 삭제 버튼 클릭 */
    data object OnDeleteThreadClick : CommunityEditAction

    /** 스레드 삭제 확인 Dialog 닫기 */
    data object OnDismissDeleteDialog : CommunityEditAction

    /** 스레드 삭제 확정 */
    data object OnConfirmDeleteClick : CommunityEditAction

    /** 챌린저 추가/삭제 완료 */
    data object OnMemberInviteSuccess : CommunityEditAction
}