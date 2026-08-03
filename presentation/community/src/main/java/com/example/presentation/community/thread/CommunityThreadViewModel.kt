package com.example.presentation.community.thread

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.community.thread.*
import com.umc.domain.repository.community.CommunityThreadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommunityThreadViewModel @Inject constructor(
    private val repository: CommunityThreadRepository,
) : BaseViewModel<CommunityThreadUiState, CommunityThreadEvent>(CommunityThreadUiState()) {
    private var searchJob: Job? = null

    init {
        refresh()
    }

    fun refresh() = load(reset = true)

    fun loadNext() {
        val state = uiState.value
        if (!state.isLoadingMore && state.nextOffset != null) load(reset = false)
    }

    fun setFilter(filter: CommunityThreadFilter) {
        updateState { copy(filter = filter) }
        refresh()
    }

    fun search(query: String) {
        updateState { copy(query = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            refresh()
        }
    }

    fun createThread(request: CreateCommunityThread) = viewModelScope.launch {
        startLoading()
        resultResponse(
            repository.createThread(request),
            successCallback = {
                refresh()
                emitEvent(CommunityThreadEvent.OpenChat(it.threadId))
            },
            errorCallback = { emitEvent(CommunityThreadEvent.ShowError(it.message)) },
        )
    }

    fun togglePin(thread: CommunityThreadSummary) = viewModelScope.launch {
        resultResponse(
            repository.setPinned(thread.threadId, !thread.isPinned),
            successCallback = { refresh() },
            errorCallback = { emitEvent(CommunityThreadEvent.ShowError(it.message)) },
        )
    }

    fun toggleMute(threadId: String, muted: Boolean) = viewModelScope.launch {
        resultResponse(
            repository.setMuted(threadId, muted),
            successCallback = { refresh() },
            errorCallback = { emitEvent(CommunityThreadEvent.ShowError(it.message)) },
        )
    }

    private fun load(reset: Boolean) = viewModelScope.launch {
        val state = uiState.value
        val offset = if (reset) 0 else state.nextOffset?.toIntOrNull() ?: return@launch
        updateState {
            if (reset) copy(isRefreshing = true) else copy(isLoadingMore = true)
        }
        when (
            val response = repository.getThreads(
                filter = state.filter.apiValue,
                query = state.query.takeIf(String::isNotBlank),
                offset = offset,
            )
        ) {
            is ApiState.Success -> updateState {
                copy(
                    pinned = response.data.pinned,
                    threads = if (reset) response.data.threads else (threads + response.data.threads)
                        .distinctBy(CommunityThreadSummary::threadId),
                    nextOffset = response.data.nextOffset,
                    total = response.data.total,
                    isRefreshing = false,
                    isLoadingMore = false,
                    errorMessage = null,
                )
            }
            is ApiState.Fail -> {
                updateState {
                    copy(
                        isRefreshing = false,
                        isLoadingMore = false,
                        errorMessage = response.failState.message,
                    )
                }
                emitEvent(CommunityThreadEvent.ShowError(response.failState.message))
            }
        }
    }

    fun openChat(threadId: String) = emitEvent(CommunityThreadEvent.OpenChat(threadId))
}

data class CommunityThreadUiState(
    val pinned: List<CommunityThreadSummary> = emptyList(),
    val threads: List<CommunityThreadSummary> = emptyList(),
    val filter: CommunityThreadFilter = CommunityThreadFilter.ALL,
    val query: String = "",
    val nextOffset: String? = null,
    val total: String = "0",
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
) : UiState

enum class CommunityThreadFilter(val apiValue: String) {
    ALL("all"),
    UNREAD("unread"),
    STUDY("STUDY"),
    QNA("QNA"),
    PROJECT("PROJECT"),
    FREE("FREE"),
}

sealed interface CommunityThreadEvent : UiEvent {
    data class OpenChat(val threadId: String) : CommunityThreadEvent
    data class ShowError(val message: String) : CommunityThreadEvent
}
