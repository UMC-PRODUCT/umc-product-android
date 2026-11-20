package com.umc.presentation.notice.detail

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.component.theme.AppStrings
import com.umc.domain.model.enums.NoticeVoteStatus
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.notice.ChallengerReadInfo
import com.umc.domain.model.notice.NoticeDetail
import com.umc.domain.model.notice.NoticeReadStatistics
import com.umc.domain.model.notice.NoticeVoteOption
import com.umc.domain.model.notice.NoticeVoteParticipant
import com.umc.domain.usecase.GetGisuInfoUseCase
import com.umc.domain.usecase.appDataStore.GetUserInfoUseCase
import com.umc.domain.usecase.member.GetMemberProfileUseCase
import com.umc.domain.usecase.notice.DeleteNoticeUseCase
import com.umc.domain.usecase.notice.GetNoticeDetailUseCase
import com.umc.domain.usecase.notice.GetNoticeReadStatisticsUseCase
import com.umc.domain.usecase.notice.GetNoticeReadStatusUseCase
import com.umc.domain.usecase.notice.MarkNoticeAsReadUseCase
import com.umc.domain.usecase.notice.SendNoticeReminderUseCase
import com.umc.domain.usecase.notice.SubmitVoteResponseUseCase
import com.umc.domain.usecase.notice.UpdateVoteResponseUseCase
import com.umc.domain.usecase.organization.GetChapterListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import javax.inject.Inject

