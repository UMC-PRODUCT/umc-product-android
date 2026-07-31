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
        }
    }

    private fun selectCategory(category: CommunityCategory) {
        _state.update {
            it.copy(selectedCategory = category)
        }
    }

    private fun loadThreads() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
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
                            errorMessage = "스레드를 불러오지 못했어요.",
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
        private val PREVIEW_STATE = PreviewState.LOADING
    }

    private val dummyThreads = listOf(
        CommunityThreadUiModel(
            id = 1L,
            title = "iOS 3주차 과제 인증방",
            contentPreview = "다른 과제 인증 완료했나요? 오늘 자정까지...",
            category = CommunityCategory.STUDY,
            dayText = "화요일",
            commentCount = 3,
            isPinned = true,
        ),
        CommunityThreadUiModel(
            id = 2L,
            title = "정기 모임 일정 안내",
            contentPreview = "다른 과제 인증 완료했나요? 오늘 자정까지...",
            category = CommunityCategory.PART_NOTICE,
            dayText = "화요일",
            commentCount = 3,
            isPinned = true,
        ),
        CommunityThreadUiModel(
            id = 3L,
            title = "OT 장소 변경 안내",
            contentPreview = "다른 과제 인증 완료했나요? 오늘 자정까지...",
            category = CommunityCategory.PART_NOTICE,
            dayText = "화요일",
            commentCount = 3,
        ),
        CommunityThreadUiModel(
            id = 4L,
            title = "리액트 상태관리 질문 있어요",
            contentPreview = "다른 과제 인증 완료했나요? 오늘 자정까지...",
            category = CommunityCategory.QUESTION,
            dayText = "화요일",
            commentCount = 3,
        ),
        CommunityThreadUiModel(
            id = 5L,
            title = "이번 주 회식 어때요?",
            contentPreview = "다른 과제 인증 완료했나요? 오늘 자정까지...",
            category = CommunityCategory.FREE,
            dayText = "화요일",
            commentCount = 3,
        ),
    )
}