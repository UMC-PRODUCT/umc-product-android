package com.umc.presentation.community.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.presentation.community.model.CommunityAiState
import com.umc.presentation.community.model.CommunityCategory
import com.umc.presentation.community.model.CommunityChallengerUiModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CommunityEditViewModel : ViewModel() {

    private val _state = MutableStateFlow(CommunityEditState())
    val state: StateFlow<CommunityEditState> = _state.asStateFlow()

    private val _event = Channel<CommunityEditEvent>()
    val event = _event.receiveAsFlow()

    fun onAction(action: CommunityEditAction) {
        when (action) {
            CommunityEditAction.OnBackClick -> {
                sendEvent(CommunityEditEvent.NavigateBack)
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
                updateTitle(action.title)
            }

            is CommunityEditAction.OnDescriptionChanged -> {
                updateDescription(action.description)
            }

            is CommunityEditAction.OnChallengersSelected -> {
                updateSelectedChallengers(action.challengers)
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

    fun loadThread(threadId: String) {
        _state.update {
            it.copy(
                threadId = threadId,
                title = "iOS 3주차 과제 인증방",
                description = "iOS 스터디원들끼리 3주차 과제를 인증하고 서로 코드 피드백을 주고받는 방이에요.",
                selectedChallengers = dummyChallengers,
                aiState = CommunityAiState.SUCCESS,
                classifiedCategory = CommunityCategory.STUDY,
            )
        }
    }

    /**
     * 제목 수정은 AI 분류 상태에 영향을 주지 않는다.
     */
    private fun updateTitle(title: String) {
        _state.update {
            it.copy(
                title = title,
            )
        }
    }

    /**
     * 스레드 특징이 수정된 경우에만 재분류 필요 상태로 변경한다.
     */
    private fun updateDescription(description: String) {
        _state.update { currentState ->
            currentState.copy(
                description = description,
                aiState = when {
                    description.isBlank() -> {
                        CommunityAiState.GUIDE
                    }

                    currentState.classifiedCategory != null -> {
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

    private fun updateSelectedChallengers(
        challengers: List<CommunityChallengerUiModel>,
    ) {
        _state.update { currentState ->
            currentState.copy(
                selectedChallengers = challengers
                    .distinctBy { challenger ->
                        challenger.id
                    }
                    .take(currentState.maxChallengerCount),
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

            val result = classifyCategory(
                title = _state.value.title,
                description = _state.value.description,
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
        val targetText = "$title $description".lowercase()

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

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isSaving = true,
                )
            }

            delay(SAVE_DELAY)

            _state.update {
                it.copy(
                    isSaving = false,
                )
            }

            sendEvent(
                CommunityEditEvent.SaveSuccess
            )
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
        viewModelScope.launch {
            _state.update {
                it.copy(
                    showDeleteDialog = false,
                )
            }

            delay(DELETE_DELAY)

            sendEvent(
                CommunityEditEvent.DeleteSuccess
            )
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
        private const val SAVE_DELAY = 700L
        private const val DELETE_DELAY = 500L
    }

    private val dummyChallengers = listOf(
        CommunityChallengerUiModel(
            id = 1L,
            name = "김도연",
        ),
        CommunityChallengerUiModel(
            id = 2L,
            name = "홍길동",
        ),
        CommunityChallengerUiModel(
            id = 3L,
            name = "김영희",
        ),
    )
}