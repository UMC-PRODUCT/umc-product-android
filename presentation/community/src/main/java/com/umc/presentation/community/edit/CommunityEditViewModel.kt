package com.umc.presentation.community.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.community.CommunityThreadCategory
import com.umc.domain.model.community.CommunityThreadMember
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.home.ParticipantItem
import com.umc.domain.usecase.ai.CommunityCategoryRules
import com.umc.domain.usecase.community.DeleteCommunityThreadUseCase
import com.umc.domain.usecase.community.GetCommunityThreadDetailUseCase
import com.umc.domain.usecase.community.GetCommunityThreadMembersUseCase
import com.umc.domain.usecase.community.SearchCommunityCreateMembersUseCase
import com.umc.domain.usecase.community.UpdateCommunityThreadUseCase
import com.umc.presentation.community.model.CommunityAiState
import com.umc.presentation.community.model.CommunityCategory
import com.umc.presentation.community.model.CommunityChallengerUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch



/**
 * 커뮤니티 스레드 수정 화면의 상태와 비즈니스 로직을 관리하는 ViewModel
 *
 * 주요 기능
 * - 스레드 상세 정보 조회
 * - 현재 참여 중인 챌린저 조회
 * - 제목 및 스레드 특징 수정
 * - AI 카테고리 재분류
 * - 챌린저 추가/삭제 후 목록 갱신
 * - 스레드 수정 및 삭제
 * - 화면 이동 및 Toast 이벤트 전달
 */
