package com.umc.presentation.study.admin.group.create

import com.umc.component.base.UiState

/**
 * 스터디 그룹 생성 화면의 UI 상태
 *
 * 사용자가 그룹 생성 과정에서 입력하거나 선택한 값과
 * 각 바텀시트의 표시 여부, API 요청 상태를 관리합니다.
 *
 * 주요 상태
 * - 기수 ID
 * - 그룹 이름
 * - 선택한 파트
 * - 선택한 담당 파트장
 * - 선택한 스터디원
 * - 각 선택 바텀시트 표시 여부
 * - 그룹 생성 API 로딩 여부
 */
data class AdminStudyGroupCreateState(

    /**
     * 스터디 그룹을 생성할 기수 ID
     *
     * 그룹 생성 API 요청 시 gisuId로 전달합니다.
     */
    val gisuId: Long? = null,

    /**
     * 사용자가 입력한 스터디 그룹 이름
     */
    val groupName: String = "",

    /**
     * 현재 선택한 파트
     *
     * 선택되지 않은 초기 상태에서는 null입니다.
     */
    val selectedPart: AdminStudyGroupCreatePartUiModel? = null,

    /**
     * 현재 선택한 담당 파트장 목록
     *
     * 그룹 생성 API 요청 시 mentorIds로 변환하여 전달합니다.
     */
    val selectedPartLeaders:
    List<AdminStudyGroupCreateMemberUiModel> = emptyList(),

    /**
     * 현재 선택한 스터디원 목록
     *
     * 그룹 생성 API 요청 시 memberIds로 변환하여 전달합니다.
     */
    val selectedMembers:
    List<AdminStudyGroupCreateMemberUiModel> = emptyList(),

    /**
     * 파트 선택 바텀시트 표시 여부
     */
    val showPartBottomSheet: Boolean = false,

    /**
     * 담당 파트장 선택 바텀시트 표시 여부
     */
    val showPartLeaderBottomSheet: Boolean = false,

    /**
     * 스터디원 선택 바텀시트 표시 여부
     */
    val showMemberBottomSheet: Boolean = false,

    /**
     * 스터디 그룹 생성 API 요청 진행 여부
     *
     * 요청 중에는 중복 등록을 방지하기 위해
     * 등록 버튼을 비활성화합니다.
     */
    val isLoading: Boolean = false,
) : UiState {

    /**
     * 선택된 스터디원을 화면에 표시하기 위한 요약 문자열
     *
     * 선택 인원에 따라 다음과 같이 표시합니다.
     *
     * 0명
     * -> ""
     *
     * 1명
     * -> "홍길동"
     *
     * 2명 이상
     * -> "홍길동 외 2명"
     */
    val memberSummary: String
        get() = when {
            selectedMembers.isEmpty() -> ""

            selectedMembers.size == 1 ->
                selectedMembers.first().name

            else ->
                "${selectedMembers.first().name} 외 ${selectedMembers.size - 1}명"
        }

    /**
     * 선택된 담당 파트장을 화면에 표시하기 위한 요약 문자열
     *
     * 여러 명이 선택된 경우 이름을 쉼표로 연결합니다.
     *
     * 예)
     * 홍길동, 김철수
     */
    val partLeaderSummary: String
        get() = selectedPartLeaders.joinToString(", ") {
            it.name
        }

    /**
     * 그룹 등록 버튼 활성화 여부
     *
     * 다음 조건을 모두 만족해야 등록할 수 있습니다.
     *
     * 1. 그룹 이름이 비어있지 않음
     * 2. 파트가 선택되어 있음
     * 3. 담당 파트장이 한 명 이상 선택되어 있음
     * 4. 스터디원이 한 명 이상 선택되어 있음
     * 5. 현재 API 요청 중이 아님
     */
    val isRegisterEnabled: Boolean
        get() =
            groupName.isNotBlank() &&
                    selectedPart != null &&
                    selectedPartLeaders.isNotEmpty() &&
                    selectedMembers.isNotEmpty() &&
                    !isLoading
}