@HiltViewModel
class NoticeDetailViewModel @Inject constructor(
    private val getNoticeDetailUseCase: GetNoticeDetailUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getGisuInfoUseCase: GetGisuInfoUseCase,
    private val getMemberProfileUseCase: GetMemberProfileUseCase,
    private val submitVoteResponseUseCase: SubmitVoteResponseUseCase,
    private val updateVoteResponseUseCase: UpdateVoteResponseUseCase,
    private val getNoticeReadStatisticsUseCase: GetNoticeReadStatisticsUseCase,
    private val getNoticeReadStatusUseCase: GetNoticeReadStatusUseCase,
    private val sendNoticeReminderUseCase: SendNoticeReminderUseCase,
    private val deleteNoticeUseCase: DeleteNoticeUseCase,
    private val markNoticeAsReadUseCase: MarkNoticeAsReadUseCase,
    private val getChapterListUseCase: GetChapterListUseCase,
) : BaseViewModel<NoticeDetailUiState, NoticeDetailEvent>(
    NoticeDetailUiState(),
) {

    private var noticeId: Long = 0L

    // 투표 등으로 loadDetail이 다시 불려도 읽음 등록은 상세 진입당 한 번만 보낸다
    private var hasMarkedRead = false

    // 상세를 한 번이라도 받아 왔는지. 회전·다크 모드 전환으로 Route가 다시 그려질 때 다시 받지 않기 위해 둔다
    private var hasLoadedDetail = false

    // 수정 화면에 다녀오면 바뀐 내용을 반영해야 하므로 돌아왔을 때 한 번 더 받는다
    private var reloadOnReturn = false

    fun load(noticeId: Long) {
        // 같은 공지를 이미 받았으면 스크롤 위치·선택 중인 투표 항목을 지키기 위해 다시 받지 않는다
        if (noticeId == this.noticeId && hasLoadedDetail) return
        this.noticeId = noticeId
        loadDetail()
    }

    /**
     * 화면이 RESUMED가 될 때 부른다. 수정 화면에서 실제로 돌아왔을 때만 다시 받는다.
     * 컴포지션에 들어올 때 확인하면 취소될 수 있는 예측 뒤로 가기 미리보기에서 플래그가 소비돼 실제 복귀 때 갱신이 빠진다
     */
    fun onResumed() {
        if (!reloadOnReturn) return
        reloadOnReturn = false
        loadDetail()
    }

    private fun loadDetail() = viewModelScope.launch {
        if (noticeId <= 0L) {
            emitEvent(NoticeDetailEvent.ShowToast(AppStrings.NOTICE_DETAIL_LOAD_FAIL, isSuccess = false))
            emitEvent(NoticeDetailEvent.MoveBack)
            return@launch
        }

        updateState { copy(isLoading = true) }

        resultResponse(
            response = getNoticeDetailUseCase(noticeId),
            successCallback = { detail ->
                hasLoadedDetail = true
                updateState {
                    copy(
                        detail = detail,
                        isLoading = false,
                        selectedOptionIds = detail.vote?.mySelectedOptionIds.orEmpty().toImmutableList(),
                        isSubmittingVote = false,
                        voteStatus = detail.vote
                            ?.let { NoticeVoteStatus.from(it.status) }
                            ?: NoticeVoteStatus.OPEN,
                    )
                }
                markAsRead()
                loadAuthorInfo(detail)
                loadReceiverText(detail)
            },
            errorCallback = {
                updateState { copy(isLoading = false) }
                emitEvent(NoticeDetailEvent.ShowToast(AppStrings.NOTICE_DETAIL_LOAD_FAIL, isSuccess = false))
            },
        )
    }

    /** 서버 수신 확인(읽음) 집계에 반영. 결과는 화면에 영향이 없으므로 실패해도 무시한다 */
    private fun markAsRead() {
        if (hasMarkedRead) return
        hasMarkedRead = true
        viewModelScope.launch {
            resultResponse(
                response = markNoticeAsReadUseCase(noticeId),
                successCallback = {},
            )
        }
    }

    /** "12기/전체" 형태의 수신대상 문구 구성. 기수 번호는 별도 조회 */
    private fun loadReceiverText(detail: NoticeDetail) = viewModelScope.launch {
        val target = detail.targetInfo
        var gisuLabel: String? = null

        if (target.targetGisuId != 0) {
            resultResponse(
                response = getGisuInfoUseCase(target.targetGisuId.toLong()),
                successCallback = { gisuLabel = AppStrings.COMMON_GENERATION_FORMAT.format(it.gisu) },
            )
        }

        // 상세 응답에는 지부 ID만 내려오므로 지부 이름은 지부 목록에서 찾는다
        var chapterLabel: String? = target.targetChapterName
        val chapterId = target.targetChapterId
        if (chapterLabel == null && chapterId != null) {
            resultResponse(
                response = getChapterListUseCase(),
                successCallback = { list -> chapterLabel = list.firstOrNull { it.id == chapterId }?.name },
            )
        }

        val scopeLabel = chapterLabel
            ?: if (target.targetChapterId == null && target.targetSchoolId == null) AppStrings.COMMON_ALL else null
        val partLabel = target.targetParts.firstOrNull()?.let { UserPart.from(it).label }

        val receiverText = listOfNotNull(gisuLabel, scopeLabel, partLabel).joinToString("/")
        if (receiverText.isNotBlank()) {
            updateState {
                copy(receiverText = AppStrings.NOTICE_DETAIL_RECEIVER_FORMAT.format(receiverText))
            }
        }
    }

    /**
     * 작성자 여부 판단(메뉴/수신 확인 현황 노출 조건) 후 작성자 프로필 로드.
     *
     * memberId는 기수와 무관한 전역 식별자라 1순위로 비교한다.
     * challengerId는 기수마다 달라지므로 "최신 기수 하나"가 아니라
     * 내가 가진 모든 기수의 challengerId 집합에 들어 있는지로 본다
     */
    private fun loadAuthorInfo(detail: NoticeDetail) = viewModelScope.launch {
        val userInfo = getUserInfoUseCase().first()

        val myMemberId = userInfo.id
        val myChallengerIds = (
            userInfo.challengerRecords.map { it.challengerId } +
                userInfo.roles.map { it.challengerId }
            ).filter { it > 0L }.toSet()

        val isAuthor = (myMemberId > 0L && myMemberId == detail.authorMemberId) ||
                (detail.authorChallengerId > 0L && detail.authorChallengerId in myChallengerIds)

        updateState { copy(isAuthor = isAuthor) }

        if (isAuthor) {
            loadReadStatistics()
            loadReadStatus(isRead = false)
            loadReadStatus(isRead = true)
        }

        // 프로필 조회는 memberId 기준 (작성자 판별에 쓰는 challengerId와 다른 값)
        if (detail.authorMemberId > 0) {
            resultResponse(
                response = getMemberProfileUseCase(detail.authorMemberId),
                successCallback = { author ->
                    updateState {
                        copy(
                            authorName = author.nickname.ifBlank { author.name },
                            authorProfileImageUrl = author.profileImageLink,
                        )
                    }
                },
            )
        }
    }

    // ---------------------------------------------------------------
    // 투표
    // ---------------------------------------------------------------

    /** 투표 항목 선택. 진행 중일 때만 동작 (투표 후에도 바로 다시 선택 가능) */
    fun onClickVoteOption(option: NoticeVoteOption) {
        val state = uiState.value
        val vote = state.detail.vote ?: return
        if (state.voteStatus != NoticeVoteStatus.OPEN) return

        val selected = state.selectedOptionIds.toMutableList()
        if (vote.allowMultipleChoice) {
            if (option.optionId in selected) selected.remove(option.optionId) else selected.add(option.optionId)
        } else {
            val wasSelected = option.optionId in selected
            selected.clear()
            if (!wasSelected) selected.add(option.optionId)
        }
        updateState { copy(selectedOptionIds = selected.toImmutableList()) }
    }

    /** 투표하기/다시 투표하기 버튼. 이미 투표한 상태면 수정 API로 제출 */
    fun onClickVoteButton() = viewModelScope.launch {
        val state = uiState.value
        if (state.voteStatus != NoticeVoteStatus.OPEN || state.isSubmittingVote) return@launch

        val vote = state.detail.vote ?: return@launch
        val optionIds = state.selectedOptionIds
        if (vote.voteId == -1L || optionIds.isEmpty()) return@launch

        val isRevote = state.hasVoted
        updateState { copy(isSubmittingVote = true) }

        resultResponse(
            response = if (isRevote) {
                updateVoteResponseUseCase(noticeId, optionIds)
            } else {
                submitVoteResponseUseCase(noticeId, optionIds)
            },
            successCallback = {
                emitEvent(
                    NoticeDetailEvent.ShowToast(
                        if (isRevote) AppStrings.NOTICE_DETAIL_VOTE_EDIT_SUCCESS
                        else AppStrings.NOTICE_DETAIL_VOTE_SUCCESS,
                        isSuccess = true,
                    )
                )
                loadDetail()
            },
            errorCallback = {
                updateState { copy(isSubmittingVote = false) }
                emitEvent(
                    NoticeDetailEvent.ShowToast(
                        it.message.ifBlank { AppStrings.NOTICE_DETAIL_VOTE_FAIL },
                        isSuccess = false,
                    )
                )
            },
        )
    }

    /** 투표 현황(옵션별 참여자) 로드. 실명 투표에서만 노출 */
    fun loadVoteParticipants() = viewModelScope.launch {
        val vote = uiState.value.detail.vote ?: return@launch
        if (vote.isAnonymous || uiState.value.isLoadingVoteParticipants) return@launch

        updateState { copy(isLoadingVoteParticipants = true) }

        val memberIds = vote.options.flatMap { it.selectedMemberIds }.distinct()
        // 참여자마다 순서대로 부르면 인원에 비례해 늦어지므로 함께 부른다.
        // 다만 한꺼번에 몰리지 않도록 동시에 보내는 요청 수는 제한한다
        val semaphore = Semaphore(VOTE_PARTICIPANT_FETCH_CONCURRENCY)
        val responses = coroutineScope {
            memberIds.map { memberId ->
                async { semaphore.withPermit { memberId to getMemberProfileUseCase(memberId) } }
            }.awaitAll()
        }
        val profiles = mutableMapOf<Long, NoticeVoteParticipant>()
        responses.forEach { (memberId, response) ->
            resultResponse(
                response = response,
                successCallback = { member ->
                    profiles[memberId] = NoticeVoteParticipant(
                        memberId = member.id,
                        nickname = member.nickname,
                        name = member.name,
                        profileImageUrl = member.profileImageLink,
                    )
                },
                // 프로필을 못 받은 참여자를 빼면 참여 인원이 실제보다 적게 보이므로 이름 대신 안내 문구로 남긴다
                errorCallback = {
                    profiles[memberId] = NoticeVoteParticipant(
                        memberId = memberId,
                        name = AppStrings.NOTICE_VOTE_UNKNOWN_PARTICIPANT,
                    )
                },
            )
        }

        val sections = vote.options.map { option ->
            VoteOptionParticipants(
                optionId = option.optionId,
                optionTitle = option.content,
                participants = option.selectedMemberIds.mapNotNull { profiles[it] }.toImmutableList(),
            )
        }
        updateState {
            copy(
                voteParticipantSections = sections.toImmutableList(),
                isLoadingVoteParticipants = false,
            )
        }
    }

    // ---------------------------------------------------------------
    // 수신 확인 현황 (작성자 전용)
    // ---------------------------------------------------------------

    private fun loadReadStatistics() = viewModelScope.launch {
        resultResponse(
            response = getNoticeReadStatisticsUseCase(noticeId),
            successCallback = { updateState { copy(readStatistics = it) } },
        )
    }

    /**
     * 확인/미확인 목록을 각각 로드한다.
     * 두 탭은 서로 다른 요청이므로 로딩 플래그를 분리해야 한다
     * (공용 플래그를 쓰면 진입 시 연달아 호출되는 두 번째 요청이 가드에 걸린다)
     */
    fun loadReadStatus(isRead: Boolean, cursorId: Long? = null) = viewModelScope.launch {
        val state = uiState.value
        if (if (isRead) state.isLoadingReadList else state.isLoadingUnreadList) return@launch
        updateState {
            if (isRead) copy(isLoadingReadList = true) else copy(isLoadingUnreadList = true)
        }

        resultResponse(
            response = getNoticeReadStatusUseCase(
                noticeId = noticeId,
                cursorId = cursorId,
                status = if (isRead) "READ" else "UNREAD",
            ),
            successCallback = { readStatus ->
                updateState {
                    if (isRead) {
                        copy(
                            readList = if (cursorId == null) {
                                readStatus.content.toImmutableList()
                            } else {
                                (readList + readStatus.content).toImmutableList()
                            },
                            readNextCursor = readStatus.nextCursor,
                            readHasNext = readStatus.hasNext,
                            isLoadingReadList = false,
                        )
                    } else {
                        copy(
                            unreadList = if (cursorId == null) {
                                readStatus.content.toImmutableList()
                            } else {
                                (unreadList + readStatus.content).toImmutableList()
                            },
                            unreadNextCursor = readStatus.nextCursor,
                            unreadHasNext = readStatus.hasNext,
                            isLoadingUnreadList = false,
                        )
                    }
                }
            },
            errorCallback = {
                updateState {
                    if (isRead) copy(isLoadingReadList = false) else copy(isLoadingUnreadList = false)
                }
            },
        )
    }

    fun loadMoreReadStatus(isRead: Boolean) {
        val state = uiState.value
        if (isRead && state.readHasNext && !state.isLoadingReadList) {
            loadReadStatus(true, state.readNextCursor)
        }
        if (!isRead && state.unreadHasNext && !state.isLoadingUnreadList) {
            loadReadStatus(false, state.unreadNextCursor)
        }
    }

    /** 미확인 인원에게 재알림 발송 */
    fun onClickSendReminder() = viewModelScope.launch {
        val state = uiState.value
        if (state.isSendingReminder || state.isReminderSent) return@launch
        val targetIds = state.unreadList.map { it.challengerId }
        if (targetIds.isEmpty()) return@launch

        updateState { copy(isSendingReminder = true) }

        resultResponse(
            response = sendNoticeReminderUseCase(noticeId, targetIds),
            successCallback = {
                updateState { copy(isSendingReminder = false, isReminderSent = true) }
                emitEvent(NoticeDetailEvent.ShowToast(AppStrings.NOTICE_DETAIL_REMINDER_SUCCESS, isSuccess = true))
            },
            errorCallback = {
                updateState { copy(isSendingReminder = false) }
                emitEvent(
                    NoticeDetailEvent.ShowToast(
                        it.message.ifBlank { AppStrings.NOTICE_DETAIL_REMINDER_FAIL },
                        isSuccess = false,
                    )
                )
            },
        )
    }

    // ---------------------------------------------------------------
    // 수정 / 삭제 (작성자 전용)
    // ---------------------------------------------------------------

    fun onClickEdit() {
        reloadOnReturn = true
        emitEvent(NoticeDetailEvent.MoveToEdit(noticeId))
    }

    fun onClickDelete() = viewModelScope.launch {
        resultResponse(
            response = deleteNoticeUseCase(noticeId),
            successCallback = {
                emitEvent(NoticeDetailEvent.ShowToast(AppStrings.NOTICE_DETAIL_DELETE_SUCCESS, isSuccess = true))
                // 목록이 삭제를 반영하도록 단순 뒤로가기와 구분해 알린다
                emitEvent(NoticeDetailEvent.Deleted)
            },
            errorCallback = {
                emitEvent(
                    NoticeDetailEvent.ShowToast(
                        it.message.ifBlank { AppStrings.NOTICE_DETAIL_DELETE_FAIL },
                        isSuccess = false,
                    )
                )
            },
        )
    }

    private companion object {
        /** 투표 참여자 프로필을 동시에 요청하는 최대 개수 */
        const val VOTE_PARTICIPANT_FETCH_CONCURRENCY = 4
    }
}

