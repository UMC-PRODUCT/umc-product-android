package com.umc.presentation.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.domain.model.community.CommunityThread
import com.umc.domain.model.community.CommunityThreadCategory
import com.umc.domain.model.community.CommunityThreadRole
import com.umc.domain.usecase.community.GetCommunityThreadsUseCase
import com.umc.presentation.community.model.CommunityCategory
import com.umc.presentation.community.model.CommunityThreadUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val getCommunityThreadsUseCase: GetCommunityThreadsUseCase,
) : ViewModel() {

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

    private fun selectCategory(
        category: CommunityCategory,
    ) {
        _state.update {
            it.copy(
                selectedCategory = category,
            )
        }

        loadThreads()
    }

    private fun loadThreads() {
        viewModelScope.launch {
            val selectedCategory = _state.value.selectedCategory

            _state.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    selectedThread = null,
                    showThreadMenuDialog = false,
                    showLeaveDialog = false,
                )
            }

            getCommunityThreadsUseCase(
                filter = selectedCategory.toApiFilter(),
                query = null,
                offset = 0,
                limit = 20,
            ).onSuccess { page ->
                val uiThreads = buildList {
                    addAll(
                        page.pinnedThreads.map { thread ->
                            thread.toUiModel(
                                forcePinned = true,
                            )
                        }
                    )

                    addAll(
                        page.threads.map { thread ->
                            thread.toUiModel(
                                forcePinned = false,
                            )
                        }
                    )
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        threads = uiThreads,
                        errorMessage = null,
                    )
                }
            }.onFailure {
                _state.update {
                    it.copy(
                        isLoading = false,
                        threads = emptyList(),
                        errorMessage =
                            "네트워크 연결을 확인하고 다시 시도해주세요.",
                    )
                }
            }
        }
    }

    private fun openThreadMenu(
        threadId: String,
    ) {
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

    /*
     * 현재는 API 명세가 아직 없으므로 UI에서만 임시 변경한다.
     * 고정 변경 API가 추가되면 해당 API 호출로 교체해야 한다.
     */
    private fun toggleSelectedThreadPin() {
        val selectedThread = _state.value.selectedThread ?: return

        updateSelectedThread(
            updatedThread = selectedThread.copy(
                isPinned = !selectedThread.isPinned,
            )
        )
    }

    /*
     * 현재는 API 명세가 아직 없으므로 UI에서만 임시 변경한다.
     * 알림 변경 API가 추가되면 해당 API 호출로 교체해야 한다.
     */
    private fun toggleSelectedThreadNotification() {
        val selectedThread = _state.value.selectedThread ?: return

        updateSelectedThread(
            updatedThread = selectedThread.copy(
                isNotificationEnabled =
                    !selectedThread.isNotificationEnabled,
            )
        )
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

    /*
     * 현재는 나가기 API 명세가 없으므로 목록에서만 임시 삭제한다.
     */
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

    private fun sendEvent(
        event: CommunityEvent,
    ) {
        viewModelScope.launch {
            _event.send(event)
        }
    }
}

private fun CommunityCategory.toApiFilter(): String {
    return when (this) {
        CommunityCategory.ALL -> "all"
        CommunityCategory.UNREAD -> "unread"
        CommunityCategory.STUDY -> "STUDY"
        CommunityCategory.QNA -> "QNA"
        CommunityCategory.PROJECT -> "PROJECT"
        CommunityCategory.FREE -> "FREE"
    }
}

private fun CommunityThread.toUiModel(
    forcePinned: Boolean,
): CommunityThreadUiModel {
    return CommunityThreadUiModel(
        id = threadId,
        title = title,
        contentPreview = lastMessage?.preview
            ?.takeIf { preview ->
                preview.isNotBlank()
            }
            ?: description,
        category = category.toUiCategory(),
        icon = icon,
        dayText = lastMessage?.createdAt
            ?.toDayText()
            ?: updatedAt.toDayText(),
        memberCount = memberCount,
        unreadCount = unreadCount,
        maxMembers = maxMembers,
        isPinned = forcePinned || isPinned,
        isNotificationEnabled = !isMuted,
        isMine = myRole == CommunityThreadRole.OWNER,
    )
}

private fun CommunityThreadCategory.toUiCategory(): CommunityCategory {
    return when (this) {
        CommunityThreadCategory.STUDY -> {
            CommunityCategory.STUDY
        }

        CommunityThreadCategory.QNA -> {
            CommunityCategory.QNA
        }

        CommunityThreadCategory.PROJECT -> {
            CommunityCategory.PROJECT
        }

        CommunityThreadCategory.FREE,
        CommunityThreadCategory.UNKNOWN,
            -> {
            CommunityCategory.FREE
        }
    }
}

private fun String.toDayText(): String {
    return runCatching {
        val dateTime = OffsetDateTime.parse(this)
        dateTime.format(
            DateTimeFormatter.ofPattern("MM.dd")
        )
    }.getOrDefault("")
}