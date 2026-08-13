package com.umc.presentation.community.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.domain.usecase.ai.ClassifyCommunityThreadUseCase
import com.umc.domain.usecase.community.CreateCommunityThreadUseCase
import com.umc.presentation.community.model.CommunityAiState
import com.umc.presentation.community.model.CommunityCategory
import com.umc.presentation.community.model.CommunityChallengerUiModel
import com.umc.domain.model.base.ApiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CommunityCreateViewModel @Inject constructor(
    private val createCommunityThreadUseCase: CreateCommunityThreadUseCase,
    private val classifyCommunityThreadUseCase: ClassifyCommunityThreadUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(
        CommunityCreateState()
    )
    val state: StateFlow<CommunityCreateState> =
        _state.asStateFlow()

    private val _event = Channel<CommunityCreateEvent>()
    val event = _event.receiveAsFlow()

    private var classificationJob: Job? = null

    fun onAction(
        action: CommunityCreateAction,
    ) {
        when (action) {
            CommunityCreateAction.OnBackClick -> {
                sendEvent(
                    CommunityCreateEvent.NavigateBack
                )
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
                updateTitle(
                    title = action.title,
                )
            }

            is CommunityCreateAction.OnDescriptionChanged -> {
                updateDescription(
                    description = action.description,
                )
            }

            is CommunityCreateAction.OnChallengersSelected -> {
                updateSelectedChallengers(
                    challengers = action.challengers,
                )
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

    private fun updateTitle(
        title: String,
    ) {
        val previousState = _state.value

        classificationJob?.cancel()

        _state.update {
            it.copy(
                title = title,
            )
        }

        // 최초 분류 전이고 제목과 특징이 모두 입력된 경우에만 자동 분류
        if (
            previousState.classifiedCategory == null &&
            title.isNotBlank() &&
            _state.value.description.isNotBlank()
        ) {
            scheduleAutoClassification()
        }
    }

    private fun updateDescription(
        description: String,
    ) {
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

        // 최초 분류 전이고 제목과 특징이 모두 입력된 경우에만 자동 분류
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
            // 입력 도중 매 글자마다 분석되지 않도록 대기
            delay(AUTO_CLASSIFICATION_DEBOUNCE)

            _state.update {
                it.copy(
                    aiState = CommunityAiState.LOADING,
                )
            }

            // 현재는 테스트용 AI 분석 시간
            delay(AUTO_CLASSIFICATION_DELAY)

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
                        challenger.memberId
                    }
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

            delay(MANUAL_CLASSIFICATION_DELAY)

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

    private suspend fun classifyCategory(
        title: String,
        description: String,
    ): ClassificationResult {

        return when (
            val result = classifyCommunityThreadUseCase(
                title = title,
                description = description,
            )
        ) {
            is ApiState.Success<*> -> {
                val category = (result.data as? String)
                    ?.trim()
                    ?.uppercase()
                    ?.toCommunityCategory()
                    ?: CommunityCategory.FREE

                ClassificationResult(
                    aiState = CommunityAiState.SUCCESS,
                    category = category,
                )
            }

            is ApiState.Fail -> {
                ClassificationResult(
                    aiState = CommunityAiState.FAILED,
                    category = CommunityCategory.FREE,
                )
            }

            else -> {
                ClassificationResult(
                    aiState = CommunityAiState.FAILED,
                    category = CommunityCategory.FREE,
                )
            }
        }
    }

    private fun createThread() {
        val currentState = _state.value

        if (currentState.isSubmitting) {
            return
        }

        if (!currentState.isCompleteEnabled) {
            sendEvent(
                CommunityCreateEvent.ShowToast(
                    message = "제목과 스레드 특징을 모두 입력해주세요.",
                )
            )
            return
        }

        val category = currentState.classifiedCategory

        if (category == null) {
            sendEvent(
                CommunityCreateEvent.ShowToast(
                    message = "카테고리 분류를 완료해주세요.",
                )
            )
            return
        }

        /*
         * CommunityChallengerUiModel.id가 Long이므로
         * 생성 요청의 memberIds에 바로 사용
         */
        val memberIds = currentState.selectedChallengers
            .map { challenger ->
                challenger.memberId
            }
            .distinct()

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isSubmitting = true,
                )
            }

            createCommunityThreadUseCase(
                title = currentState.title.trim(),
                description = currentState.description.trim(),
                category = category.toApiCategory(),
                icon = currentState.selectedIcon.ifBlank {
                    DEFAULT_ICON
                },
                memberIds = memberIds,
            ).onSuccess { createdThread ->
                _state.update {
                    it.copy(
                        isSubmitting = false,
                    )
                }

                sendEvent(
                    CommunityCreateEvent.CreateSuccess(
                        threadId = createdThread.threadId,
                    )
                )
            }.onFailure { throwable ->
                _state.update {
                    it.copy(
                        isSubmitting = false,
                    )
                }

                sendEvent(
                    CommunityCreateEvent.ShowToast(
                        message = throwable.message
                            ?: "스레드를 만들지 못했어요.",
                    )
                )
            }
        }
    }

    private fun sendEvent(
        event: CommunityCreateEvent,
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
        private const val AUTO_CLASSIFICATION_DEBOUNCE = 600L
        private const val AUTO_CLASSIFICATION_DELAY = 6000L
        private const val MANUAL_CLASSIFICATION_DELAY = 1800L

        private const val DEFAULT_ICON = "📚"
    }
}

private fun CommunityCategory.toApiCategory(): String {
    return when (this) {
        CommunityCategory.STUDY -> "STUDY"
        CommunityCategory.QNA -> "QNA"
        CommunityCategory.PROJECT -> "PROJECT"
        CommunityCategory.FREE -> "FREE"

        CommunityCategory.ALL,
        CommunityCategory.UNREAD,
            -> error("스레드 생성에 사용할 수 없는 카테고리입니다: $this")
    }
}

private fun String.toCommunityCategory(): CommunityCategory {
    return when (this) {
        "STUDY" -> CommunityCategory.STUDY
        "PROJECT" -> CommunityCategory.PROJECT
        "QNA" -> CommunityCategory.QNA
        "FREE" -> CommunityCategory.FREE
        else -> CommunityCategory.FREE
    }
}