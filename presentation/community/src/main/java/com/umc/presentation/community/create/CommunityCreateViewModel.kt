package com.umc.presentation.community.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.component.theme.AppStrings
import com.umc.domain.usecase.appDataStore.GetUserInfoUseCase
import com.umc.domain.usecase.ai.ClassifyCommunityThreadUseCase
import com.umc.domain.usecase.community.CreateCommunityThreadUseCase
import com.umc.presentation.community.model.CommunityAiState
import com.umc.presentation.community.model.CommunityCategory
import com.umc.presentation.community.model.CommunityChallengerUiModel
import com.umc.domain.model.base.ApiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 커뮤니티 스레드 생성 화면의 상태와 비즈니스 로직을 관리하는 ViewModel
 *
 * 주요 기능
 * - 제목 및 스레드 특징 입력 관리
 * - 챌린저 선택 관리
 * - AI 카테고리 자동/수동 분류
 * - 스레드 생성 API 호출
 * - 화면 이동 및 Toast 이벤트 전달
 */
@HiltViewModel
class CommunityCreateViewModel @Inject constructor(
    private val createCommunityThreadUseCase: CreateCommunityThreadUseCase,
    private val classifyCommunityThreadUseCase: ClassifyCommunityThreadUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase,
) : ViewModel() {

    /** 스레드 생성 화면 UI 상태 */
    private val _state = MutableStateFlow(
        CommunityCreateState()
    )
    val state: StateFlow<CommunityCreateState> =
        _state.asStateFlow()

    /** 화면 이동 및 Toast 등 일회성 이벤트 */
    private val _event = Channel<CommunityCreateEvent>()
    val event = _event.receiveAsFlow()

    /** AI 분류 요청 및 디바운스를 관리하는 Job */
    private var classificationJob: Job? = null

    init {
        // 생성 시점에 동기로 검사할 수 있도록 내 memberId를 미리 들고 있는다
        viewModelScope.launch {
            getUserInfoUseCase().collectLatest { userInfo ->
                _state.update { it.copy(myMemberId = userInfo.id) }
            }
        }
    }

    /**
     * 화면에서 전달된 Action을 처리합니다.
     */
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

            is CommunityCreateAction.OnCategorySelected -> {
                selectCategory(
                    category = action.category,
                )
            }
        }
    }

    /**
     * 사용자가 아이콘 선택 영역에서 카테고리를 직접 선택합니다.
     *
     * 실제 선택값([CommunityCreateState.selectedCategory])만 바꾸고 AI 결과는 그대로 둡니다.
     * 진행 중이거나 예약된 AI 분류가 선택값을 덮어쓰지 않도록 취소하고,
     * 분류 중이었다면 안내 상태로 되돌립니다.
     * 이후 "카테고리 분류"나 재분류를 누르면 AI 결과로 다시 바뀝니다.
     */
    private fun selectCategory(
        category: CommunityCategory,
    ) {
        classificationJob?.cancel()

        _state.update {
            it.copy(
                selectedCategory = category,
                isCategorySelectedByUser = true,
                aiState = if (it.aiState == CommunityAiState.LOADING) {
                    CommunityAiState.GUIDE
                } else {
                    it.aiState
                },
            )
        }
    }

    /**
     * 스레드 제목을 변경합니다.
     *
     * 최초 AI 분류 전이며 제목과 특징이 모두 입력된 경우
     * 자동 카테고리 분류를 예약합니다.
     */
    private fun updateTitle(
        title: String,
    ) {
        val previousState = _state.value

        // 기존 자동 분류 대기 작업 취소
        classificationJob?.cancel()

        _state.update {
            it.copy(
                title = title,
                // 재분류 중 제목을 고치면 위에서 Job 이 취소되므로 LOADING 에 갇히지 않게 되돌린다
                aiState = if (
                    it.aiState == CommunityAiState.LOADING &&
                    it.aiCategory != null
                ) {
                    CommunityAiState.NEEDS_RECLASSIFICATION
                } else if (it.aiState == CommunityAiState.LOADING) {
                    CommunityAiState.GUIDE
                } else {
                    it.aiState
                },
            )
        }

        // 최초 분류 전(AI 결과·직접 선택 모두 없음)이고 제목과 특징이 모두 입력된 경우에만 자동 분류
        if (
            previousState.aiCategory == null &&
            !previousState.isCategorySelectedByUser &&
            com.umc.presentation.community.model.isCommunityThreadInputValid(title, _state.value.description) &&
            _state.value.description.isNotBlank()
        ) {
            scheduleAutoClassification()
        }
    }

    /**
     * 스레드 특징을 변경합니다.
     *
     * 이미 카테고리 분류가 완료된 이후 특징이 변경되면
     * NEEDS_RECLASSIFICATION 상태로 변경합니다.
     */
    private fun updateDescription(
        description: String,
    ) {
        val previousState = _state.value

        // 기존 자동 분류 대기 작업 취소
        classificationJob?.cancel()

        _state.update {
            it.copy(
                description = description,
                aiState = when {
                    description.isBlank() -> {
                        CommunityAiState.GUIDE
                    }

                    previousState.aiCategory != null -> {
                        CommunityAiState.NEEDS_RECLASSIFICATION
                    }

                    else -> {
                        CommunityAiState.GUIDE
                    }
                },
            )
        }

        // 최초 분류 전(AI 결과·직접 선택 모두 없음)이고 제목과 특징이 모두 입력된 경우에만 자동 분류
        if (
            previousState.aiCategory == null &&
            !previousState.isCategorySelectedByUser &&
            com.umc.presentation.community.model.isCommunityThreadInputValid(_state.value.title, description) &&
            description.isNotBlank()
        ) {
            scheduleAutoClassification()
        }
    }

    /**
     * 제목과 특징 입력 완료 후 AI 자동 분류를 예약합니다.
     *
     * 사용자가 입력하는 동안 매 글자마다 AI 호출이 발생하지 않도록
     * 디바운스 시간을 적용합니다.
     */
    private fun scheduleAutoClassification() {
        classificationJob?.cancel()

        val currentState = _state.value

        // 제목이나 특징이 비어 있으면 안내 상태 유지
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

            // 대기 중 입력값이 변경됐을 수 있으므로 최신 상태 사용
            val latestState = _state.value

            val result = classifyCategory(
                title = latestState.title,
                description = latestState.description,
            )

            // AI 분류 결과 상태 반영
            if (_state.value.title != latestState.title || _state.value.description != latestState.description) return@launch
            _state.update {
                it.copy(
                    aiState = result.aiState,
                    // AI가 분류하면 실제 선택값도 함께 갱신
                    aiCategory = result.category,
                    selectedCategory = result.category,
                )
            }
        }
    }

    /** 챌린저 선택 BottomSheet를 표시합니다. */
    private fun openChallengerBottomSheet() {
        _state.update {
            it.copy(
                showChallengerBottomSheet = true,
            )
        }
    }

    /** 챌린저 선택 BottomSheet를 닫습니다. */
    private fun dismissChallengerBottomSheet() {
        _state.update {
            it.copy(
                showChallengerBottomSheet = false,
            )
        }
    }

    /**
     * BottomSheet에서 선택한 챌린저 목록을 반영합니다.
     *
     * memberId 기준으로 중복을 제거하고
     * 최대 선택 가능 인원까지만 저장합니다.
     */
    private fun updateSelectedChallengers(
        challengers: List<CommunityChallengerUiModel>,
    ) {
        _state.update { currentState ->
            currentState.copy(
                selectedChallengers = challengers
                    .distinctBy { challenger ->
                        challenger.memberId
                    }
                    .take(currentState.maxChallengerCount)
                    .toImmutableList(),
                showChallengerBottomSheet = false,
            )
        }
    }

    /**
     * 사용자가 다시 분류하기 버튼을 눌렀을 때
     * AI 카테고리 분류를 직접 요청합니다.
     */
    private fun requestClassification() {
        classificationJob?.cancel()

        val currentState = _state.value

        // 제목 또는 특징이 입력되지 않았다면 분류하지 않음
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

            // 수동 재분류 로딩 연출 시간
            delay(MANUAL_CLASSIFICATION_DELAY)

            val latestState = _state.value

            val result = classifyCategory(
                title = latestState.title,
                description = latestState.description,
            )

            if (_state.value.title != latestState.title || _state.value.description != latestState.description) return@launch
            _state.update {
                it.copy(
                    aiState = result.aiState,
                    // AI가 분류하면 실제 선택값도 함께 갱신
                    aiCategory = result.category,
                    selectedCategory = result.category,
                )
            }
        }
    }

    /**
     * AI UseCase를 호출하여 스레드 카테고리를 분류합니다.
     *
     * 분류 결과를 CommunityCategory로 변환하며,
     * 실패하거나 알 수 없는 값이 반환되면 FREE를 기본값으로 사용합니다.
     */
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

    /**
     * 입력된 정보를 이용해 새로운 스레드를 생성합니다.
     *
     * 생성 조건을 다시 확인한 뒤
     * 선택된 챌린저 memberId와 AI 분류 결과를 서버에 전달합니다.
     */
    private fun createThread() {
        val currentState = _state.value

        // 이미 생성 요청 중이라면 중복 요청 방지
        if (currentState.isSubmitting) {
            return
        }

        // 스레드 생성에 필요한 필수값 확인
        if (!currentState.isCompleteEnabled) {
            sendEvent(
                CommunityCreateEvent.ShowToast(
                    message = "제목 1~80자·특징 500자 이하와 초대 대상을 확인해주세요.",
                )
            )
            return
        }

        // 분류하지 않았다면 기본값 자유로 생성
        val category = currentState.selectedCategory

        // 본인은 참여자로 넣을 수 없다. 후보 목록이 범용 챌린저 검색이라 본인이 섞여 들어온다
        val myMemberId = currentState.myMemberId
        if (myMemberId > 0L && currentState.selectedChallengers.any { it.memberId == myMemberId }) {
            sendEvent(
                CommunityCreateEvent.ShowToast(
                    message = AppStrings.COMMUNITY_CREATE_SELF_NOT_ALLOWED,
                )
            )
            return
        }

        /*
         * CommunityChallengerUiModel의 memberId가 Long이므로
         * 생성 요청의 memberIds에 바로 사용
         */
        val memberIds = currentState.selectedChallengers
            .map { challenger ->
                challenger.memberId
            }
            .distinct()

        viewModelScope.launch {
            // 완료 버튼 중복 클릭 방지를 위해 생성 진행 상태로 변경
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
                // Keep submission locked until navigation consumes the successful result.
                // 생성된 threadId와 함께 성공 이벤트 전달
                sendEvent(
                    CommunityCreateEvent.CreateSuccess(
                        threadId = createdThread.threadId,
                    )
                )
            }.onFailure {
                _state.update {
                    it.copy(
                        isSubmitting = false,
                    )
                }

                // 서버 메시지 대신 디자인에 정해진 문구의 에러 토스트를 띄운다.
                sendEvent(
                    CommunityCreateEvent.CreateFailed
                )
            }
        }
    }

    /**
     * Route에서 처리할 일회성 이벤트를 전달합니다.
     */
    private fun sendEvent(
        event: CommunityCreateEvent,
    ) {
        viewModelScope.launch {
            _event.send(event)
        }
    }

    /**
     * AI 카테고리 분류 결과를 내부에서 전달하기 위한 모델
     */
    private data class ClassificationResult(
        val aiState: CommunityAiState,
        val category: CommunityCategory,
    )

    companion object {
        /** 자동 분류 전 입력 대기 시간 */
        private const val AUTO_CLASSIFICATION_DEBOUNCE = 600L

        /** 자동 AI 분류 로딩 연출 시간 */
        private const val AUTO_CLASSIFICATION_DELAY = 6000L

        /** 수동 재분류 로딩 연출 시간 */
        private const val MANUAL_CLASSIFICATION_DELAY = 1800L

        /** 아이콘이 선택되지 않은 경우 사용할 기본 아이콘 */
        private const val DEFAULT_ICON = "📚"
    }
}

/**
 * 화면에서 사용하는 CommunityCategory를
 * 스레드 생성 API 요청 값으로 변환합니다.
 */
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

/**
 * AI가 반환한 카테고리 문자열을
 * 화면에서 사용하는 CommunityCategory로 변환합니다.
 *
 * 알 수 없는 값은 FREE로 처리합니다.
 */
private fun String.toCommunityCategory(): CommunityCategory {
    return when (this) {
        "STUDY" -> CommunityCategory.STUDY
        "PROJECT" -> CommunityCategory.PROJECT
        "QNA" -> CommunityCategory.QNA
        "FREE" -> CommunityCategory.FREE
        else -> CommunityCategory.FREE
    }
}