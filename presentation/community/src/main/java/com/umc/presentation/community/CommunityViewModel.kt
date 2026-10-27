package com.umc.presentation.community

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.component.theme.AppStrings
import com.umc.domain.model.community.CommunityThread
import com.umc.domain.model.community.CommunityThreadCategory
import com.umc.domain.model.community.CommunityThreadRole
import com.umc.domain.model.community.canEditThread
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
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 커뮤니티 메인 화면의 상태와 비즈니스 로직을 관리하는 ViewModel
 *
 * 주요 기능
 * - 스레드 목록 조회 및 주기적 Polling
 * - 카테고리 필터 상태 관리
 * - 스레드 고정/고정 해제
 * - 스레드 알림 켜기/끄기
 * - 스레드 수정 화면 이동
 * - 스레드 나가기
 * - 화면 이동 및 Toast 이벤트 전달
 */
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
    private var leaveJob: Job? = null
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

        loadAllThreadPages().onSuccess { (pinnedThreads, threads) ->
            val uiThreads = buildList {
                addAll(
                    pinnedThreads.map { thread ->
                        thread.toUiModel(forcePinned = true)
                    }
                )
                addAll(
                    threads.map { thread ->
                        thread.toUiModel(forcePinned = false)
                    }
                )
            }

            _state.update {
                it.copy(
                    isLoading = false,
                    threads = uiThreads.toImmutableList(),
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
                        errorMessage =
                            "네트워크 연결을 확인하고 다시 시도해주세요.",
                    )
                }
            }
        }
    }

    /**
     * 목록은 폴링마다 통째로 교체되므로, 스크롤 페이징 대신 nextOffset 을 따라 모든 페이지를 한 번에 받아온다.
     * 어느 페이지든 실패하면 전체를 실패로 처리해 마지막 성공 목록을 유지한다.
     */
    private suspend fun loadAllThreadPages(): Result<Pair<List<CommunityThread>, List<CommunityThread>>> {
        val pinnedThreads = mutableListOf<CommunityThread>()
        val threads = mutableListOf<CommunityThread>()
        var offset = 0
        var pageCount = 0

        while (pageCount < MAX_THREAD_PAGES) {
            val page = getCommunityThreadsUseCase(
                filter = "all",
                query = null,
                offset = offset,
                limit = THREAD_PAGE_SIZE,
            ).getOrElse { throwable ->
                return Result.failure(throwable)
            }
            pageCount++

            pinnedThreads.addAll(page.pinnedThreads)
            threads.addAll(page.threads)

            val nextOffset = page.nextOffset?.toIntOrNull()
            if (nextOffset == null || nextOffset <= offset) break
            offset = nextOffset
        }

        // 페이지마다 고정 스레드가 반복되어도 LazyColumn key 가 겹치지 않게 중복을 걸러낸다.
        val distinctPinned = pinnedThreads.distinctBy { it.threadId }
        val pinnedIds = distinctPinned.mapTo(hashSetOf()) { it.threadId }
        val distinctThreads = threads
            .filterNot { it.threadId in pinnedIds }
            .distinctBy { it.threadId }

        return Result.success(distinctPinned to distinctThreads)
    }

    private fun openThreadMenu(
        threadId: String,
    ) {
        val selectedThread = _state.value.threads.find { thread ->
            thread.id == threadId
        } ?: return

        if (!selectedThread.isJoined) return

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
                        }.toImmutableList(),
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
                }.toImmutableList(),
                selectedThread = updatedThread,
                showThreadMenuDialog = false,
            )
        }
    }

    private fun navigateToEditSelectedThread() {
        val selectedThread = _state.value.selectedThread ?: return

        if (!selectedThread.canEdit) return

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
        if (leaveJob?.isActive == true) return

        leaveJob = viewModelScope.launch {
            // 나간 스레드는 메뉴가 없어 고정을 풀 수 없으므로 나가기 전에 먼저 푼다. 방장은 나가기가 거절되므로 건드리지 않는다.
            // 해제에 실패해도 나가기는 진행한다(목록 경로는 실패 원인을 구분하지 못한다).
            if (selectedThread.isPinned && !selectedThread.isMine) {
                unpinCommunityThreadUseCase(threadId = selectedThread.id)
            }

            leaveCommunityThreadUseCase(
                threadId = selectedThread.id,
            ).onSuccess {
                _state.update { currentState ->
                    currentState.copy(
                        // 나간 스레드도 목록에 남는다(보기 전용). 미참여 상태로만 바꾼다.
                        threads = currentState.threads.map { thread ->
                            if (thread.id == selectedThread.id) {
                                thread.copy(
                                    isJoined = false,
                                    isPinned = false,
                                    memberCount = (thread.memberCount - 1).coerceAtLeast(0),
                                    unreadCount = 0,
                                )
                            } else {
                                thread
                            }
                        }.toImmutableList(),
                        selectedThread = null,
                        showThreadMenuDialog = false,
                        showLeaveDialog = false,
                    )
                }

                sendEvent(
                    CommunityEvent.ShowToast(
                        message = AppStrings.CHAT_LEAVE_SUCCESS,
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
        canEdit = myRole.canEditThread(),
        isJoined = isJoined,
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
        canEdit = myRole.canEditThread(),
        isJoined = isJoined,
    )
}

private const val THREAD_POLLING_INTERVAL_MS = 3_000L

// 서버가 허용하는 최대 limit. 평소에는 한 번의 요청으로 끝난다.
private const val THREAD_PAGE_SIZE = 100

// nextOffset 이 비정상적으로 이어져도 폴링이 무한 반복되지 않게 하는 상한
private const val MAX_THREAD_PAGES = 10
