package com.umc.presentation.community.bottomsheet.edit

import com.umc.domain.repository.community.CommunityThreadRepository
import com.umc.domain.model.community.thread.CommunityThreadRole
import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.community.CommunityThreadMember
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.home.ParticipantItem
import com.umc.domain.usecase.community.GetCommunityThreadMembersUseCase
import com.umc.domain.usecase.community.SearchCommunityCreateMembersUseCase
import com.umc.domain.usecase.member.GetMyProfileUseCase
import com.umc.presentation.community.model.CommunityChallengerUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch

/**
 * 커뮤니티 스레드 멤버 관리 BottomSheet의 상태와 로직을 관리하는 ViewModel입니다.
 *
 * 주요 기능
 * - 현재 스레드 멤버 조회
 * - 챌린저 이름 검색
 * - 검색 결과 페이지네이션
 * - 현재 로그인한 본인 검색 결과 제외
 * - 스레드 OWNER 검색 결과 제외
 * - 챌린저 선택/해제
 * - 새 멤버 초대
 * - 멤버 변경사항 비교 및 반영
 */
@HiltViewModel
class CommunityMemberBottomSheetViewModel @Inject constructor(
    private val threadRepository: CommunityThreadRepository,
    private val getCommunityThreadMembersUseCase:
    GetCommunityThreadMembersUseCase,
    private val searchCommunityCreateMembersUseCase:
    SearchCommunityCreateMembersUseCase,
    private val getMyProfileUseCase:
    GetMyProfileUseCase,
) : BaseViewModel<
        CommunityMemberBottomSheetState,
        CommunityMemberBottomSheetEvent,
        >(
    CommunityMemberBottomSheetState()
) {

    /** 검색 디바운스 처리를 위한 Job */
    private var searchJob: Job? = null

    /**
     * 초대 가능 멤버 조회 Job
     *
     * 본인 학교 회원만 남기려고 서버 페이지를 여러 번 받을 수 있어,
     * 검색어가 바뀌거나 검색을 닫으면 진행 중인 조회를 바로 멈춥니다.
     */
    private var loadJob: Job? = null
    private var sessionGeneration = 0

    /**
     * 현재 로그인한 사용자의 memberId
     *
     * 로그인한 본인은 초대 대상이 아니므로
     * 챌린저 검색 결과에서 제외하기 위해 사용합니다.
     */
    private var myMemberId: Long? = null

    init {
        loadMyProfile()
    }

    /**
     * 현재 로그인한 사용자의 프로필 정보를 조회합니다.
     *
     * 조회된 memberId는 챌린저 검색 시
     * 로그인한 본인을 검색 결과에서 제외하는 기준값으로 사용합니다.
     */
    private fun loadMyProfile() {
        viewModelScope.launch {
            when (val response = getMyProfileUseCase()) {
                is ApiState.Success -> {
                    myMemberId = response.data.id
                }

                is ApiState.Fail -> Unit
            }
        }
    }

    /**
     * BottomSheet 최초 진입 시 초기 데이터를 설정합니다.
     *
     * threadId가 유효한 경우 현재 스레드에 참여 중인 멤버를 조회합니다.
     * threadId가 비어 있다면 API를 호출하지 않고 안내 Toast를 표시합니다.
     */
    fun initialize(
        threadId: String,
    ) {
        sessionGeneration++
        if (threadId.isBlank()) {
            emitEvent(
                CommunityMemberBottomSheetEvent.ShowToast(
                    message = "스레드 정보를 확인할 수 없어요.",
                )
            )
            return
        }

        searchJob?.cancel()
        loadJob?.cancel()

        updateState {
            CommunityMemberBottomSheetState(
                threadId = threadId,
                isLoading = true,
            )
        }

        loadCurrentMembers(
            threadId = threadId,
        )
    }

    /**
     * 현재 스레드에 참여 중인 멤버를 조회합니다.
     *
     * OWNER는 일반 멤버 관리 대상에서 제외하고,
     * OWNER의 memberId는 별도로 저장하여 검색 결과에서도 제외합니다.
     *
     * 일반 멤버는 챌린저 검색 API와 memberId를 매칭하여
     * 프로필 이미지, 학교, 닉네임 등의 상세 정보를 보완합니다.
     */
    private fun loadCurrentMembers(
        threadId: String,
    ) = viewModelScope.launch {
            val generation = sessionGeneration
            val detail = threadRepository.getThread(threadId)
            if (generation != sessionGeneration) return@launch
            if (detail !is ApiState.Success || detail.data.myRole !in setOf(CommunityThreadRole.OWNER, CommunityThreadRole.ADMIN)) {
                updateState { copy(isLoading = false, canManage = false, currentMembers = persistentListOf(), selectedMembers = persistentListOf(), errorMessage = "멤버를 관리할 권한이 없어요.") }
                return@launch
            }
            updateState {
                copy(
                    canManage = true,
                    maxMemberCount = ((detail.data.maxMembers.toIntOrNull() ?: 1) - 1)
                        .coerceIn(0, INVITE_MEMBER_LIMIT),
                )
            }
            val memberPage = getCommunityThreadMembersUseCase(
                threadId = threadId,
                query = null,
                role = null,
                part = null,
                generation = null,
                offset = FIRST_OFFSET,
                limit = CURRENT_MEMBER_PAGE_SIZE,
            ).getOrElse {
                if (generation != sessionGeneration) return@launch
                // 예외 메시지는 파싱 오류 원문 등이 그대로 들어올 수 있어 화면에 노출하지 않음
                updateState {
                    copy(
                        isLoading = false,
                        canManage = false,
                        currentMembers = persistentListOf(), selectedMembers = persistentListOf(),
                        errorMessage = "현재 스레드 멤버를 불러오지 못했어요.",
                    )
                }
                return@launch
            }

            /**
             * OWNER의 memberId 목록
             *
             * OWNER는 검색을 통해 다시 추가할 수 없도록
             * 이후 검색 결과 필터링에 사용합니다.
             */
            if (generation != sessionGeneration) return@launch
            val ownerMemberIds = memberPage.items
                .filter { member ->
                    member.role.equals(
                        OWNER_ROLE,
                        ignoreCase = true,
                    )
                }
                .mapNotNull { member ->
                    member.memberId.toLongOrNull()
                }
                .toImmutableSet()

            /**
             * OWNER를 제외한 현재 참여 멤버 목록
             *
             * 스레드 멤버 조회 API에는 일부 화면 정보가 없을 수 있으므로
             * 검색 API를 이용해 상세 정보를 다시 매칭합니다.
             */
            val currentMembers = memberPage.items
                .filter { it.state.equals("ACTIVE", ignoreCase = true) }
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
                .toImmutableList()

            if (generation != sessionGeneration) return@launch
            updateState {
                copy(
                    currentMembers = currentMembers,
                    ownerMemberIds = ownerMemberIds,

                    // 검색 화면 최초 진입 시 현재 멤버를 체크 상태로 표시하기 위해 저장
                    selectedMembers = currentMembers,

                    isLoading = false,
                    errorMessage = null,
                )
            }
    }

    /**
     * 현재 스레드 멤버의 상세 챌린저 정보를 조회합니다.
     *
     * 스레드 멤버의 이름으로 챌린저 검색 API를 호출한 뒤,
     * 검색 결과 중 memberId가 정확히 일치하는 멤버를 사용합니다.
     *
     * 매칭 성공 시:
     * - 이름
     * - 닉네임
     * - 학교
     * - 기수
     * - 파트
     * - 프로필 이미지
     *
     * 등의 상세 정보를 사용할 수 있습니다.
     *
     * 검색 결과와 정확히 매칭되지 않거나 API 호출이 실패하면
     * 스레드 멤버 조회 응답을 기반으로 fallback UI 모델을 생성합니다.
     */
    private suspend fun findChallengerDetail(
        threadMember: CommunityThreadMember,
    ): CommunityChallengerUiModel {
        val targetMemberId =
            threadMember.memberId.toLongOrNull()

        // memberId를 Long으로 변환할 수 없는 경우 검색 매칭 없이 fallback 사용
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
     * 챌린저 검색어가 변경될 때 호출됩니다.
     *
     * 연속 입력마다 API가 호출되지 않도록
     * 300ms 디바운스를 적용합니다.
     *
     * 검색 화면에 처음 진입하면 현재 스레드 멤버를
     * 모두 선택된 상태로 설정하여 체크박스에 반영합니다.
     */
    fun searchMembers(
        query: String,
    ) {
        searchJob?.cancel()
        loadJob?.cancel()

        val trimmedQuery = query.trim()
        val currentState = uiState.value

        // 검색어가 비어 있으면 검색 화면만 종료하고 현재 멤버 상태로 복구
        if (trimmedQuery.isBlank()) {
            clearSearchOnly()
            return
        }

        updateState {
            copy(
                query = query,
                isSearching = true,

                /**
                 * 검색 화면 최초 진입 시
                 * 기존 스레드 멤버 전체를 체크 상태로 설정합니다.
                 *
                 * 이후 검색어 변경 시에는 사용자가 선택/해제한 상태를 유지합니다.
                 */
                selectedMembers = if (!currentState.isSearching) {
                    currentMembers
                } else {
                    selectedMembers
                },

                isLoading = true,
                isLoadingMore = false,
                searchResults = persistentListOf(),
                nextCursor = null,
                hasNext = false,
                errorMessage = null,
            )
        }

        searchJob = viewModelScope.launch {
            delay(SEARCH_DELAY)

            loadMembers(
                keyword = trimmedQuery,
                cursor = null,
                append = false,
            )
        }
    }

    /**
     * 챌린저 검색 결과의 다음 페이지를 조회합니다.
     *
     * 다음 cursor가 없거나,
     * 이미 데이터를 불러오는 중이거나,
     * 다음 페이지가 존재하지 않으면 추가 호출하지 않습니다.
     */
    fun loadMoreMembers() {
        val currentState = uiState.value
        val nextCursor = currentState.nextCursor ?: return

        if (
            currentState.isLoading ||
            currentState.isLoadingMore ||
            !currentState.hasNext ||
            currentState.query.isBlank()
        ) {
            return
        }

        loadMembers(
            keyword = currentState.query.trim(),
            cursor = nextCursor,
            append = true,
        )
    }

    /**
     * 챌린저 검색 API를 호출합니다.
     *
     * 생성 바텀시트와 동일한 챌린저 검색 API를 사용합니다.
     *
     * 검색 결과에서는 아래 사용자를 제외합니다.
     * - 스레드 OWNER
     * - 현재 로그인한 본인
     *
     * 기존 스레드 멤버는 초대 검색 결과에서 제외합니다.
     *
     * @param keyword 검색할 챌린저 이름
     * @param cursor 다음 페이지 조회용 cursor. 최초 조회 시 null
     * @param append true면 기존 검색 결과 뒤에 추가,
     * false면 새로운 검색 결과로 교체
     */
    private fun loadMembers(
        keyword: String,
        cursor: Long?,
        append: Boolean,
    ) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            updateState {
                if (append) {
                    copy(
                        isLoadingMore = true,
                        errorMessage = null,
                    )
                } else {
                    copy(
                        isLoading = true,
                        errorMessage = null,
                    )
                }
            }

            val generation = sessionGeneration
            val threadId = uiState.value.threadId
            var offset = cursor?.toInt() ?: 0
            var appendPage = append
            while (true) {
                val result = threadRepository.getInvitableMembers(
                    threadId = threadId, query = keyword, offset = offset, limit = PAGE_SIZE,
                )
                // 네트워크 호출부가 취소를 실패 응답으로 바꿔 돌려주므로, 취소된 조회가 상태를 덮어쓰지 않게 여기서 멈춘다
                ensureActive()
                if (generation != sessionGeneration || uiState.value.threadId != threadId || uiState.value.query.trim() != keyword) return@launch
                when (result) {
                    is ApiState.Success -> {
                        val page = result.data
                        val candidates = page.items.mapNotNull { member ->
                            val id = member.memberId.toLongOrNull() ?: return@mapNotNull null
                            if (id == myMemberId || id in uiState.value.ownerMemberIds) return@mapNotNull null
                            CommunityChallengerUiModel(
                                memberId = id, name = member.name, nickname = "", school = "",
                                generation = member.generation?.toLongOrNull() ?: 0L,
                                partLabel = UserPart.from(member.part.orEmpty()).label, profileImage = "",
                            )
                        }
                        val nextOffset = page.nextOffset?.toIntOrNull()
                        // 본인 학교만 남기느라 이번 페이지에 보여줄 사람이 없으면, 빈 목록에서는 스크롤로 다음 페이지를 부를 수 없으므로 바로 이어 받는다
                        val continueScan = candidates.isEmpty() && nextOffset != null
                        updateState {
                            copy(
                                searchResults = ((if (appendPage) searchResults else emptyList()) + candidates)
                                    .distinctBy { it.memberId }.toImmutableList(),
                                nextCursor = page.nextOffset?.toLongOrNull(), hasNext = page.nextOffset != null,
                                isLoading = if (continueScan) isLoading else false,
                                isLoadingMore = if (continueScan) isLoadingMore else false,
                            )
                        }
                        if (!continueScan) return@launch
                        offset = nextOffset ?: return@launch
                        appendPage = true
                    }
                    is ApiState.Fail -> {
                        updateState { copy(isLoading = false, isLoadingMore = false, errorMessage = "초대 가능한 멤버를 불러오지 못했어요.") }
                        if (result.failState.code in setOf("403", "409", "COMMUNITY-0034") ||
                            result.failState.code.endsWith("-403") || result.failState.code.endsWith("-409")) {
                            loadCurrentMembers(threadId)
                        }
                        return@launch
                    }
                }
            }
        }
    }

    /**
     * 챌린저를 선택하거나 선택 해제합니다.
     *
     * - 선택된 챌린저를 누르면 선택 해제
     * - 선택되지 않은 챌린저를 누르면 선택
     * - 최대 선택 가능 인원을 초과하면 Toast 표시
     * - 현재 로그인한 본인은 선택 불가
     * - 스레드 OWNER는 선택 불가
     */
    fun toggleMember(
        member: CommunityChallengerUiModel,
    ) {
        val currentState = uiState.value
        if (!currentState.canManage || currentState.isUpdatingMembers) return

        // 검색 필터 외의 경로로 전달되더라도 본인과 OWNER 선택 방지
        if (
            member.memberId == myMemberId ||
            member.memberId in currentState.ownerMemberIds ||
            currentState.currentMembers.any { it.memberId == member.memberId }
        ) {
            return
        }

        val isSelected =
            currentState.selectedMembers.any { selectedMember ->
                selectedMember.memberId == member.memberId
            }

        // 최대 선택 가능 인원을 초과하는 추가 선택 방지
        if (
            !isSelected &&
            currentState.selectedMembers.size >=
            currentState.maxMemberCount
        ) {
            emitEvent(
                CommunityMemberBottomSheetEvent.ShowToast(
                    message =
                        "최대 ${currentState.maxMemberCount}명까지 초대할 수 있습니다.",
                )
            )
            return
        }

        updateState {
            val updatedMembers = if (isSelected) {
                selectedMembers.filterNot { selectedMember ->
                    selectedMember.memberId == member.memberId
                }
            } else {
                selectedMembers + member
            }

            copy(
                selectedMembers = updatedMembers
                    .distinctBy { selectedMember ->
                        selectedMember.memberId
                    }
                    .toImmutableList(),
            )
        }
    }

    /**
     * 검색 화면에서 확인 버튼을 눌렀을 때
     * 현재 멤버와 최종 선택된 멤버를 비교하여 변경사항을 적용합니다.
     *
     * - 새롭게 선택된 멤버 → invite API 호출
     * 기존 멤버는 유지하고 새롭게 선택한 멤버만 초대합니다.
     */
    fun updateMembers() {
        val currentState = uiState.value

        // 이미 멤버 변경 요청이 진행 중이라면 중복 요청 방지
        if (!currentState.canManage || currentState.isUpdatingMembers) {
            return
        }

        // 실제 멤버 변경사항이 없다면 API 요청 없이 검색 화면 종료
        if (!currentState.hasMemberChanges) {
            clearSearchOnly()
            return
        }

        val generation = sessionGeneration
        val addedMembers = currentState.addedMembers

        viewModelScope.launch {
            updateState {
                copy(
                    isUpdatingMembers = true,
                    errorMessage = null,
                )
            }

            var addedCount = 0
            var failedCount = 0
            val failedNames = mutableListOf<String>()
            if (addedMembers.isNotEmpty()) {
                when (val result = threadRepository.inviteMembers(currentState.threadId, addedMembers.map { it.memberId })) {
                    is ApiState.Success -> {
                        addedCount = result.data.invitedMembers.size
                        val invitedIds = result.data.invitedMembers.map { it.memberId }.toSet()
                        val failed = addedMembers.filter { it.memberId.toString() !in invitedIds }
                        failedCount += failed.size
                        failedNames += failed.map { it.name }
                    }
                    is ApiState.Fail -> { failedCount += addedMembers.size; failedNames += addedMembers.map { it.name } }
                }
            }
            if (generation != sessionGeneration) return@launch
            // Includes role/capacity revalidation after 403/409 or partial success.
            loadCurrentMembers(currentState.threadId).join()
            updateState { copy(isUpdatingMembers = false, query = "", isSearching = false, searchResults = persistentListOf()) }
            // 일부만 초대되어도 상위 수정 화면과 채팅방의 참여자 정보를 갱신한다.
            if (addedCount > 0 || failedCount == 0) {
                emitEvent(CommunityMemberBottomSheetEvent.MemberUpdateSuccess(addedCount))
            }
            if (failedCount > 0) {
                emitEvent(CommunityMemberBottomSheetEvent.ShowToast(
                    "초대 ${addedCount}명 완료, ${failedCount}명 미완료(${failedNames.joinToString()}). 현재 멤버 목록을 다시 확인해주세요."
                ))
            }
        }
    }

    /**
     * 검색 상태만 초기화합니다.
     *
     * 검색어와 검색 결과를 비우고,
     * 선택 목록은 현재 스레드 멤버 목록으로 되돌립니다.
     *
     * BottomSheet 전체 상태는 유지됩니다.
     */
    fun clearSearchOnly() {
        searchJob?.cancel()
        loadJob?.cancel()

        updateState {
            copy(
                query = "",
                isSearching = false,
                selectedMembers = currentMembers,
                searchResults = persistentListOf(),
                isLoading = false,
                isLoadingMore = false,
                nextCursor = null,
                hasNext = false,
                errorMessage = null,
            )
        }
    }

    /**
     * BottomSheet가 닫힐 때 전체 상태를 초기화합니다.
     *
     * 진행 중인 검색 Job도 함께 취소합니다.
     */
    fun resetAfterDismiss() {
        sessionGeneration++
        searchJob?.cancel()
        loadJob?.cancel()

        updateState {
            CommunityMemberBottomSheetState()
        }
    }

    companion object {
        // 방장을 제외한 기존 참여자와 새 초대 대상의 최대 합계.
        private const val INVITE_MEMBER_LIMIT = 99

        /** 챌린저 검색 API 한 페이지 조회 개수 */
        private const val PAGE_SIZE = 20

        /**
         * 현재 스레드 멤버의 상세 정보를 검색 API와 매칭할 때
         * 한 번에 조회하는 최대 챌린저 수
         */
        private const val MEMBER_MATCH_PAGE_SIZE = 100

        /** 현재 스레드 멤버 조회 시 한 번에 요청하는 최대 멤버 수 */
        private const val CURRENT_MEMBER_PAGE_SIZE = 100

        /** 현재 멤버 목록 최초 조회 offset */
        private const val FIRST_OFFSET = 0

        /** 검색 API 호출 전 적용하는 디바운스 시간 */
        private const val SEARCH_DELAY = 300L

        /** 스레드 생성자 역할을 구분하기 위한 role 값 */
        private const val OWNER_ROLE = "OWNER"
    }
}

/**
 * 챌린저 검색 API의 ParticipantItem을
 * 멤버 관리 BottomSheet에서 사용하는 UI 모델로 변환합니다.
 *
 * profileImage도 함께 전달하므로
 * 검색 결과와 현재 멤버 목록에서 실제 프로필 이미지를 표시할 수 있습니다.
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
 * 현재 스레드 멤버와 챌린저 검색 결과를 매칭하지 못했을 때
 * 사용하는 기본 UI 모델입니다.
 *
 * CommunityThreadMember에는 프로필 이미지, 학교, 닉네임 등의
 * 상세 정보가 없기 때문에 해당 값은 기본값으로 설정합니다.
 *
 * profileImage가 빈 문자열이므로 UI에서는 기본 프로필 이미지를 표시합니다.
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
