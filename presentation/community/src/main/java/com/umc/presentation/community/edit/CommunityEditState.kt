package com.umc.presentation.community.edit

import com.umc.presentation.community.model.CommunityAiState
import com.umc.presentation.community.model.CommunityCategory
import com.umc.presentation.community.model.CommunityChallengerUiModel

/**
 * 스레드 수정 화면에서 사용하는 UI 상태
 */
data class CommunityEditState(

    /** 현재 수정 중인 스레드 ID */
    val threadId: String = "",

    /** 스레드 제목 */
    val title: String = "",

    /** 스레드 특징 및 상세 내용 */
    val description: String = "",

    /** 현재 스레드에 참여 중인 챌린저 목록 */
    val selectedChallengers: List<CommunityChallengerUiModel> = emptyList(),

    /** 현재 참여 중인 챌린저 수 */
    val currentChallengerCount: Int = 0,

    /** 스레드 최대 참여 가능 인원 */
    val maxChallengerCount: Int = 8,

    /** 현재 AI 카테고리 분류 상태 */
    val aiState: CommunityAiState = CommunityAiState.SUCCESS,

    /** 현재 스레드의 분류 카테고리 */
    val classifiedCategory: CommunityCategory? = CommunityCategory.STUDY,

    /** 현재 스레드에 설정된 아이콘 */
    val selectedIcon: String = "BOOK",

    /** 스레드 상세 정보 로딩 여부 */
    val isLoading: Boolean = false,

    /** 스레드 수정 API 요청 진행 여부 */
    val isSaving: Boolean = false,

    /** 스레드 삭제 API 요청 진행 여부 */
    val isDeleting: Boolean = false,

    /** 조회 또는 수정 실패 시 사용할 에러 메시지 */
    val errorMessage: String? = null,

    /** 챌린저 관리 BottomSheet 표시 여부 */
    val showChallengerBottomSheet: Boolean = false,

    /** 스레드 삭제 확인 Dialog 표시 여부 */
    val showDeleteDialog: Boolean = false,
) {

    /** 현재 참여 인원 / 최대 참여 가능 인원 */
    val selectedChallengerCountText: String
        get() = "$currentChallengerCount / $maxChallengerCount"

    /**
     * 수정 완료 버튼 활성화 여부
     *
     * 제목과 특징이 입력되어 있고 카테고리가 존재하며,
     * 현재 수정 요청 중이 아닐 때 활성화됩니다.
     */
    val isSaveEnabled: Boolean
        get() = title.isNotBlank() &&
                description.isNotBlank() &&
                classifiedCategory != null &&
                !isSaving

    /**
     * AI 카테고리 재분류 가능 여부
     *
     * 제목과 특징이 입력되어 있고
     * 현재 AI 분류 중이 아닐 때 활성화됩니다.
     */
    val canRequestClassification: Boolean
        get() = title.isNotBlank() &&
                description.isNotBlank() &&
                aiState != CommunityAiState.LOADING
}