package com.umc.presentation.community.create

import com.umc.presentation.community.DEFAULT_COMMUNITY_MAX_MEMBER_COUNT
import com.umc.presentation.community.model.CommunityAiState
import com.umc.presentation.community.model.CommunityCategory
import com.umc.presentation.community.model.CommunityChallengerUiModel

/**
 * 스레드 생성 화면에서 사용하는 UI 상태
 */
data class CommunityCreateState(

    /** 입력된 스레드 제목 */
    val title: String = "",

    /** 입력된 스레드 특징 및 상세 내용 */
    val description: String = "",

    /** 스레드에 추가할 챌린저 목록 */
    val selectedChallengers: List<CommunityChallengerUiModel> = emptyList(),

    /** 선택 가능한 최대 챌린저 수 */
    val maxChallengerCount: Int = DEFAULT_COMMUNITY_MAX_MEMBER_COUNT,

    /** 현재 AI 카테고리 분류 상태 */
    val aiState: CommunityAiState = CommunityAiState.GUIDE,

    /** AI를 통해 최종 분류된 카테고리 */
    val classifiedCategory: CommunityCategory? = null,

    /** 챌린저 선택 BottomSheet 표시 여부 */
    val showChallengerBottomSheet: Boolean = false,

    /** 스레드에 사용할 아이콘 */
    val selectedIcon: String = "📚",

    /** 스레드 생성 API 호출 진행 여부 */
    val isSubmitting: Boolean = false,
    /** 내 memberId. 참여자에 본인이 들어갔는지 검사할 때 쓴다 */
    val myMemberId: Long = 0L,
) {

    /** 현재 선택된 챌린저 수 / 최대 선택 가능 수 */
    val selectedChallengerCountText: String
        get() = "${selectedChallengers.size} / $maxChallengerCount"

    /** 제목 입력 여부 */
    val isTitleValid: Boolean
        get() = title.isNotBlank()

    /** 스레드 특징 입력 여부 */
    val isDescriptionValid: Boolean
        get() = description.isNotBlank()

    /**
     * 완료 버튼 활성화 여부
     *
     * 제목, 특징, 챌린저, 카테고리가 모두 존재하고
     * 현재 생성 요청 중이 아닐 때 활성화됩니다.
     */
    val isCompleteEnabled: Boolean
        get() = isTitleValid &&
                isDescriptionValid &&
                selectedChallengers.isNotEmpty() &&
                classifiedCategory != null &&
                !isSubmitting

    /**
     * AI 카테고리 분류 요청 가능 여부
     *
     * 제목과 특징이 모두 입력되어 있고
     * 현재 AI 분석 중이 아닐 때 요청할 수 있습니다.
     */
    val canRequestClassification: Boolean
        get() = title.isNotBlank() &&
                description.isNotBlank() &&
                aiState != CommunityAiState.LOADING
}