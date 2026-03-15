package com.umc.presentation.ui.notice.detail

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.UDomainFormat.parseDateTime
import com.umc.domain.model.UserInfo
import com.umc.domain.model.notice.ChallengerReadInfo
import com.umc.domain.model.notice.NoticeDetail
import com.umc.domain.model.notice.NoticeReadStatistics
import com.umc.domain.model.notice.NoticeTarget
import com.umc.domain.model.notice.NoticeVote
import com.umc.domain.model.notice.NoticeVoteOption
import com.umc.domain.model.notice.NoticeVoteParticipant
import com.umc.domain.model.enums.UserChallengerRole
import com.umc.domain.usecase.GetChallengerIdUseCase
import com.umc.domain.usecase.GetGisuInfoUseCase
import com.umc.domain.usecase.appDataStore.GetUserInfoUseCase
import com.umc.domain.usecase.member.GetMemberProfileUseCase
import com.umc.domain.usecase.notice.DeleteNoticeUseCase
import com.umc.domain.usecase.notice.GetNoticeDetailUseCase
import com.umc.domain.usecase.notice.GetNoticeReadStatisticsUseCase
import com.umc.domain.usecase.notice.GetNoticeReadStatusUseCase
import com.umc.domain.usecase.notice.SendNoticeReminderUseCase
import com.umc.domain.usecase.notice.SubmitVoteResponseUseCase
import com.umc.domain.usecase.notice.UpdateVoteResponseUseCase
import com.umc.domain.usecase.organization.GetChapterDetailUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import com.umc.presentation.util.ULog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoticeDetailViewModel @Inject constructor(
    private val getNoticeDetailUseCase: GetNoticeDetailUseCase,
    private val getNoticeReadStatusUseCase: GetNoticeReadStatusUseCase,
    private val getNoticeReadStatisticsUseCase: GetNoticeReadStatisticsUseCase,
    private val sendNoticeReminderUseCase: SendNoticeReminderUseCase,
    private val submitVoteResponseUseCase: SubmitVoteResponseUseCase,
    private val updateVoteResponseUseCase: UpdateVoteResponseUseCase,
    private val getMemberProfileUseCase: GetMemberProfileUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getChallengerIdUseCase: GetChallengerIdUseCase,
    private val deleteNoticeUseCase: DeleteNoticeUseCase,
    private val getGisuInfoUseCase: GetGisuInfoUseCase,
    private val getChapterDetailUseCase: GetChapterDetailUseCase,
) : BaseViewModel<NoticeFragmentUiState, NoticeFragmentEvent>(
    NoticeFragmentUiState()
) {

    private var currentNoticeId: Long = 0L
    private var selectedOptionIds: MutableSet<Long> = mutableSetOf()
    private var currentUserChallengerId: Long = -1L

    fun init(noticeId: Long) = viewModelScope.launch {
        currentNoticeId = noticeId
        // 공지 상세 로드 완료 후 권한 체크 및 통계 조회
        loadNoticeDetailSync()
    }

    private fun loadNoticeDetailSync() {
        if (currentNoticeId == 0L) {
            emitEvent(NoticeFragmentEvent.ShowError("유효하지 않은 공지사항 ID입니다"))
            return
        }

        updateState { copy(isLoading = true) }
        startLoading()

        viewModelScope.launch {
            val response = getNoticeDetailUseCase(currentNoticeId)
            resultResponse(
                response = response,
                successCallback = { detail ->
                    detail.vote?.mySelectedOptionIds?.let { ids ->
                        selectedOptionIds = ids.toMutableSet()
                    } ?: run {
                        selectedOptionIds = mutableSetOf()
                    }

                    val formattedCreatedAt = getFormattedCreatedAt(detail.createdAt)
                    val formattedVoteCondition = getFormattedVoteCondition(detail.vote)

                    // chapterId가 있으면 chapter 정보를 조회하여 이름 가져오기
                    val targetChapterId = detail.targetInfo.targetChapterId
                    if (targetChapterId != null) {
                        fetchChapterAndUpdateDetailSync(targetChapterId.toLong(), detail, formattedCreatedAt, formattedVoteCondition)
                    } else {
                        updateDetailState(detail, formattedCreatedAt, formattedVoteCondition, null)
                    }
                },
                errorCallback = {
                    updateState { copy(isLoading = false) }
                    emitEvent(NoticeFragmentEvent.ShowError("공지사항을 불러오는데 실패했습니다"))
                }
            )
        }
    }

    private fun fetchChapterAndUpdateDetailSync(
        chapterId: Long,
        detail: NoticeDetail,
        formattedCreatedAt: String,
        formattedVoteCondition: String,
    ) = viewModelScope.launch {
        val response = getChapterDetailUseCase(chapterId)
        resultResponse(
            response = response,
            successCallback = { chapter ->
                // chapter 이름을 targetInfo에 설정
                val updatedDetail = detail.copy(
                    targetInfo = detail.targetInfo.copy(
                        targetChapterName = chapter.name
                    )
                )
                updateDetailState(updatedDetail, formattedCreatedAt, formattedVoteCondition, null)
            },
            errorCallback = {
                // chapter 조회 실패 시 원본 detail 사용
                updateDetailState(detail, formattedCreatedAt, formattedVoteCondition, null)
            }
        )
    }

    private fun loadCurrentUserInfoAndCheckPermission(targetInfo: NoticeTarget) = viewModelScope.launch {
        val userInfo = getUserInfoUseCase().first()
        currentUserChallengerId = getChallengerIdUseCase()
        checkIsMember(userInfo)
        checkIsAuthor()
        
        // 권한 체크 후 통계/현황 조회
        if (hasAdminPermission(userInfo, targetInfo)) {
            loadReadStatistics()
            loadReadStatus()
            loadUnReadStatus()
        }
    }

    private fun hasAdminPermission(userInfo: UserInfo, targetInfo: NoticeTarget): Boolean {
        ULog.d("[hasAdminPermission] 시작 - userId: ${userInfo.id}, roles: ${userInfo.roles.map { it.roleType }}")
        ULog.d("[hasAdminPermission] targetInfo - chapterId: ${targetInfo.targetChapterId}, schoolId: ${targetInfo.targetSchoolId}, gisuId: ${targetInfo.targetGisuId}, parts: ${targetInfo.targetParts}")

        // 1. 총괄단(SUPER_ADMIN)은 모든 공지에 대해 수신 조회 가능
        val isSuperAdmin = userInfo.roles.any {
            it.roleType == UserChallengerRole.SUPER_ADMIN.name ||
                    it.roleType == UserChallengerRole.CENTRAL_PRESIDENT.name ||
                    it.roleType == UserChallengerRole.CENTRAL_VICE_PRESIDENT.name
        }
        ULog.d("[hasAdminPermission] 1. 총괄단 체크 - isSuperAdmin: $isSuperAdmin")
        if (isSuperAdmin) {
            ULog.d("[hasAdminPermission] ✓ 총괄단 권한으로 통과")
            return true
        }

        val targetChapterId = targetInfo.targetChapterId
        val targetSchoolId = targetInfo.targetSchoolId
        val targetGisuId = targetInfo.targetGisuId
        val hasPart = !targetInfo.targetParts.isNullOrEmpty()

        ULog.d("[hasAdminPermission] 공지 타입 분석 - hasPart: $hasPart, targetChapterId: $targetChapterId, targetSchoolId: $targetSchoolId, targetGisuId: $targetGisuId")

        // 2. 기수파트공지 (gisuId != 0 && part가 비어있지 않음) -> 중앙운영진
        if (targetGisuId != 0 && hasPart) {
            ULog.d("[hasAdminPermission] 2. 기수파트공지 체크 시작")
            val centralTeamRoles = setOf(
                UserChallengerRole.CENTRAL_OPERATING_TEAM_MEMBER.name,
                UserChallengerRole.CENTRAL_EDUCATION_TEAM_MEMBER.name
            )
            val hasPermission = userInfo.roles.any { role ->
                val matches = centralTeamRoles.contains(role.roleType) && role.gisuId == targetGisuId.toLong()
                ULog.d("[hasAdminPermission]    기수파트공지 체크 - role: ${role.roleType}, gisuId: ${role.gisuId}, targetGisuId: $targetGisuId, matches: $matches")
                matches
            }
            ULog.d("[hasAdminPermission] 2. 기수파트공지 결과 - hasPermission: $hasPermission")
            if (hasPermission) {
                ULog.d("[hasAdminPermission] ✓ 기수파트공지 권한으로 통과")
                return true
            }
        }

        // 3. 지부파트공지 (chapterId != null && part가 비어있지 않음) -> 해당 지부장
        if (targetChapterId != null && hasPart) {
            ULog.d("[hasAdminPermission] 3. 지부파트공지 체크 시작 - targetChapterId: $targetChapterId")
            val hasPermission = userInfo.roles.any { role ->
                val matches = role.roleType == UserChallengerRole.CHAPTER_PRESIDENT.name && role.chapterId == targetChapterId.toLong()
                ULog.d("[hasAdminPermission]    지부파트공지 체크 - role: ${role.roleType}, chapterId: ${role.chapterId}, matches: $matches")
                matches
            }
            ULog.d("[hasAdminPermission] 3. 지부파트공지 결과 - hasPermission: $hasPermission")
            if (hasPermission) {
                ULog.d("[hasAdminPermission] ✓ 지부파트공지 권한으로 통과")
                return true
            }
        }

        // 4. 학교파트공지 (schoolId != null && part가 비어있지 않음) -> 학교 운영진
        if (targetSchoolId != null && hasPart) {
            ULog.d("[hasAdminPermission] 4. 학교파트공지 체크 시작 - targetSchoolId: $targetSchoolId")
            val schoolAdminRoles = setOf(
                UserChallengerRole.SCHOOL_PRESIDENT.name,
                UserChallengerRole.SCHOOL_VICE_PRESIDENT.name,
                UserChallengerRole.SCHOOL_PART_LEADER.name,
                UserChallengerRole.SCHOOL_ETC_ADMIN.name
            )
            ULog.d("[hasAdminPermission]    schoolAdminRoles: $schoolAdminRoles")
            val hasPermission = userInfo.roles.any { role ->
                val roleMatches = schoolAdminRoles.contains(role.roleType)
                val orgMatches = role.organizationId == targetSchoolId.toLong()
                ULog.d("[hasAdminPermission]    학교파트공지 체크 - role: ${role.roleType}, orgId: ${role.organizationId}, roleMatches: $roleMatches, orgMatches: $orgMatches")
                roleMatches && orgMatches
            }
            ULog.d("[hasAdminPermission] 4. 학교파트공지 결과 - hasPermission: $hasPermission")
            if (hasPermission) {
                ULog.d("[hasAdminPermission] ✓ 학교파트공지 권한으로 통과")
                return true
            }
        }

        // 5. 지부공지 (chapterId != null && part가 비어있음) -> 해당 지부장
        if (targetChapterId != null) {
            ULog.d("[hasAdminPermission] 5. 지부공지 체크 시작 - targetChapterId: $targetChapterId")
            val hasPermission = userInfo.roles.any { role ->
                val matches = role.roleType == UserChallengerRole.CHAPTER_PRESIDENT.name && role.chapterId == targetChapterId.toLong()
                ULog.d("[hasAdminPermission]    지부공지 체크 - role: ${role.roleType}, chapterId: ${role.chapterId}, matches: $matches")
                matches
            }
            ULog.d("[hasAdminPermission] 5. 지부공지 결과 - hasPermission: $hasPermission")
            if (hasPermission) {
                ULog.d("[hasAdminPermission] ✓ 지부공지 권한으로 통과")
                return true
            }
        }

        // 6. 학교공지 (schoolId != null && part가 비어있음) -> 학교 운영진
        if (targetSchoolId != null) {
            ULog.d("[hasAdminPermission] 6. 학교공지 체크 시작 - targetSchoolId: $targetSchoolId")
            val schoolAdminRoles = setOf(
                UserChallengerRole.SCHOOL_PRESIDENT.name,
                UserChallengerRole.SCHOOL_VICE_PRESIDENT.name,
                UserChallengerRole.SCHOOL_PART_LEADER.name,
                UserChallengerRole.SCHOOL_ETC_ADMIN.name
            )
            val hasPermission = userInfo.roles.any { role ->
                val roleMatches = schoolAdminRoles.contains(role.roleType)
                val orgMatches = role.organizationId == targetSchoolId.toLong()
                ULog.d("[hasAdminPermission]    학교공지 체크 - role: ${role.roleType}, orgId: ${role.organizationId}, roleMatches: $roleMatches, orgMatches: $orgMatches")
                roleMatches && orgMatches
            }
            ULog.d("[hasAdminPermission] 6. 학교공지 결과 - hasPermission: $hasPermission")
            if (hasPermission) {
                ULog.d("[hasAdminPermission] ✓ 학교공지 권한으로 통과")
                return true
            }
        }

        // 7. 기수공지(gisuId와 상관없이) -> 중앙운영진들 가능
        ULog.d("[hasAdminPermission] 7. 기수공지/전체공지 체크 시작 - targetGisuId: $targetGisuId")
        val centralTeamRoles = setOf(
            UserChallengerRole.CENTRAL_OPERATING_TEAM_MEMBER.name,
            UserChallengerRole.CENTRAL_EDUCATION_TEAM_MEMBER.name
        )
        val isCentralTeamMember = userInfo.roles.any { role ->
            val roleMatches = centralTeamRoles.contains(role.roleType)
            val gisuMatches = targetGisuId == 0 || role.gisuId == targetGisuId.toLong()
            ULog.d("[hasAdminPermission]    기수공지 체크 - role: ${role.roleType}, gisuId: ${role.gisuId}, roleMatches: $roleMatches, gisuMatches: $gisuMatches")
            roleMatches && gisuMatches
        }
        ULog.d("[hasAdminPermission] 7. 기수공지/전체공지 결과 - isCentralTeamMember: $isCentralTeamMember")
        if (isCentralTeamMember) {
            ULog.d("[hasAdminPermission] ✓ 기수공지/전체공지 권한으로 통과")
            return true
        }

        ULog.d("[hasAdminPermission] ✗ 모든 권한 체크 실패 - 접근 거부")
        return false
    }

    private fun checkIsAuthor() {
        val isAuthor = currentUserChallengerId == uiState.value.detail.authorChallengerId && currentUserChallengerId != -1L
        updateState { copy(isAuthor = isAuthor) }
    }

    private fun checkIsMember(userInfo: UserInfo) {
        // hasAdminPermission과 동일한 로직으로 체크 (반대로)
        val targetInfo = uiState.value.detail.targetInfo
        ULog.d("checkIsMember 에서 보냄")
        val adminPermission = hasAdminPermission(userInfo, targetInfo)
        
        updateState { 
            copy(isCurrentUserMember = !adminPermission) 
        }
    }

    fun onClickMenu() {
        updateState { copy(isMenuVisible = !isMenuVisible) }
    }

    private fun updateDetailState(
        detail: NoticeDetail,
        formattedCreatedAt: String,
        formattedVoteCondition: String,
        gisu: Long?
    ) {
        val targetGisuId = detail.targetInfo.targetGisuId
        if (targetGisuId != null && gisu == null) {
            fetchGisuAndUpdateTargetInfo(targetGisuId.toLong(), detail, formattedCreatedAt, formattedVoteCondition)
        } else {
            val formattedTargetInfo = getFormattedTargetInfo(detail.targetInfo, gisu)
            ULog.d("detail Target ${detail.targetInfo}")
            updateState {
                copy(
                    detail = detail,
                    selectedVoteOptionIds = selectedOptionIds.toList(),
                    isVoteEditMode = false,
                    isLoading = false,
                    formattedCreatedAt = formattedCreatedAt,
                    formattedTargetInfo = formattedTargetInfo,
                    formattedVoteCondition = formattedVoteCondition
                )
            }
            loadCurrentUserInfoAndCheckPermission(detail.targetInfo)
            loadAuthorNickname(detail.authorChallengerId)
        }
    }

    private fun getFormattedCreatedAt(date: String): String {
        return if (date.isNotBlank()) {
            date.parseDateTime().first
        } else ""
    }

    private fun getFormattedTargetInfo(targetInfo: NoticeTarget, gisu: Long?): String {
        val chapterStr = if (targetInfo.targetChapterId != null) "${targetInfo.targetChapterId}장" else "전체"
        val partsStr = if (!targetInfo.targetParts.isNullOrEmpty()) "/${targetInfo.targetParts[0]}" else ""
        val gisuStr = gisu?.let { "${it}기/" } ?: ""
        return "수신대상: ${gisuStr}${chapterStr}${partsStr}"
    }

    private fun fetchGisuAndUpdateTargetInfo(
        gisuId: Long,
        detail: NoticeDetail,
        formattedCreatedAt: String,
        formattedVoteCondition: String
    ) = viewModelScope.launch {
        resultResponse(
            response = getGisuInfoUseCase(gisuId),
            successCallback = { gisuInfo ->
                val formattedTargetInfo = getFormattedTargetInfo(detail.targetInfo, gisuInfo.gisu)
                ULog.d("detail Target2 ${detail.targetInfo}")
                updateState {
                    copy(
                        detail = detail,
                        selectedVoteOptionIds = selectedOptionIds.toList(),
                        isVoteEditMode = false,
                        isLoading = false,
                        formattedCreatedAt = formattedCreatedAt,
                        formattedTargetInfo = formattedTargetInfo,
                        formattedVoteCondition = formattedVoteCondition
                    )
                }
                loadCurrentUserInfoAndCheckPermission(detail.targetInfo)
                loadAuthorNickname(detail.authorChallengerId)
            },
            errorCallback = {
                // gisu 조회 실패 시 targetGisuId로 fallback
                val formattedTargetInfo = getFormattedTargetInfo(detail.targetInfo, null)
                updateState {
                    copy(
                        detail = detail,
                        selectedVoteOptionIds = selectedOptionIds.toList(),
                        isVoteEditMode = false,
                        isLoading = false,
                        formattedCreatedAt = formattedCreatedAt,
                        formattedTargetInfo = formattedTargetInfo,
                        formattedVoteCondition = formattedVoteCondition
                    )
                }
                loadCurrentUserInfoAndCheckPermission(detail.targetInfo)
                loadAuthorNickname(detail.authorChallengerId)
            }
        )
    }

    private fun getFormattedVoteCondition(vote: NoticeVote?): String {
        return vote?.let {
            val choiceType = if (it.allowMultipleChoice) "복수선택" else "단일선택"
            val anonymity = if (it.isAnonymous) "익명" else "실명"
            val startDate = it.startsAt.parseDateTime().first
            val endDate = it.endsAtExclusive.parseDateTime().first
            "${startDate} ~ ${endDate} • ${choiceType} • ${anonymity}"
        } ?: ""
    }

    private fun loadAuthorNickname(authorChallengerId: Long) = viewModelScope.launch {
        if (authorChallengerId <= 0) {
            updateState { copy(authorNickname = "알수없음") }
            return@launch
        }

        startLoading()

        resultResponse(
            response = getMemberProfileUseCase(authorChallengerId),
            successCallback = { memberDetail ->
                updateState { copy(authorNickname = "${memberDetail.nickname}/${memberDetail.name}") }
            },
            errorCallback = {
                updateState { copy(authorNickname = "알수없음") }
            }
        )
    }

    private fun loadReadStatistics() = viewModelScope.launch {
        if (currentNoticeId == 0L) return@launch
        startLoading()

        resultResponse(
            response = getNoticeReadStatisticsUseCase(currentNoticeId),
            successCallback = { statistics ->
                updateState { copy(readStatistics = statistics) }
            },
            errorCallback = {
                // 통계 로드 실패는 침묵으로 처리
            }
        )
    }

    fun loadReadStatus(status: String = "READ", cursorId: Long? = null) = viewModelScope.launch {
        if (currentNoticeId == 0L) return@launch

        updateState { copy(isLoadingReadStatus = true) }
        startLoading()
        resultResponse(
            response = getNoticeReadStatusUseCase(
                noticeId = currentNoticeId,
                status = status,
                filterType = "ALL",
                cursorId = cursorId
            ),
            successCallback = { readStatus ->
                updateState {
                    copy(
                        readStatusList = if (cursorId == null) {
                            readStatus.content
                        } else {
                            readStatusList + readStatus.content
                        },
                        readNextCursor = readStatus.nextCursor,
                        readHasNext = readStatus.hasNext,
                        isLoadingReadStatus = false
                    )
                }
            },
            errorCallback = {
                updateState { copy(isLoadingReadStatus = false) }
                emitEvent(NoticeFragmentEvent.ShowError("확인 현황을 불러오는데 실패했습니다"))
            }
        )
    }

    fun loadMoreReadStatus() {
        val state = uiState.value
        if (state.isLoadingReadStatus || !state.readHasNext) return
        loadReadStatus("READ", state.readNextCursor)
    }

    fun loadUnReadStatus(status: String = "UNREAD", cursorId: Long? = null) = viewModelScope.launch {
        if (currentNoticeId == 0L) return@launch

        updateState { copy(isLoadingReadStatus = true) }
        startLoading()
        resultResponse(
            response = getNoticeReadStatusUseCase(
                noticeId = currentNoticeId,
                status = status,
                filterType = "ALL",
                cursorId = cursorId
            ),
            successCallback = { unReadStatus ->
                updateState {
                    copy(
                        unReadStatusList = if (cursorId == null) {
                            unReadStatus.content
                        } else {
                            unReadStatusList + unReadStatus.content
                        },
                        unReadNextCursor = unReadStatus.nextCursor,
                        unReadHasNext = unReadStatus.hasNext,
                        isLoadingReadStatus = false
                    )
                }
            },
            errorCallback = {
                updateState { copy(isLoadingReadStatus = false) }
                emitEvent(NoticeFragmentEvent.ShowError("확인 현황을 불러오는데 실패했습니다"))
            }
        )
    }

    fun loadMoreUnReadStatus() {
        val state = uiState.value
        if (state.isLoadingReadStatus || !state.unReadHasNext) return
        loadUnReadStatus("UNREAD", state.unReadNextCursor)
    }

    fun onClickBackPressed() {
        emitEvent(NoticeFragmentEvent.MoveBackPressedEvent)
    }

    fun onClickVoteItem(clickedOption: NoticeVoteOption) {
        val vote = uiState.value.detail.vote ?: return
        val isMultiple = vote.allowMultipleChoice

        if (isMultiple) {
            if (selectedOptionIds.contains(clickedOption.optionId)) {
                selectedOptionIds.remove(clickedOption.optionId)
            } else {
                selectedOptionIds.add(clickedOption.optionId)
            }
        } else {
            if (selectedOptionIds.contains(clickedOption.optionId)) {
                selectedOptionIds.clear()
            } else {
                selectedOptionIds.clear()
                selectedOptionIds.add(clickedOption.optionId)
            }
        }

        updateState {
            copy(selectedVoteOptionIds = selectedOptionIds.toList())
        }
    }

    private fun isAlreadyVoted(): Boolean {
        return uiState.value.detail.vote?.mySelectedOptionIds?.isNotEmpty() == true
    }

    fun onClickVoteAction() {
        if (isAlreadyVoted() && !uiState.value.isVoteEditMode) {
            updateState { copy(isVoteEditMode = true) }
            return
        }
        onClickSubmitVote()
    }

    private fun onClickSubmitVote() = viewModelScope.launch {
        val vote = uiState.value.detail.vote ?: return@launch
        val voteId = vote.voteId
        val optionIds = selectedOptionIds.toList()
        val isEditMode = uiState.value.isVoteEditMode

        if (voteId == -1L) return@launch

        updateState { copy(isSubmittingVote = true) }
        startLoading()

        resultResponse(
            response = if (isEditMode) {
                updateVoteResponseUseCase(voteId, optionIds)
            } else {
                submitVoteResponseUseCase(voteId, optionIds)
            },
            successCallback = {
                viewModelScope.launch {
                    loadNoticeDetailSync()
                    emitEvent(
                        NoticeFragmentEvent.ShowSuccess(
                            if (isEditMode) "투표가 수정되었습니다" else "투표가 완료되었습니다"
                        )
                    )
                }
            },
            errorCallback = {
                updateState { copy(isSubmittingVote = false) }
                emitEvent(
                    NoticeFragmentEvent.ShowError(it.message)
                )
            }
        )
    }

    fun onClickShowBottomSheet() {
        emitEvent(NoticeFragmentEvent.ShowBottomSheetEvent)
    }

    fun onClickVoteParticipants() = viewModelScope.launch {
        val vote = uiState.value.detail.vote ?: return@launch
        if (vote.isAnonymous) return@launch

        val allMemberIds = vote.options
            .flatMap { it.selectedMemberIds }
            .distinct()

        val memberInfoMap = mutableMapOf<Long, NoticeVoteParticipant>()

        allMemberIds.forEach { memberId ->
            resultResponse(
                response = getMemberProfileUseCase(memberId),
                successCallback = { data ->
                    memberInfoMap[memberId] = NoticeVoteParticipant(
                        memberId = data.id,
                        nickname = data.nickname,
                        name = data.name,
                        profileImageUrl = data.profileImageLink
                    )
                },
                errorCallback = {
                }
            )
        }

        val sections = vote.options.map { option ->
            VoteOptionParticipants(
                optionId = option.optionId,
                optionTitle = option.content,
                participants = option.selectedMemberIds.mapNotNull { memberInfoMap[it] }
            )
        }

        updateState { copy(voteParticipantSections = sections) }
        emitEvent(NoticeFragmentEvent.MoveToVoteParticipantsFragment)
    }

    fun onClickSendReminder(targetChallengerIds: List<Long>) = viewModelScope.launch {
        if (currentNoticeId == 0L) return@launch

        updateState { copy(isSendingReminder = true) }
        startLoading()
        resultResponse(
            response = sendNoticeReminderUseCase(currentNoticeId, targetChallengerIds),
            successCallback = {
                updateState { copy(isSendingReminder = false) }
            },
            errorCallback = {
                updateState { copy(isSendingReminder = false) }
            }
        )
    }

    fun onClickEditPost() {
        updateState { copy(isMenuVisible = false) }
        emitEvent(NoticeFragmentEvent.MoveToEditPostEvent(noticeId = currentNoticeId))
    }

    fun onClickDeletePost() = viewModelScope.launch {
        if (currentNoticeId == 0L) {
            emitEvent(NoticeFragmentEvent.ShowError("유효하지 않은 공지사항 ID입니다"))
            return@launch
        }

        updateState { copy(isLoading = true) }
        startLoading()

        resultResponse(
            response = deleteNoticeUseCase(currentNoticeId),
            successCallback = {
                updateState { copy(isMenuVisible = false) }
                emitEvent(NoticeFragmentEvent.MoveBackPressedEvent)
            },
            errorCallback = {
                updateState { copy(isMenuVisible = false) }
                emitEvent(NoticeFragmentEvent.ShowError("공지사항 삭제에 실패했습니다"))
            }
        )
    }
}


