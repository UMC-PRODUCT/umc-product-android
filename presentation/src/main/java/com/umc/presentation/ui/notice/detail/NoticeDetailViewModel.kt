package com.umc.presentation.ui.notice.detail

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.UDomainFormat.parseDateTime
import com.umc.domain.model.UserInfo
import com.umc.domain.model.home.GisuInfo
import com.umc.domain.model.notice.ChallengerReadInfo
import com.umc.domain.model.notice.NoticeDetail
import com.umc.domain.model.notice.NoticeReadStatistics
import com.umc.domain.model.notice.NoticeTarget
import com.umc.domain.model.notice.NoticeVote
import com.umc.domain.model.notice.NoticeVoteOption
import com.umc.domain.model.enums.UserChallengerRole
import com.umc.domain.usecase.GetChallengerIdUseCase
import com.umc.domain.usecase.GetGisuInfoUseCase
import com.umc.domain.usecase.appDataStore.GetUserInfoUseCase
import com.umc.domain.usecase.challenger.GetChallengerDetailUseCase
import com.umc.domain.usecase.member.GetMemberProfileUseCase
import com.umc.domain.usecase.notice.DeleteNoticeUseCase
import com.umc.domain.usecase.notice.GetNoticeDetailUseCase
import com.umc.domain.usecase.notice.GetNoticeReadStatisticsUseCase
import com.umc.domain.usecase.notice.GetNoticeReadStatusUseCase
import com.umc.domain.usecase.notice.SendNoticeReminderUseCase
import com.umc.domain.usecase.notice.SubmitVoteResponseUseCase
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

    fun init(noticeId: Long) {
        currentNoticeId = noticeId
        loadNoticeDetail()
        loadCurrentUserInfoAndCheckPermission()
    }

    private fun loadCurrentUserInfoAndCheckPermission() = viewModelScope.launch {
        val userInfo = getUserInfoUseCase().first()
        currentUserChallengerId = getChallengerIdUseCase()
        checkIsMember(userInfo)
        checkIsAuthor()
        
        // 권한 체크 후 통계/현황 조회
        if (hasAdminPermission(userInfo)) {
            loadReadStatistics()
            loadReadStatus()
            loadUnReadStatus()
        }
    }

    private fun hasAdminPermission(userInfo: UserInfo): Boolean {
        val adminRoles = setOf(
            UserChallengerRole.SUPER_ADMIN,
            UserChallengerRole.CENTRAL_PRESIDENT,
            UserChallengerRole.CENTRAL_VICE_PRESIDENT,
            UserChallengerRole.CENTRAL_OPERATING_TEAM_MEMBER,
            UserChallengerRole.CENTRAL_EDUCATION_TEAM_MEMBER,
            UserChallengerRole.CHAPTER_PRESIDENT,
            UserChallengerRole.SCHOOL_PRESIDENT,
            UserChallengerRole.SCHOOL_VICE_PRESIDENT,
            UserChallengerRole.SCHOOL_PART_LEADER,
            UserChallengerRole.SCHOOL_ETC_ADMIN
        )
        
        return userInfo.roles.any { role ->
            val userRole = UserChallengerRole.from(role.roleType)
            adminRoles.contains(userRole)
        }
    }

    private fun checkIsAuthor() {
        val isAuthor = currentUserChallengerId == uiState.value.detail.authorChallengerId && currentUserChallengerId != -1L
        updateState { copy(isAuthor = isAuthor) }
    }

    private fun checkIsMember(userInfo: UserInfo) {
        val hasOnlyMemberRole = userInfo.roles.isNotEmpty() &&
            userInfo.roles.all { it.roleType == "MEMBER" }
        
        updateState { 
            copy(isCurrentUserMember = hasOnlyMemberRole) 
        }
    }

    fun onClickMenu() {
        updateState { copy(isMenuVisible = !isMenuVisible) }
    }

    private fun loadNoticeDetail() = viewModelScope.launch {
        if (currentNoticeId == 0L) {
            emitEvent(NoticeFragmentEvent.ShowError("유효하지 않은 공지사항 ID입니다"))
            return@launch
        }

        updateState { copy(isLoading = true) }
        startLoading()

        resultResponse(
            response = getNoticeDetailUseCase(currentNoticeId),
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
                    fetchChapterAndUpdateDetail(targetChapterId.toLong(), detail, formattedCreatedAt, formattedVoteCondition)
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

    private fun fetchChapterAndUpdateDetail(
        chapterId: Long,
        detail: NoticeDetail,
        formattedCreatedAt: String,
        formattedVoteCondition: String
    ) = viewModelScope.launch {
        resultResponse(
            response = getChapterDetailUseCase(chapterId),
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
            updateState {
                copy(
                    detail = detail,
                    selectedVoteOptionIds = selectedOptionIds.toList(),
                    isLoading = false,
                    formattedCreatedAt = formattedCreatedAt,
                    formattedTargetInfo = formattedTargetInfo,
                    formattedVoteCondition = formattedVoteCondition
                )
            }
            checkIsAuthor()
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
                updateState {
                    copy(
                        detail = detail,
                        selectedVoteOptionIds = selectedOptionIds.toList(),
                        isLoading = false,
                        formattedCreatedAt = formattedCreatedAt,
                        formattedTargetInfo = formattedTargetInfo,
                        formattedVoteCondition = formattedVoteCondition
                    )
                }
                checkIsAuthor()
                loadAuthorNickname(detail.authorChallengerId)
            },
            errorCallback = {
                // gisu 조회 실패 시 targetGisuId로 fallback
                val formattedTargetInfo = getFormattedTargetInfo(detail.targetInfo, null)
                updateState {
                    copy(
                        detail = detail,
                        selectedVoteOptionIds = selectedOptionIds.toList(),
                        isLoading = false,
                        formattedCreatedAt = formattedCreatedAt,
                        formattedTargetInfo = formattedTargetInfo,
                        formattedVoteCondition = formattedVoteCondition
                    )
                }
                checkIsAuthor()
                loadAuthorNickname(detail.authorChallengerId)
            }
        )
    }

    private fun getFormattedVoteCondition(vote: NoticeVote?): String {
        return vote?.let {
            val choiceType = if (it.allowMultipleChoice) "복수선택" else "단일선택"
            val anonymity = if (it.isAnonymous) "익명" else "신원공개"
            val startDate = it.startDateKst.parseDateTime().first
            val endDate = it.endDateKst.parseDateTime().first
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

    fun onClickSubmitVote() = viewModelScope.launch {
        val vote = uiState.value.detail.vote ?: return@launch
        val voteId = vote.voteId
        val optionIds = selectedOptionIds.toList()

        if (voteId == -1L || optionIds.isEmpty()) return@launch

        updateState { copy(isSubmittingVote = true) }
        startLoading()

        resultResponse(
            response = submitVoteResponseUseCase(voteId, optionIds),
            successCallback = {
                loadNoticeDetail()
                emitEvent(NoticeFragmentEvent.ShowSuccess("투표가 완료되었습니다"))
            },
            errorCallback = {
                updateState { copy(isSubmittingVote = false) }
                emitEvent(NoticeFragmentEvent.ShowError("투표 제출에 실패했습니다"))
            }
        )
    }

    fun onClickShowBottomSheet() {
        emitEvent(NoticeFragmentEvent.ShowBottomSheetEvent)
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
) : UiState

sealed interface NoticeFragmentEvent : UiEvent {
    object ShowBottomSheetEvent : NoticeFragmentEvent
    object MoveBackPressedEvent : NoticeFragmentEvent
    data class MoveToEditPostEvent(val noticeId: Long) : NoticeFragmentEvent
    data class ShowError(val message: String) : NoticeFragmentEvent
    data class ShowSuccess(val message: String) : NoticeFragmentEvent
}