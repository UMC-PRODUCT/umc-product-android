package com.umc.presentation.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.presentation.community.model.CommunityCategory
import com.umc.presentation.community.model.CommunityThreadUiModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CommunityViewModel : ViewModel() {

    private val _state = MutableStateFlow(CommunityState())
    val state: StateFlow<CommunityState> = _state.asStateFlow()

    private val _event = Channel<CommunityEvent>()
    val event = _event.receiveAsFlow()

    init {
        loadThreads()
    }

    fun onAction(action: CommunityAction) {
        when (action) {
            CommunityAction.OnFilterClick -> Unit

            CommunityAction.OnSearchClick -> {
                sendEvent(CommunityEvent.NavigateToSearch)
            }

            CommunityAction.OnCreateThreadClick -> {
                sendEvent(CommunityEvent.NavigateToCreateThread)
            }

            CommunityAction.OnRetryClick -> {
                loadThreads()
            }

            is CommunityAction.OnCategorySelected -> {
                selectCategory(action.category)
            }

            is CommunityAction.OnThreadClick -> {
                sendEvent(
                    CommunityEvent.NavigateToThreadDetail(
                        threadId = action.threadId,
                    )
                )
            }

            is CommunityAction.OnThreadLongClick -> {
                openThreadMenu(action.threadId)
            }

            CommunityAction.OnDismissThreadMenu -> {
                dismissThreadMenu()
            }

            CommunityAction.OnTogglePinClick -> {
                toggleSelectedThreadPin()
            }

            CommunityAction.OnToggleNotificationClick -> {
                toggleSelectedThreadNotification()
            }

            CommunityAction.OnEditThreadClick -> {
                navigateToEditSelectedThread()
            }

            CommunityAction.OnLeaveThreadClick -> {
                openLeaveDialog()
            }

            CommunityAction.OnDismissLeaveDialog -> {
                dismissLeaveDialog()
            }

            CommunityAction.OnConfirmLeaveClick -> {
                leaveSelectedThread()
            }
        }
    }

    private fun selectCategory(category: CommunityCategory) {
        _state.update {
            it.copy(
                selectedCategory = category,
            )
        }
    }

    private fun openThreadMenu(threadId: Long) {
        val selectedThread = _state.value.threads.find { thread ->
            thread.id == threadId
        } ?: return

        _state.update {
            it.copy(
                selectedThread = selectedThread,
                showThreadMenuDialog = true,
                showLeaveDialog = false,
            )
        }
    }

    private fun dismissThreadMenu() {
        _state.update {
            it.copy(
                showThreadMenuDialog = false,
            )
        }
    }

    private fun toggleSelectedThreadPin() {
        val selectedThread = _state.value.selectedThread ?: return

        val updatedThread = selectedThread.copy(
            isPinned = !selectedThread.isPinned,
        )

        updateSelectedThread(updatedThread)
    }

    private fun toggleSelectedThreadNotification() {
        val selectedThread = _state.value.selectedThread ?: return

        val updatedThread = selectedThread.copy(
            isNotificationEnabled =
                !selectedThread.isNotificationEnabled,
        )

        updateSelectedThread(updatedThread)
    }

    private fun updateSelectedThread(
        updatedThread: CommunityThreadUiModel,
    ) {
        _state.update { currentState ->
            currentState.copy(
                threads = currentState.threads.map { thread ->
                    if (thread.id == updatedThread.id) {
                        updatedThread
                    } else {
                        thread
                    }
                },
                selectedThread = updatedThread,
                showThreadMenuDialog = false,
            )
        }
    }

    private fun navigateToEditSelectedThread() {
        val selectedThread = _state.value.selectedThread ?: return

        // 내 스레드가 아닌 경우 편집 화면으로 이동하지 않음
        if (!selectedThread.isMine) return

        _state.update {
            it.copy(
                showThreadMenuDialog = false,
            )
        }

        sendEvent(
            CommunityEvent.NavigateToEditThread(
                threadId = selectedThread.id,
            )
        )
    }

    private fun openLeaveDialog() {
        _state.update {
            it.copy(
                showThreadMenuDialog = false,
                showLeaveDialog = true,
            )
        }
    }

    private fun dismissLeaveDialog() {
        _state.update {
            it.copy(
                showLeaveDialog = false,
            )
        }
    }

    private fun leaveSelectedThread() {
        val selectedThread = _state.value.selectedThread ?: return

        _state.update { currentState ->
            currentState.copy(
                threads = currentState.threads.filterNot { thread ->
                    thread.id == selectedThread.id
                },
                selectedThread = null,
                showThreadMenuDialog = false,
                showLeaveDialog = false,
            )
        }
    }

    private fun loadThreads() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    selectedThread = null,
                    showThreadMenuDialog = false,
                    showLeaveDialog = false,
                )
            }

            delay(1200L)

            when (PREVIEW_STATE) {
                PreviewState.SUCCESS -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            threads = dummyThreads,
                            errorMessage = null,
                        )
                    }
                }

                PreviewState.EMPTY -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            threads = emptyList(),
                            errorMessage = null,
                        )
                    }
                }

                PreviewState.ERROR -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            threads = emptyList(),
                            errorMessage =
                                "네트워크 연결을 확인하고 다시 시도해주세요.",
                        )
                    }
                }

                PreviewState.LOADING -> {
                    // 스켈레톤 화면을 계속 유지
                }
            }
        }
    }

    private fun sendEvent(event: CommunityEvent) {
        viewModelScope.launch {
            _event.send(event)
        }
    }

    private enum class PreviewState {
        SUCCESS,
        EMPTY,
        ERROR,
        LOADING,
    }

    companion object {
        private val PREVIEW_STATE = PreviewState.SUCCESS
    }

    private val dummyThreads = listOf(
        CommunityThreadUiModel(
            id = 1L,
            title = "iOS 3주차 과제 인증방",
            contentPreview =
                "다른 과제 인증 완료했나요? 오늘 자정까지...",
            category = CommunityCategory.STUDY,
            dayText = "화요일",
            commentCount = 3,
            isPinned = true,
            isNotificationEnabled = false,
            isMine = true,
            isRead = false,
        ),
        CommunityThreadUiModel(
            id = 2L,
            title = "정기 모임 일정 안내",
            contentPreview =
                "다른 과제 인증 완료했나요? 오늘 자정까지...",
            category = CommunityCategory.PART_NOTICE,
            dayText = "화요일",
            commentCount = 3,
            isPinned = true,
            isNotificationEnabled = true,
            isMine = false,
            isRead = false,
        ),
        CommunityThreadUiModel(
            id = 3L,
            title = "OT 장소 변경 안내",
            contentPreview =
                "다른 과제 인증 완료했나요? 오늘 자정까지...",
            category = CommunityCategory.PART_NOTICE,
            dayText = "화요일",
            commentCount = 3,
            isPinned = false,
            isNotificationEnabled = true,
            isMine = true,
            isRead = true,
        ),
        CommunityThreadUiModel(
            id = 4L,
            title = "리액트 상태관리 질문 있어요",
            contentPreview =
                "다른 과제 인증 완료했나요? 오늘 자정까지...",
            category = CommunityCategory.QUESTION,
            dayText = "화요일",
            commentCount = 3,
            isPinned = false,
            isNotificationEnabled = false,
            isMine = false,
            isRead = false,
        ),
        CommunityThreadUiModel(
            id = 5L,
            title = "이번 주 회식 어때요?",
            contentPreview =
                "다른 과제 인증 완료했나요? 오늘 자정까지...",
            category = CommunityCategory.FREE,
            dayText = "화요일",
            commentCount = 3,
            isPinned = false,
            isNotificationEnabled = true,
            isMine = true,
            isRead = false,
        ),
    )
}