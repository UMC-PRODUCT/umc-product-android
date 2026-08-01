package com.umc.presentation.community.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.presentation.community.model.CommunityAiState
import com.umc.presentation.community.model.CommunityCategory
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job

class CommunityCreateViewModel : ViewModel() {

    private val _state = MutableStateFlow(CommunityCreateState())
    val state: StateFlow<CommunityCreateState> = _state.asStateFlow()
    private var classificationJob: Job? = null

    private val _event = Channel<CommunityCreateEvent>()
    val event = _event.receiveAsFlow()

    fun onAction(action: CommunityCreateAction) {
        when (action) {
            CommunityCreateAction.OnBackClick -> {
                sendEvent(CommunityCreateEvent.NavigateBack)
            }

            CommunityCreateAction.OnCompleteClick -> {
                createThread()
            }

            CommunityCreateAction.OnChallengerCardClick -> {
                openChallengerBottomSheet()
            }

            CommunityCreateAction.OnDismissChallengerBottomSheet -> {
                dismissChallengerBottomSheet()
            }

            is CommunityCreateAction.OnTitleChanged -> {
                updateTitle(action.title)
            }

            is CommunityCreateAction.OnDescriptionChanged -> {
                updateDescription(action.description)
            }

            is CommunityCreateAction.OnChallengersSelected -> {
                updateSelectedChallengers(action.challengers)
            }

            CommunityCreateAction.OnRequestClassificationClick -> {
                requestClassification()
            }

            CommunityCreateAction.OnRetryClassificationClick -> {
                requestClassification()
            }

            CommunityCreateAction.OnChangeEmojiClick -> {
                sendEvent(
                    CommunityCreateEvent.NavigateToEmojiPicker
                )
            }
        }
    }

    private fun updateTitle(title: String) {
        val previousState = _state.value

        classificationJob?.cancel()

        _state.update {
            it.copy(
                title = title,
            )
        }

        // 한 번도 분류하지 않은 최초 입력 상태에서만 자동 분석
        if (
            previousState.classifiedCategory == null &&
            title.isNotBlank() &&
            _state.value.description.isNotBlank()
        ) {
            scheduleAutoClassification()
        }
    }

    private fun updateDescription(description: String) {
        val previousState = _state.value

        classificationJob?.cancel()

        _state.update {
            it.copy(
                description = description,
                aiState = when {
                    description.isBlank() -> {
                        CommunityAiState.GUIDE
                    }

                    previousState.classifiedCategory != null -> {
                        CommunityAiState.NEEDS_RECLASSIFICATION
                    }

                    else -> {
                        CommunityAiState.GUIDE
                    }
                },
            )
        }

        // 한 번도 분류하지 않은 최초 입력 상태에서만 자동 분석
        if (
            previousState.classifiedCategory == null &&
            _state.value.title.isNotBlank() &&
            description.isNotBlank()
        ) {
            scheduleAutoClassification()
        }
    }

    private fun scheduleAutoClassification() {
        classificationJob?.cancel()

        val currentState = _state.value

        if (
            currentState.title.isBlank() ||
            currentState.description.isBlank()
        ) {
            _state.update {
                it.copy(
                    aiState = CommunityAiState.GUIDE,
                )
            }
            return
        }

        classificationJob = viewModelScope.launch {
            // 입력하는 도중 매 글자마다 분석되지 않도록 대기
            delay(600L)

            _state.update {
                it.copy(
                    aiState = CommunityAiState.LOADING,
                )
            }

            // 테스트용 AI 분석 시간
            delay(6000L)

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
        challengers: List<com.umc.presentation.community.model.CommunityChallengerUiModel>,
    ) {
        _state.update { currentState ->
            currentState.copy(
                selectedChallengers = challengers
                    .distinctBy { challenger -> challenger.id }
                    .take(currentState.maxChallengerCount),
                showChallengerBottomSheet = false,
            )
        }
    }

    private fun requestClassification() {
        classificationJob?.cancel()

        val currentState = _state.value

        if (!currentState.canRequestClassification) {
            sendEvent(
                CommunityCreateEvent.ShowToast(
                    message = "제목과 스레드 특징을 입력해주세요.",
                )
            )
            return
        }

        classificationJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    aiState = CommunityAiState.LOADING,
                )
            }

            delay(1800L)

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

    private fun createThread() {
        val currentState = _state.value

        if (!currentState.isCompleteEnabled) {
            sendEvent(
                CommunityCreateEvent.ShowToast(
                    message = "제목과 스레드 특징을 모두 입력해주세요.",
                )
            )
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isSubmitting = true,
                )
            }

            delay(SUBMIT_DELAY)

            _state.update {
                it.copy(
                    isSubmitting = false,
                )
            }

            sendEvent(CommunityCreateEvent.CreateSuccess)
        }
    }

    private fun sendEvent(event: CommunityCreateEvent) {
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
        private const val SUBMIT_DELAY = 700L
    }
}