@HiltViewModel
class CommunityEditViewModel @Inject constructor(
    private val getCommunityThreadDetailUseCase:
    GetCommunityThreadDetailUseCase,
    private val getCommunityThreadMembersUseCase:
    GetCommunityThreadMembersUseCase,
    private val searchCommunityCreateMembersUseCase:
    SearchCommunityCreateMembersUseCase,
    private val updateCommunityThreadUseCase:
    UpdateCommunityThreadUseCase,
    private val deleteCommunityThreadUseCase:
    DeleteCommunityThreadUseCase,
) : ViewModel() {

    private var classificationJob: Job? = null
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

                // 추가/삭제 후 수정 화면의 챌린저 목록도 다시 조회
                loadCurrentMembers(
                    threadId = _state.value.threadId,
                )

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

    /**
     * 수정 화면 최초 진입
     */
    fun loadThread(
        threadId: String,
    ) {
        if (threadId.isBlank()) {
            // 로딩이 기본값(true)이라 조회 없이 끝나는 경우에도 로딩을 해제한다.
            _state.update {
                it.copy(
                    isLoading = false,
                )
            }

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
                if (thread.myRole != com.umc.domain.model.community.CommunityThreadRole.OWNER || thread.deletedAt != null) {
                    _state.update { it.copy(isLoading = false, canEdit = false, errorMessage = "스레드를 수정할 권한이 없어요.") }
                    sendEvent(CommunityEditEvent.ShowToast("스레드를 수정할 권한이 없어요."))
                    return@onSuccess
                }
                _state.update {
                    it.copy(
                        threadId = thread.threadId,
                        canEdit = true,
                        title = thread.title,
                        description = thread.description,
                        currentChallengerCount = thread.memberCount,
                        maxChallengerCount = thread.maxMembers,
                        aiState = CommunityAiState.SUCCESS,
                        classifiedCategory =
                            thread.category.toUiCategory(),
                        selectedIcon = thread.icon,
                        isLoading = false,
                        errorMessage = null,
                    )
                }

                // 상세 API에는 멤버 이름 목록이 없으므로
                // 현재 스레드 멤버를 별도로 조회
                loadCurrentMembers(
                    threadId = thread.threadId,
                )
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

    /**
     * 현재 스레드에 참여 중인 챌린저 조회
     *
     * OWNER는 챌린저 명단에서 제외
     */
    private fun loadCurrentMembers(
        threadId: String,
    ) {
        if (threadId.isBlank()) {
            return
        }

        viewModelScope.launch {
            val memberPage = getCommunityThreadMembersUseCase(
                threadId = threadId,
                query = null,
                role = null,
                part = null,
                generation = null,
                offset = FIRST_OFFSET,
                limit = CURRENT_MEMBER_PAGE_SIZE,
            ).getOrElse {
                return@launch
            }

            val members = memberPage.items
                .filterNot { member ->
                    member.role.equals(
                        OWNER_ROLE,
                        ignoreCase = true,
                    )
                }
                .map { threadMember ->
                    findChallengerDetail(
                        threadMember = threadMember,
                    )
                }
                .distinctBy { member ->
                    member.memberId
                }

            _state.update {
                it.copy(
                    selectedChallengers = members.toImmutableList(),
                    currentChallengerCount = members.size,
                )
            }
        }
    }

    /**
     * 스레드 멤버 정보에 없는 학교/닉네임 등의 정보를
     * 챌린저 검색 API를 통해 보완
     */
    private suspend fun findChallengerDetail(
        threadMember: CommunityThreadMember,
    ): CommunityChallengerUiModel {
        val targetMemberId =
            threadMember.memberId.toLongOrNull()

        if (targetMemberId == null) {
            return threadMember.toFallbackUiModel()
        }

        return when (
            val response = searchCommunityCreateMembersUseCase(
                cursor = null,
                size = MEMBER_MATCH_PAGE_SIZE,
                keyword = threadMember.name,
            )
        ) {
            is ApiState.Success -> {
                response.data.content
                    .firstOrNull { participant ->
                        participant.id == targetMemberId
                    }
                    ?.toCommunityChallengerUiModel()
                    ?: threadMember.toFallbackUiModel()
            }

            is ApiState.Fail -> {
                threadMember.toFallbackUiModel()
            }
        }
    }

    /**
     * 제목 수정은 AI 재분류 상태에 영향 X
     */
    private fun updateTitle(
        title: String,
    ) {
        classificationJob?.cancel()
        _state.update {
            it.copy(
                aiState = if (it.aiState == CommunityAiState.LOADING) CommunityAiState.NEEDS_RECLASSIFICATION else it.aiState,
                title = title,
            )
        }
    }

    /**
     * 스레드 특징을 수정했을 때만 재분류 필요 상태로 변경
     */
    private fun updateDescription(
        description: String,
    ) {
        classificationJob?.cancel()
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

        classificationJob?.cancel()
        classificationJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    aiState = CommunityAiState.LOADING,
                )
            }

            delay(CLASSIFICATION_DELAY)

            val latestState = _state.value
            if (!latestState.canEdit) return@launch

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
        // 생성 화면과 같은 신호어 규칙을 먼저 적용한다. 기기 AI 는 쓰지 않는다.
        CommunityCategoryRules.classify(title, description)?.let { code ->
            return ClassificationResult(
                aiState = CommunityAiState.SUCCESS,
                category = code.toCommunityCategory(),
            )
        }

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

        // 저장 요청 중(성공 후 화면이 닫히기 전 포함)의 추가 탭은 무시한다.
        if (currentState.isSaving) {
            return
        }

        if (!currentState.isSaveEnabled) {
            sendEvent(
                CommunityEditEvent.ShowToast(
                    message = "제목 1~80자·특징 1~500자와 수정 권한을 확인해주세요.",
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
                title = currentState.title.trim(),
                description = currentState.description.trim(),
                category = category.name,
                icon = currentState.selectedIcon,
            ).onSuccess {

                // 성공 후에는 화면이 닫힐 때까지 isSaving 을 유지해 완료 버튼 재활성(PATCH 중복)을 막는다.
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
        if (!_state.value.canEdit) return
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

        if (!currentState.canEdit || currentState.isDeleting) {
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

                // 성공 후에는 화면이 닫힐 때까지 isDeleting 을 유지해 DELETE 중복 전송을 막는다.
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

        private const val FIRST_OFFSET = 0
        private const val CURRENT_MEMBER_PAGE_SIZE = 100
        private const val MEMBER_MATCH_PAGE_SIZE = 100
        private const val OWNER_ROLE = "OWNER"
    }
}

/**
 * 분류 규칙 코드 → UI 카테고리 (알 수 없는 값은 FREE)
 */
private fun String.toCommunityCategory():
        CommunityCategory {
    return when (this) {
        "STUDY" -> CommunityCategory.STUDY
        "PROJECT" -> CommunityCategory.PROJECT
        "QNA" -> CommunityCategory.QNA
        else -> CommunityCategory.FREE
    }
}

/**
 * 상세 API 카테고리 → UI 카테고리
 */
private fun CommunityThreadCategory.toUiCategory():
        CommunityCategory {
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

/**
 * 챌린저 검색 결과 → 커뮤니티 UI 모델
 */
private fun ParticipantItem.toCommunityChallengerUiModel():
        CommunityChallengerUiModel {
    return CommunityChallengerUiModel(
        memberId = id,
        name = name,
        nickname = nickname,
        school = school,
        generation = gisu,
        partLabel = userPart.label,
        profileImage = profileImage,
    )
}

/**
 * 챌린저 상세 매칭 실패 시 스레드 멤버 정보로 대체
 */
private fun CommunityThreadMember.toFallbackUiModel():
        CommunityChallengerUiModel {
    return CommunityChallengerUiModel(
        memberId = memberId.toLongOrNull() ?: 0L,
        name = name,
        nickname = "",
        school = "",
        generation = generation.toLongOrNull() ?: 0L,
        partLabel = UserPart.from(part).label,
        profileImage = "",
    )
}
