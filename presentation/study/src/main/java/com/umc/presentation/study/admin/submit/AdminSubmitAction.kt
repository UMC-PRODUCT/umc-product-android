package com.umc.presentation.study.admin.submit

sealed interface AdminSubmitAction {
    data class SelectWeek(val week: Int) : AdminSubmitAction
    data class SelectGroup(val name: String) : AdminSubmitAction

    // 바텀시트
    data class OpenBottomSheet(val item: AdminSubmitItemUiModel) : AdminSubmitAction
    data object CloseBottomSheet : AdminSubmitAction
    data class OnFeedbackChanged(val feedback: String) : AdminSubmitAction
    data class OnReviewTabChanged(val index: Int) : AdminSubmitAction
    data class SubmitReview(val pass: Boolean) : AdminSubmitAction  // 반려/통과
    data class ChangeStatus(val status: String) : AdminSubmitAction // 현황 변경 (Pass/Fail)
    data object CompleteChange : AdminSubmitAction                  // 완료하기

    data object ConfirmApprove : AdminSubmitAction
    data object ConfirmReject : AdminSubmitAction
    data object DismissDialog : AdminSubmitAction


    data object OpenWeekBottomSheet : AdminSubmitAction
    data object OpenGroupBottomSheet : AdminSubmitAction
    data object CloseWeekBottomSheet : AdminSubmitAction
    data object CloseGroupBottomSheet : AdminSubmitAction

    data class OnBestCommentChanged(val comment: String) : AdminSubmitAction
    data object RegisterBest : AdminSubmitAction        // 등록하기
    data object CancelBest : AdminSubmitAction          // 등록 취소하기
    data object EditBest : AdminSubmitAction            // 수정하기
    data object CompleteBest : AdminSubmitAction        // 완료하기
    data object ConfirmBest : AdminSubmitAction         // 선정 확인
    data object ConfirmCancelBest : AdminSubmitAction   // 취소 확인
    data object DismissBestDialog : AdminSubmitAction   // 다이얼로그 닫기
}