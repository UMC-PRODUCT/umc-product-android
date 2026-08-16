package com.umc.presentation.community

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.domain.model.community.CommunityThread
import com.umc.domain.model.community.CommunityThreadCategory
import com.umc.domain.model.community.CommunityThreadRole
import com.umc.domain.usecase.community.GetCommunityThreadsUseCase
import com.umc.presentation.community.model.CommunityCategory
import com.umc.presentation.community.model.CommunityThreadUiModel
import com.umc.domain.usecase.community.LeaveCommunityThreadUseCase
import com.umc.domain.usecase.community.MuteCommunityThreadUseCase
import com.umc.domain.usecase.community.PinCommunityThreadUseCase
import com.umc.domain.usecase.community.UnmuteCommunityThreadUseCase
import com.umc.domain.usecase.community.UnpinCommunityThreadUseCase
import com.umc.domain.model.community.CommunityThreadDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val getCommunityThreadsUseCase: GetCommunityThreadsUseCase,
    private val pinCommunityThreadUseCase: PinCommunityThreadUseCase,
    private val unpinCommunityThreadUseCase: UnpinCommunityThreadUseCase,
    private val muteCommunityThreadUseCase: MuteCommunityThreadUseCase,
    private val unmuteCommunityThreadUseCase: UnmuteCommunityThreadUseCase,
    private val leaveCommunityThreadUseCase: LeaveCommunityThreadUseCase,
) : ViewModel() {

    private var threadPollingJob: Job? = null
    private val _state = MutableStateFlow(CommunityState())
    val state: StateFlow<CommunityState> = _state.asStateFlow()

    private val _event = Channel<CommunityEvent>()
    val event = _event.receiveAsFlow()

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
    }

    fun loadThreads() {
        viewModelScope.launch {
            fetchThreads(showLoading = true)
        }
    }

    fun startThreadPolling() {
        if (threadPollingJob?.isActive == true) return

        threadPollingJob = viewModelScope.launch {
            fetchThreads(showLoading = _state.value.threads.isEmpty())

            while (true) {
                delay(THREAD_POLLING_INTERVAL_MS)
                fetchThreads(showLoading = false)
            }
        }
    }

    fun stopThreadPolling() {
        threadPollingJob?.cancel()
        threadPollingJob = null
    }

    private suspend fun fetchThreads(showLoading: Boolean) {
        if (showLoading) {
            _state.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    selectedThread = null,
                    showThreadMenuDialog = false,
                    showLeaveDialog = false,
                )
            }
        }

        getCommunityThreadsUseCase(
            filter = "all",
            query = null,
            offset = 0,
            limit = 20,
        ).onSuccess { page ->
            val uiThreads = buildList {
                addAll(
                    page.pinnedThreads.map { thread ->
                        thread.toUiModel(forcePinned = true)
                    }
                )
                addAll(
                    page.threads.map { thread ->
                        thread.toUiModel(forcePinned = false)
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
        }.onFailure { throwable ->
            Log.e(
                "COMMUNITY_LIST",
                "목록 조회 실패",
                throwable,
            )

            // Keep the last successful list visible when a background refresh fails.
            if (showLoading) {
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

    private fun toggleSelectedThreadPin() {
        val selectedThread = _state.value.selectedThread ?: return

        viewModelScope.launch {
            val result = if (selectedThread.isPinned) {
                unpinCommunityThreadUseCase(
                    threadId = selectedThread.id,
                )
            } else {
                pinCommunityThreadUseCase(
                    threadId = selectedThread.id,
                )
            }

            result.onSuccess { updatedThread ->
                val updatedUiModel = updatedThread.toUiModel()

                _state.update { currentState ->
                    currentState.copy(
                        threads = currentState.threads.map { thread ->
                            if (thread.id == updatedUiModel.id) {
                                updatedUiModel
                            } else {
                                thread
                            }
                        },
                        selectedThread = null,
                        showThreadMenuDialog = false,
                    )
                }

                // 고정 영역과 일반 영역 순서를 서버 기준으로 다시 맞춤
                loadThreads()
            }.onFailure { throwable ->
                dismissThreadMenu()

                sendEvent(
                    CommunityEvent.ShowToast(
                        message = throwable.message
                            ?: "스레드 고정 상태를 변경하지 못했어요.",
                    )
                )
            }
        }
    }

    private fun toggleSelectedThreadNotification() {
        val selectedThread = _state.value.selectedThread ?: return

        viewModelScope.launch {
            val result = if (selectedThread.isNotificationEnabled) {
                // 현재 알림이 켜져 있으면 mute 호출
                muteCommunityThreadUseCase(
                    threadId = selectedThread.id,
                )
            } else {
                // 현재 알림이 꺼져 있으면 unmute 호출
                unmuteCommunityThreadUseCase(
                    threadId = selectedThread.id,
                )
            }

            result.onSuccess { updatedThread ->
                val updatedUiModel = updatedThread.toUiModel()

                updateSelectedThread(
                    updatedThread = updatedUiModel,
                )
            }.onFailure { throwable ->
                dismissThreadMenu()

                sendEvent(
                    CommunityEvent.ShowToast(
                        message = throwable.message
                            ?: "알림 설정을 변경하지 못했어요.",
                    )
                )
            }
        }
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

    private fun leaveSelectedThread() {
        val selectedThread = _state.value.selectedThread ?: return

        viewModelScope.launch {
            leaveCommunityThreadUseCase(
                threadId = selectedThread.id,
            ).onSuccess {
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

                sendEvent(
                    CommunityEvent.ShowToast(
                        message = "스레드에서 나갔어요.",
                    )
                )
            }.onFailure { throwable ->
                _state.update {
                    it.copy(
                        showLeaveDialog = false,
                    )
                }

                sendEvent(
                    CommunityEvent.ShowToast(
                        message = throwable.message
                            ?: "스레드에서 나가지 못했어요.",
                    )
                )
            }
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
            .orEmpty(),
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

private fun CommunityThreadDetail.toUiModel(): CommunityThreadUiModel {
    return CommunityThreadUiModel(
        id = threadId,
        title = title,
        contentPreview = lastMessage?.preview
            ?.takeIf { preview ->
                preview.isNotBlank()
            }
            .orEmpty(),
        category = category.toUiCategory(),
        icon = icon,
        dayText = lastMessage?.createdAt
            ?.toDayText()
            ?: updatedAt.toDayText(),
        memberCount = memberCount,
        unreadCount = unreadCount,
        maxMembers = maxMembers,
        isPinned = isPinned,
        isNotificationEnabled = !isMuted,
        isMine = myRole == CommunityThreadRole.OWNER,
    )
}

private const val THREAD_POLLING_INTERVAL_MS = 3_000L
