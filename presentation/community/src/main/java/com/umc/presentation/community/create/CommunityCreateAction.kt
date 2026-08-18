package com.umc.presentation.community.create

import com.umc.presentation.community.model.CommunityChallengerUiModel

/**
 * 스레드 생성 화면에서 발생하는 사용자 액션
 *
 * 화면에서 발생한 클릭, 입력, 챌린저 선택 등의 동작을
 * ViewModel로 전달하기 위해 사용합니다.
 */
sealed interface CommunityCreateAction {

    /** 뒤로가기 버튼 클릭 */
    data object OnBackClick : CommunityCreateAction

    /** 스레드 생성 완료 버튼 클릭 */
    data object OnCompleteClick : CommunityCreateAction

    /** 챌린저 선택 카드 클릭 */
    data object OnChallengerCardClick : CommunityCreateAction

    /** 챌린저 선택 BottomSheet 닫기 */
    data object OnDismissChallengerBottomSheet : CommunityCreateAction

    /** 스레드 제목 변경 */
    data class OnTitleChanged(
        val title: String,
    ) : CommunityCreateAction

    /** 스레드 특징(설명) 변경 */
    data class OnDescriptionChanged(
        val description: String,
    ) : CommunityCreateAction

    /** BottomSheet에서 선택한 챌린저 목록 반영 */
    data class OnChallengersSelected(
        val challengers: List<CommunityChallengerUiModel>,
    ) : CommunityCreateAction

    /** AI 카테고리 분류 요청 */
    data object OnRequestClassificationClick : CommunityCreateAction

    /** AI 카테고리 재분류 요청 */
    data object OnRetryClassificationClick : CommunityCreateAction

    /** 스레드 아이콘 변경 버튼 클릭 */
    data object OnChangeEmojiClick : CommunityCreateAction
}