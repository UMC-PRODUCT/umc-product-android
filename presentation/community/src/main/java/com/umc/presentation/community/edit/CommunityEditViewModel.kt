package com.umc.presentation.community.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.domain.model.community.CommunityThreadCategory
import com.umc.domain.usecase.community.GetCommunityThreadDetailUseCase
import com.umc.presentation.community.model.CommunityAiState
import com.umc.presentation.community.model.CommunityCategory
import com.umc.presentation.community.model.CommunityChallengerUiModel
import com.umc.domain.usecase.community.UpdateCommunityThreadUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import com.umc.domain.usecase.community.DeleteCommunityThreadUseCase
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CommunityEditViewModel @Inject constructor(
    private val getCommunityThreadDetailUseCase:
    GetCommunityThreadDetailUseCase,
    private val updateCommunityThreadUseCase:
    UpdateCommunityThreadUseCase,
    private val deleteCommunityThreadUseCase:
    DeleteCommunityThreadUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(
        CommunityEditState()
    )
    val state: StateFlow<CommunityEditState> =
        _state.asStateFlow()

    private val _event = Channel<CommunityEditEvent>()
    val event = _event.receiveAsFlow()

    fun onAction(
        action: CommunityEditAction,
    ) {
        when (action) {
            CommunityEditAction.OnBackClick -> {
                sendEvent(
                    CommunityEditEvent.NavigateBack
                )
            }

            CommunityEditAction.OnSaveClick -> {
                saveThread()
            }

            CommunityEditAction.OnChallengerCardClick -> {
                openChallengerBottomSheet()
            }

            CommunityEditAction.OnDismissChallengerBottomSheet -> {
                dismissChallengerBottomSheet()
            }

            is CommunityEditAction.OnTitleChanged -> {
                updateTitle(
                    title = action.title,
                )
            }

            is CommunityEditAction.OnDescriptionChanged -> {
                updateDescription(
                    description = action.description,
                )
            }

            CommunityEditAction.OnMemberInviteSuccess -> {
                _state.update {
                    it.copy(
                        showChallengerBottomSheet = false,
                    )
                }

                refreshMemberCount()

                sendEvent(
                    CommunityEditEvent.MemberInviteSuccess
                )
            }



            CommunityEditAction.OnRetryClassificationClick -> {
                requestClassification()
            }

            CommunityEditAction.OnChangeEmojiClick -> {
                sendEvent(
                    CommunityEditEvent.NavigateToEmojiPicker
                )
            }

            CommunityEditAction.OnDeleteThreadClick -> {
                openDeleteDialog()
            }

            CommunityEditAction.OnDismissDeleteDialog -> {
                dismissDeleteDialog()
            }

            CommunityEditAction.OnConfirmDeleteClick -> {
                deleteThread()
            }
        }
    }


    fun loadThread(
        threadId: String,
    ) {
        if (threadId.isBlank()) {
            sendEvent(
                CommunityEditEvent.ShowToast(
                    message = "스레드 정보를 확인할 수 없어요.",
                )
            )
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    threadId = threadId,
                    isLoading = true,
                    errorMessage = null,
                )
            }

            getCommunityThreadDetailUseCase(
                threadId = threadId,
            ).onSuccess { thread ->
                _state.update {
                    it.copy(
                        threadId = thread.threadId,
                        title = thread.title,
                        description = thread.description,
                        currentChallengerCount = thread.memberCount,
                        maxChallengerCount = thread.maxMembers,
                        aiState = CommunityAiState.SUCCESS,
                        classifiedCategory = thread.category.toUiCategory(),
                        selectedIcon = thread.icon,
                        isLoading = false,
                        errorMessage = null,
                    )
                }
            }.onFailure { throwable ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message
                            ?: "스레드 정보를 불러오지 못했어요.",
                    )
                }

                sendEvent(
                    CommunityEditEvent.ShowToast(
                        message = "스레드 정보를 불러오지 못했어요.",
                    )
                )
            }
        }
    }

    private fun refreshMemberCount() {
        val threadId = _state.value.threadId

        if (threadId.isBlank()) {
            return
        }

        viewModelScope.launch {
            getCommunityThreadDetailUseCase(
                threadId = threadId,
            ).onSuccess { thread ->
                _state.update {
                    it.copy(
                        currentChallengerCount = thread.memberCount,
                        maxChallengerCount = thread.maxMembers,
                    )
                }
            }
        }
    }

    /**
     * 제목 수정은 AI 재분류 상태에 영향 X
     */
    private fun updateTitle(
        title: String,
    ) {
        _state.update {
            it.copy(
                title = title,
            )
        }
    }

    /**
     * 스레드 특징을 수정했을 때만 재분류 필요 상태로 바꿈
     */
    private fun updateDescription(
        description: String,
    ) {
        _state.update {
            it.copy(
                description = description,
                aiState = when {
                    description.isBlank() -> {
                        CommunityAiState.GUIDE
                    }

                    it.classifiedCategory != null -> {
                        CommunityAiState.NEEDS_RECLASSIFICATION
                    }

                    else -> {
                        CommunityAiState.GUIDE
                    }
                },
            )
        }
    }

    private fun openChallengerBottomSheet() {
        _state.update {
            it.copy(
                showChallengerBottomSheet = true,
            )
        }
    }

    private fun dismissChallengerBottomSheet() {
        _state.update {
            it.copy(
                showChallengerBottomSheet = false,
            )
        }
    }



    private fun requestClassification() {
        val currentState = _state.value

        if (!currentState.canRequestClassification) {
            sendEvent(
                CommunityEditEvent.ShowToast(
                    message = "제목과 스레드 특징을 입력해주세요.",
                )
            )
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    aiState = CommunityAiState.LOADING,
                )
            }

            delay(CLASSIFICATION_DELAY)

            val latestState = _state.value

            val result = classifyCategory(
                title = latestState.title,
                description = latestState.description,
            )

            _state.update {
                it.copy(
                    aiState = result.aiState,
                    classifiedCategory = result.category,
                )
            }
        }
    }

    private fun classifyCategory(
        title: String,
        description: String,
    ): ClassificationResult {
        val targetText =
            "$title $description".lowercase()

        return when {
            targetText.contains("과제") ||
                    targetText.contains("스터디") ||
                    targetText.contains("코드") -> {
                ClassificationResult(
                    aiState = CommunityAiState.SUCCESS,
                    category = CommunityCategory.STUDY,
                )
            }

            targetText.contains("공지") ||
                    targetText.contains("일정") ||
                    targetText.contains("안내") -> {
                ClassificationResult(
                    aiState = CommunityAiState.SUCCESS,
                    category = CommunityCategory.PROJECT,
                )
            }

            targetText.contains("질문") ||
                    targetText.contains("궁금") -> {
                ClassificationResult(
                    aiState = CommunityAiState.SUCCESS,
                    category = CommunityCategory.QNA,
                )
            }

            else -> {
                ClassificationResult(
                    aiState = CommunityAiState.SUCCESS,
                    category = CommunityCategory.FREE,
                )
            }
        }
    }

    private fun saveThread() {
        val currentState = _state.value

        if (!currentState.isSaveEnabled) {
            sendEvent(
                CommunityEditEvent.ShowToast(
                    message = "제목과 스레드 특징을 모두 입력해주세요.",
                )
            )
            return
        }

        val category =
            currentState.classifiedCategory ?: run {
                sendEvent(
                    CommunityEditEvent.ShowToast(
                        message = "카테고리를 선택해주세요.",
                    )
                )
                return
            }

        viewModelScope.launch {

            _state.update {
                it.copy(
                    isSaving = true,
                )
            }

            updateCommunityThreadUseCase(
                threadId = currentState.threadId,
                title = currentState.title,
                description = currentState.description,
                category = category.name,
                icon = currentState.selectedIcon,
            ).onSuccess {

                _state.update {
                    it.copy(
                        isSaving = false,
                    )
                }

                sendEvent(
                    CommunityEditEvent.SaveSuccess
                )
            }.onFailure { throwable ->

                _state.update {
                    it.copy(
                        isSaving = false,
                    )
                }

                sendEvent(
                    CommunityEditEvent.ShowToast(
                        message = throwable.message
                            ?: "스레드를 수정하지 못했어요.",
                    )
                )
            }
        }
    }

    private fun openDeleteDialog() {
        _state.update {
            it.copy(
                showDeleteDialog = true,
            )
        }
    }

    private fun dismissDeleteDialog() {
        _state.update {
            it.copy(
                showDeleteDialog = false,
            )
        }
    }

    private fun deleteThread() {
        val currentState = _state.value

        if (currentState.threadId.isBlank()) {
            sendEvent(
                CommunityEditEvent.ShowToast(
                    message = "스레드 정보를 확인할 수 없어요.",
                )
            )
            return
        }

        if (currentState.isDeleting) {
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    showDeleteDialog = false,
                    isDeleting = true,
                )
            }

            deleteCommunityThreadUseCase(
                threadId = currentState.threadId,
            ).onSuccess {
                _state.update {
                    it.copy(
                        isDeleting = false,
                    )
                }

                sendEvent(
                    CommunityEditEvent.DeleteSuccess
                )
            }.onFailure { throwable ->
                _state.update {
                    it.copy(
                        isDeleting = false,
                    )
                }

                sendEvent(
                    CommunityEditEvent.ShowToast(
                        message = throwable.message
                            ?: "스레드를 삭제하지 못했어요.",
                    )
                )
            }
        }
    }

    private fun sendEvent(
        event: CommunityEditEvent,
    ) {
        viewModelScope.launch {
            _event.send(event)
        }
    }

    private data class ClassificationResult(
        val aiState: CommunityAiState,
        val category: CommunityCategory,
    )

    companion object {
        private const val CLASSIFICATION_DELAY = 1500L
    }
}

/**
 * 상세 API의 카테고리 타입이 String일 때 사용
 */
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

        CommunityThreadCategory.FREE -> {
            CommunityCategory.FREE
        }

        CommunityThreadCategory.UNKNOWN -> {
            CommunityCategory.FREE
        }
    }
}