data class NoticeFragmentUiState(
    val detail: NoticeDetail = NoticeDetail(),
    val readStatistics: NoticeReadStatistics? = null,
    val readStatusList: List<ChallengerReadInfo> = emptyList(),
    val unReadStatusList: List<ChallengerReadInfo> = emptyList(),
    val selectedVoteOptionIds: List<Long> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingReadStatus: Boolean = false,
    val isSendingReminder: Boolean = false,
    val isSubmittingVote: Boolean = false,
    val formattedVoteCondition: String = "",
    val formattedTargetInfo: String = "",
    val formattedCreatedAt: String = "",
    val authorNickname: String = "",
    val readNextCursor: Long? = null,
    val readHasNext: Boolean = false,
    val unReadNextCursor: Long? = null,
    val unReadHasNext: Boolean = false,
    val isMenuVisible: Boolean = false,
    val isCurrentUserMember: Boolean = false,
    val isAuthor: Boolean = false,
    val isVoteEditMode: Boolean = false,
    val voteParticipantSections: List<VoteOptionParticipants> = emptyList(),
) : UiState

sealed interface NoticeFragmentEvent : UiEvent {
    object ShowBottomSheetEvent : NoticeFragmentEvent
    object MoveBackPressedEvent : NoticeFragmentEvent
    data class MoveToEditPostEvent(val noticeId: Long) : NoticeFragmentEvent
    data class ShowError(val message: String) : NoticeFragmentEvent
    data class ShowSuccess(val message: String) : NoticeFragmentEvent
    object MoveToVoteParticipantsFragment : NoticeFragmentEvent
}

data class VoteOptionParticipants(
    val optionId: Long,
    val optionTitle: String,
    val participants: List<NoticeVoteParticipant>
)