data class NoticeDetailUiState(
    val detail: NoticeDetail = NoticeDetail(),
    val isLoading: Boolean = true,
    val isAuthor: Boolean = false,
    val authorName: String = "",
    val authorProfileImageUrl: String = "",
    val selectedOptionIds: ImmutableList<Long> = persistentListOf(),
    val isSubmittingVote: Boolean = false,
    val receiverText: String = "",
    val voteStatus: NoticeVoteStatus = NoticeVoteStatus.OPEN,
    val voteParticipantSections: ImmutableList<VoteOptionParticipants> = persistentListOf(),
    val isLoadingVoteParticipants: Boolean = false,
    val readStatistics: NoticeReadStatistics? = null,
    val readList: ImmutableList<ChallengerReadInfo> = persistentListOf(),
    val readNextCursor: Long? = null,
    val readHasNext: Boolean = false,
    val unreadList: ImmutableList<ChallengerReadInfo> = persistentListOf(),
    val unreadNextCursor: Long? = null,
    val unreadHasNext: Boolean = false,
    val isLoadingReadList: Boolean = false,
    val isLoadingUnreadList: Boolean = false,
    val isSendingReminder: Boolean = false,
    val isReminderSent: Boolean = false,
) : UiState {
    /** 내가 이미 투표했는지 (서버 기준) */
    val hasVoted: Boolean
        get() = detail.vote?.mySelectedOptionIds?.isNotEmpty() == true

    val isVoteClosed: Boolean
        get() = voteStatus == NoticeVoteStatus.CLOSED

    val isVoteNotStarted: Boolean
        get() = voteStatus == NoticeVoteStatus.NOT_STARTED

    /** 결과(득표수 게이지)를 보여줄 상태인지: 투표를 마쳤을 때만 (마감이어도 미투표면 비공개) */
    val showVoteResult: Boolean
        get() = hasVoted
}

sealed interface NoticeDetailEvent : UiEvent {
    data object MoveBack : NoticeDetailEvent
    data object Deleted : NoticeDetailEvent
    data class MoveToEdit(val noticeId: Long) : NoticeDetailEvent
    /** [isSuccess]가 true면 성공 아이콘, false면 실패 아이콘으로 띄운다 */
    data class ShowToast(val message: String, val isSuccess: Boolean) : NoticeDetailEvent
}

/** 투표 현황 시트에 표시할 옵션별 참여자 묶음 */
data class VoteOptionParticipants(
    val optionId: Long,
    val optionTitle: String,
    val participants: ImmutableList<NoticeVoteParticipant>,
)
