package com.umc.presentation.ui.notice.detail

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.notice.ChallengerReadInfo
import com.umc.domain.model.notice.NoticeDetail
import com.umc.domain.model.notice.NoticeImage
import com.umc.domain.model.notice.NoticeLink
import com.umc.domain.model.notice.NoticeReadStatistics
import com.umc.domain.model.notice.NoticeTarget
import com.umc.domain.model.notice.NoticeVote
import com.umc.domain.model.notice.NoticeVoteOption
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoticeDetailViewModel @Inject constructor(
    // 더미 데이터를 사용하므로 UseCase 주입 없음
) : BaseViewModel<NoticeFragmentUiState, NoticeFragmentEvent>(
    NoticeFragmentUiState()
) {

    private var currentNoticeId: Long = 0L
    private var selectedOptionIds: MutableSet<Long> = mutableSetOf()

    fun init(noticeId: Long) {
        currentNoticeId = noticeId
        loadDummyNoticeDetail()
        loadDummyReadStatistics()
        loadDummyReadStatus()
        loadDummyUnReadStatus()
    }

    private fun loadDummyNoticeDetail() = viewModelScope.launch {
        updateState { copy(isLoading = true) }
        delay(500) // 가짜 네트워크 지연

        // 1. 투표 옵션 더미 데이터
        val dummyOptions = listOf(
            NoticeVoteOption(optionId = 1L, content = "1번 옵션 (월요일)", voteCount = 5, voteRate = 50.0),
            NoticeVoteOption(optionId = 2L, content = "2번 옵션 (화요일)", voteCount = 3, voteRate = 30.0),
            NoticeVoteOption(optionId = 3L, content = "3번 옵션 (수요일)", voteCount = 2, voteRate = 20.0)
        )

        // 2. 투표 정보 더미 데이터 (미투표 상태로 시작)
        val dummyVote = NoticeVote(
            voteId = 100L,
            title = "회의 날짜 투표",
            allowMultipleChoice = true,
            isAnonymous = true,
            status = "PROGRESS",
            startsAt = "2026-02-20T00:00:00",
            endsAtExclusive = "2026-02-26T00:00:00",
            startDateKst = "2026-02-20T00:00:00",
            endDateKst = "2026-02-25T23:59:59",
            totalParticipants = 10,
            mySelectedOptionIds = emptyList(), // 미투표 상태
            options = dummyOptions
        )

        // 3. 타겟 정보 더미 데이터
        val dummyTarget = NoticeTarget(
            targetGisuId = 9,
            targetChapterId = 1,
            targetSchoolId = null,
            targetParts = listOf("Android", "Server")
        )

        // 4. 이미지 / 링크 더미 데이터
        val dummyImages = listOf(
            NoticeImage(id = 1L, url = "https://picsum.photos/seed/notice1/400/300", displayOrder = 1)
        )
        val dummyLinks = listOf(
            NoticeLink(id = 1L, url = "https://www.umc.com", displayOrder = 1)
        )

        // 5. 공지사항 상세 더미 데이터
        val dummyDetail = NoticeDetail(
            id = currentNoticeId,
            title = "더미 공지사항 제목입니다",
            content = "이것은 더미 데이터로 구성된 공지사항 내용입니다. 투표에 참여해 주세요!",
            createdAt = "2026-02-20T14:30:00",
            authorChallengerId = 999L,
            mustRead = true,
            targetInfo = dummyTarget,
            vote = dummyVote,
            images = dummyImages,
            links = dummyLinks,
            viewCount = 42
        )

        selectedOptionIds = dummyVote.mySelectedOptionIds.toMutableSet()

        val formattedCreatedAt = "2026.02.20"
        val formattedTargetInfo = "수신대상: 9기 / Android"
        val formattedVoteCondition = "2026-02-20 ~ 2026-02-25 • 복수선택 • 익명"

        updateState {
            copy(
                detail = dummyDetail,
                selectedVoteOptionIds = selectedOptionIds.toList(),
                isLoading = false,
                formattedCreatedAt = formattedCreatedAt,
                formattedTargetInfo = formattedTargetInfo,
                formattedVoteCondition = formattedVoteCondition,
                isCurrentUserMember = true,
                isAuthor = true,
                authorNickname = "UMC 관리자 (더미)"
            )
        }
    }

    private fun loadDummyReadStatistics() = viewModelScope.launch {
        delay(500)
        val dummyStats = NoticeReadStatistics(
            totalCount = 20,
            readCount = 15,
            unreadCount = 5,
            readRate = 75.0
        )
        updateState { copy(readStatistics = dummyStats) }
    }

    fun loadDummyReadStatus() = viewModelScope.launch {
        updateState { copy(isLoadingReadStatus = true) }
        delay(500)

        val dummyReadList = listOf(
            ChallengerReadInfo(
                challengerId = 1L,
                name = "김철수",
                profileImageUrl = "https://picsum.photos/seed/challenger1/100/100",
                part = "Android",
                schoolId = 1L,
                schoolName = "서울대학교",
                chapterId = 1L,
                chapterName = "서울 1장"
            ),
            ChallengerReadInfo(
                challengerId = 2L,
                name = "이영희",
                profileImageUrl = "https://picsum.photos/seed/challenger2/100/100",
                part = "Server",
                schoolId = 2L,
                schoolName = "연세대학교",
                chapterId = 1L,
                chapterName = "서울 1장"
            )
        )

        updateState {
            copy(
                readStatusList = dummyReadList,
                readHasNext = false,
                isLoadingReadStatus = false
            )
        }
    }

    fun loadDummyUnReadStatus() = viewModelScope.launch {
        updateState { copy(isLoadingReadStatus = true) }
        delay(500)

        val dummyUnReadList = listOf(
            ChallengerReadInfo(
                challengerId = 3L,
                name = "박지성",
                profileImageUrl = "https://picsum.photos/seed/challenger3/100/100",
                part = "Web",
                schoolId = 3L,
                schoolName = "고려대학교",
                chapterId = 2L,
                chapterName = "서울 2장"
            ),
            ChallengerReadInfo(
                challengerId = 4L,
                name = "손흥민",
                profileImageUrl = "https://picsum.photos/seed/challenger4/100/100",
                part = "iOS",
                schoolId = 4L,
                schoolName = "성균관대학교",
                chapterId = 2L,
                chapterName = "서울 2장"
            )
        )

        updateState {
            copy(
                unReadStatusList = dummyUnReadList,
                unReadHasNext = false,
                isLoadingReadStatus = false
            )
        }
    }

    fun onClickMenu() {
        updateState { copy(isMenuVisible = !isMenuVisible) }
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
        val optionIds = selectedOptionIds.toList()
        if (optionIds.isEmpty()) return@launch

        updateState { copy(isSubmittingVote = true) }
        delay(1000) // 가짜 제출 로딩 시간

        // 투표 제출 후 로컬 상태 업데이트 (더미 서버 응답 시뮬레이션)
        val currentVote = uiState.value.detail.vote ?: return@launch
        val totalVoters = currentVote.totalParticipants + 1
        val updatedOptions = currentVote.options.map { option ->
            val isSelected = optionIds.contains(option.optionId)
            val newVoteCount = if (isSelected) option.voteCount + 1 else option.voteCount
            val newVoteRate = if (totalVoters > 0) (newVoteCount.toDouble() / totalVoters) * 100.0 else 0.0
            option.copy(voteCount = newVoteCount, voteRate = newVoteRate)
        }
        val updatedVote = currentVote.copy(
            mySelectedOptionIds = optionIds,
            totalParticipants = totalVoters,
            options = updatedOptions
        )
        val updatedDetail = uiState.value.detail.copy(vote = updatedVote)

        updateState {
            copy(
                isSubmittingVote = false,
                detail = updatedDetail,
                selectedVoteOptionIds = optionIds
            )
        }
        emitEvent(NoticeFragmentEvent.ShowSuccess("투표가 성공적으로 완료되었습니다!"))
    }

    fun onClickShowBottomSheet() {
        emitEvent(NoticeFragmentEvent.ShowBottomSheetEvent)
    }

    fun onClickSendReminder(@Suppress("UNUSED_PARAMETER") targetChallengerIds: List<Long>) = viewModelScope.launch {
        updateState { copy(isSendingReminder = true) }
        delay(1000)

        updateState { copy(isSendingReminder = false) }
        emitEvent(NoticeFragmentEvent.ShowSuccess("리마인더 알림을 성공적으로 보냈습니다 (더미)"))
    }

    fun onClickEditPost() {
        // 수정 로직 더미
    }

    fun onClickDeletePost() {
        // 삭제 로직 더미
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
    data class ShowError(val message: String) : NoticeFragmentEvent
    data class ShowSuccess(val message: String) : NoticeFragmentEvent
}
