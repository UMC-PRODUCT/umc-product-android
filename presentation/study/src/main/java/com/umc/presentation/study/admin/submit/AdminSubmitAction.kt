package com.umc.presentation.study.admin.submit

/**
 * 관리자 스터디 제출 현황 화면에서 발생하는 사용자 액션
 *
 * 주차/그룹 필터 선택, 제출 상세 조회,
 * 피드백 승인/반려 및 베스트 워크북 관리를 처리합니다.
 */
sealed interface AdminSubmitAction {

    /** 조회할 주차 선택 */
    data class SelectWeek(
        val week: Int,
    ) : AdminSubmitAction

    /** 조회할 스터디 그룹 선택 */
    data class SelectGroup(
        val group: AdminSubmitGroupUiModel,
    ) : AdminSubmitAction

    /** 제출 상세 BottomSheet 열기 */
    data class OpenBottomSheet(
        val item: AdminSubmitItemUiModel,
    ) : AdminSubmitAction

    /** 제출 상세 BottomSheet 닫기 */
    data object CloseBottomSheet : AdminSubmitAction

    /** 피드백 내용 변경 */
    data class OnFeedbackChanged(
        val feedback: String,
    ) : AdminSubmitAction

    /** 제출 상세 화면 탭 변경 */
    data class OnReviewTabChanged(
        val index: Int,
    ) : AdminSubmitAction

    /** 제출 승인/반려 요청 */
    data class SubmitReview(
        val pass: Boolean,
    ) : AdminSubmitAction

    /** 기존 제출 상태 변경 */
    data class ChangeStatus(
        val status: String,
    ) : AdminSubmitAction

    /** 변경한 제출 상태 저장 */
    data object CompleteChange : AdminSubmitAction

    /** 승인 확정 */
    data object ConfirmApprove : AdminSubmitAction

    /** 반려 확정 */
    data object ConfirmReject : AdminSubmitAction

    /** 승인/반려 Dialog 닫기 */
    data object DismissDialog : AdminSubmitAction

    /** 주차 선택 BottomSheet 열기 */
    data object OpenWeekBottomSheet : AdminSubmitAction

    /** 그룹 선택 BottomSheet 열기 */
    data object OpenGroupBottomSheet : AdminSubmitAction

    /** 주차 선택 BottomSheet 닫기 */
    data object CloseWeekBottomSheet : AdminSubmitAction

    /** 그룹 선택 BottomSheet 닫기 */
    data object CloseGroupBottomSheet : AdminSubmitAction

    /** 제출 현황 다음 페이지 조회 */
    data object LoadMore : AdminSubmitAction

    /** 베스트 선정 사유 변경 */
    data class OnBestCommentChanged(
        val comment: String,
    ) : AdminSubmitAction

    /** 베스트 워크북 등록 요청 */
    data object RegisterBest : AdminSubmitAction

    /** 베스트 워크북 등록 취소 요청 */
    data object CancelBest : AdminSubmitAction

    /** 베스트 선정 사유 수정 시작 */
    data object EditBest : AdminSubmitAction

    /** 베스트 선정 사유 수정 완료 */
    data object CompleteBest : AdminSubmitAction

    /** 베스트 등록/수정 확정 */
    data object ConfirmBest : AdminSubmitAction

    /** 베스트 선정 취소 확정 */
    data object ConfirmCancelBest : AdminSubmitAction

    /** 베스트 관련 Dialog 닫기 */
    data object DismissBestDialog : AdminSubmitAction
}