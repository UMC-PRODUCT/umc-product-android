package com.umc.presentation.ui.notice.detail

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.notice.ChallengerReadInfo
import com.umc.domain.model.notice.NoticeDetail
import com.umc.domain.model.notice.NoticeReadStatistics
import com.umc.domain.model.notice.NoticeVoteOption
import com.umc.domain.usecase.notice.GetNoticeDetailUseCase
import com.umc.domain.usecase.notice.GetNoticeReadStatisticsUseCase
import com.umc.domain.usecase.notice.GetNoticeReadStatusUseCase
import com.umc.domain.usecase.notice.SendNoticeReminderUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoticeDetailViewModel @Inject
constructor(
    private val getNoticeDetailUseCase: GetNoticeDetailUseCase,
    private val getNoticeReadStatusUseCase: GetNoticeReadStatusUseCase,
    private val getNoticeReadStatisticsUseCase: GetNoticeReadStatisticsUseCase,
    private val sendNoticeReminderUseCase: SendNoticeReminderUseCase
) : BaseViewModel<NoticeFragmentUiState, NoticeFragmentEvent>(
    NoticeFragmentUiState()
) {

    private var currentNoticeId: Long = 0L
    private var selectedOptionIds: MutableSet<Long> = mutableSetOf()

    fun init(noticeId: Long) {
        currentNoticeId = noticeId
        loadNoticeDetail()
        loadReadStatistics()
    }

    private fun loadNoticeDetail() = viewModelScope.launch {
        if (currentNoticeId == 0L) {
            emitEvent(NoticeFragmentEvent.ShowError("유효하지 않은 공지사항 ID입니다"))
            return@launch
        }

        updateState { copy(isLoading = true) }

        resultResponse(
            response = getNoticeDetailUseCase(currentNoticeId),
            successCallback = { detail ->
                detail.vote?.mySelectedOptionIds?.let { ids ->
                    selectedOptionIds = ids.toMutableSet()
                }
                updateState { copy(detail = detail, isLoading = false) }
            },
            errorCallback = {
                updateState { copy(isLoading = false) }
                emitEvent(NoticeFragmentEvent.ShowError("공지사항을 불러오는데 실패했습니다"))
            }
        )
    }

    private fun loadReadStatistics() = viewModelScope.launch {
        if (currentNoticeId == 0L) return@launch

        resultResponse(
            response = getNoticeReadStatisticsUseCase(currentNoticeId),
            successCallback = { statistics ->
                updateState { copy(readStatistics = statistics) }
            },
            errorCallback = {
                // 통계 로드 실패는 침묵으로 처리
            }
        )
    }

    fun loadReadStatus(status: String = "READ") = viewModelScope.launch {
        if (currentNoticeId == 0L) return@launch

        updateState { copy(isLoadingReadStatus = true) }

        resultResponse(
            response = getNoticeReadStatusUseCase(
                noticeId = currentNoticeId,
                status = status,
                filterType = "ALL"
            ),
            successCallback = { readStatus ->
                updateState {
                    copy(
                        readStatusList = readStatus.content,
                        isLoadingReadStatus = false
                    )
                }
            },
            errorCallback = {
                updateState { copy(isLoadingReadStatus = false) }
                emitEvent(NoticeFragmentEvent.ShowError("확인 현황을 불러오는데 실패했습니다"))
            }
        )
    }

    fun refresh() {
        loadNoticeDetail()
        loadReadStatistics()
    }

    fun onClickBackPressed() {
        emitEvent(NoticeFragmentEvent.MoveBackPressedEvent)
    }

    fun onClickVoteItem(clickedOption: NoticeVoteOption) {
        val vote = uiState.value.detail.vote ?: return

        val isMultiple = vote.allowMultipleChoice

        if (isMultiple) {
            // 복수 선택 가능: 토글
            if (selectedOptionIds.contains(clickedOption.optionId)) {
                selectedOptionIds.remove(clickedOption.optionId)
            } else {
                selectedOptionIds.add(clickedOption.optionId)
            }
        } else {
            // 단일 선택: 하나만 선택
            if (selectedOptionIds.contains(clickedOption.optionId)) {
                selectedOptionIds.clear()
            } else {
                selectedOptionIds.clear()
                selectedOptionIds.add(clickedOption.optionId)
            }
        }

        updateState {
            copy(
                selectedVoteOptionIds = selectedOptionIds.toList()
            )
        }

        // TODO: 투표 제출 API 호출
        submitVote()
    }

    private fun submitVote() = viewModelScope.launch {
        // TODO: 투표 제출 API가 구현되면 추가
        // 현재는 로컬 상태만 업데이트
    }

    fun onClickShowBottomSheet() {
        loadReadStatus("READ")
        emitEvent(NoticeFragmentEvent.ShowBottomSheetEvent)
    }

    fun onClickSendReminder(targetChallengerIds: List<Long>) = viewModelScope.launch {
        if (currentNoticeId == 0L) return@launch

        updateState { copy(isSendingReminder = true) }

        resultResponse(
            response = sendNoticeReminderUseCase(currentNoticeId, targetChallengerIds),
            successCallback = {
                updateState { copy(isSendingReminder = false) }
                emitEvent(NoticeFragmentEvent.ShowSuccess("알림이 발송되었습니다"))
            },
            errorCallback = {
                updateState { copy(isSendingReminder = false) }
                emitEvent(NoticeFragmentEvent.ShowError("알림 발송에 실패했습니다"))
            }
        )
    }

    fun isOptionSelected(optionId: Long): Boolean {
        return selectedOptionIds.contains(optionId)
    }

}


data class NoticeFragmentUiState(
    val detail: NoticeDetail = NoticeDetail(),
    val readStatistics: NoticeReadStatistics? = null,
    val readStatusList: List<ChallengerReadInfo> = emptyList(),
    val selectedVoteOptionIds: List<Long> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingReadStatus: Boolean = false,
    val isSendingReminder: Boolean = false
) : UiState

sealed interface NoticeFragmentEvent : UiEvent {
    object ShowBottomSheetEvent : NoticeFragmentEvent
    object MoveBackPressedEvent : NoticeFragmentEvent
    data class ShowError(val message: String) : NoticeFragmentEvent
    data class ShowSuccess(val message: String) : NoticeFragmentEvent
}