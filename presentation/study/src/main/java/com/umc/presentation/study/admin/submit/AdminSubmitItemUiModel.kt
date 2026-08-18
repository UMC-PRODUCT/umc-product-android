package com.umc.presentation.study.admin.submit

/**
 * 관리자 제출 현황 목록에서 사용하는 챌린저별 UI 모델
 *
 * 챌린저의 기본 정보와 워크북 제출 상태,
 * BEST 등록 여부 및 제출 정보를 관리합니다.
 */
data class AdminSubmitItemUiModel(
    val id: Long,
    val challengerWorkbookId: Long? = null,
    val memberId: Long,
    val studyGroupId: Long,
    val weeklyCurriculumId: Long,
    val weeklyBestWorkbookId: Long? = null,

    // 챌린저 기본 정보
    val name: String,
    val nickname: String,
    val partLabel: String,
    val weekText: String,
    val studyTitle: String = "",
    val schoolName: String,

    // 사용자 프로필 이미지 URL
    val profileImageUrl: String? = null,

    // 워크북 제출 상태 및 제출 정보
    val status: String,
    val submitUrl: String = "",
    val bestComment: String = "",
    val isBestRegistered: Boolean = false,
) {

    /** BEST 워크북 여부 */
    val isBest: Boolean
        get() = status == "BEST"

    /**
     * 화면에 표시할 채점 상태
     *
     * BEST는 PASS 처리된 워크북이므로
     * 화면에서는 PASS 상태와 함께 표시합니다.
     */
    val markStatus: String?
        get() = when (status) {
            "PASS" -> "PASS"
            "FAIL" -> "FAIL"
            "BEST" -> "PASS"
            else -> null
        }
}