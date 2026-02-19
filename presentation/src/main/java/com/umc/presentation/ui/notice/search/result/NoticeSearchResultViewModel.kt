package com.umc.presentation.ui.notice.search.result

import android.view.inputmethod.EditorInfo
import androidx.lifecycle.viewModelScope
import com.umc.domain.model.notice.NoticeSummary
import com.umc.domain.model.notice.NoticeTarget
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoticeSearchResultViewModel
@Inject
constructor(
    // 더미 데이터를 사용하므로 UseCase 주입 없음
) : BaseViewModel<NoticeSearchResultUiState, NoticeSearchResultEvent>(
    NoticeSearchResultUiState(),
) {

    // 전체 더미 공지 목록 (검색 필터링용)
    private val allDummyNotices = listOf(
        NoticeSummary(
            id = 1L,
            title = "[필독] 9기 OT 안내",
            content = "9기 OT 일정 및 장소를 안내드립니다. 많은 참여 바랍니다.",
            shouldSendNotification = true,
            viewCount = 120,
            createdAt = "2026-02-10T10:00:00",
            targetInfo = NoticeTarget(targetGisuId = 9, targetParts = listOf("Android", "Server", "iOS", "Web")),
            authorChallengerId = 1L,
            authorNickname = "운영진",
            authorName = "홍길동"
        ),
        NoticeSummary(
            id = 2L,
            title = "Android 파트 세션 일정 변경 안내",
            content = "이번 주 Android 파트 세션이 목요일로 변경되었습니다.",
            shouldSendNotification = false,
            viewCount = 55,
            createdAt = "2026-02-12T14:00:00",
            targetInfo = NoticeTarget(targetGisuId = 9, targetParts = listOf("Android")),
            authorChallengerId = 2L,
            authorNickname = "Android 파트장",
            authorName = "김철수"
        ),
        NoticeSummary(
            id = 3L,
            title = "Server 파트 스터디 모집",
            content = "Server 파트 스터디 멤버를 모집합니다. 관심 있는 분은 연락주세요.",
            shouldSendNotification = false,
            viewCount = 40,
            createdAt = "2026-02-13T09:30:00",
            targetInfo = NoticeTarget(targetGisuId = 9, targetParts = listOf("Server")),
            authorChallengerId = 3L,
            authorNickname = "Server 파트장",
            authorName = "이영희"
        ),
        NoticeSummary(
            id = 4L,
            title = "데모데이 준비 일정 안내",
            content = "데모데이 준비 일정을 공유드립니다. 각 파트별 준비 사항을 확인해 주세요.",
            shouldSendNotification = true,
            viewCount = 98,
            createdAt = "2026-02-15T11:00:00",
            targetInfo = NoticeTarget(targetGisuId = 9, targetParts = listOf("Android", "Server", "iOS", "Web")),
            authorChallengerId = 1L,
            authorNickname = "운영진",
            authorName = "홍길동"
        ),
        NoticeSummary(
            id = 5L,
            title = "iOS 파트 멘토링 일정",
            content = "iOS 파트 멘토링이 다음 주 수요일에 진행됩니다.",
            shouldSendNotification = false,
            viewCount = 30,
            createdAt = "2026-02-17T16:00:00",
            targetInfo = NoticeTarget(targetGisuId = 9, targetParts = listOf("iOS")),
            authorChallengerId = 4L,
            authorNickname = "iOS 파트장",
            authorName = "박지성"
        ),
        NoticeSummary(
            id = 6L,
            title = "9기 전체 회의 공지",
            content = "9기 전체 회의가 이번 주 토요일 오후 2시에 진행됩니다.",
            shouldSendNotification = true,
            viewCount = 200,
            createdAt = "2026-02-18T08:00:00",
            targetInfo = NoticeTarget(targetGisuId = 9, targetParts = listOf("Android", "Server", "iOS", "Web")),
            authorChallengerId = 1L,
            authorNickname = "운영진",
            authorName = "홍길동"
        ),
        NoticeSummary(
            id = 7L,
            title = "프로젝트 중간 발표 안내",
            content = "프로젝트 중간 발표 일정 및 평가 기준을 안내드립니다.",
            shouldSendNotification = false,
            viewCount = 77,
            createdAt = "2026-02-19T13:00:00",
            targetInfo = NoticeTarget(targetGisuId = 9, targetParts = listOf("Android", "Server")),
            authorChallengerId = 2L,
            authorNickname = "Android 파트장",
            authorName = "김철수"
        )
    )

    fun initSearch(query: String, gisuId: Long) {
        updateState {
            copy(searchText = query, selectedGisu = gisuId)
        }
        searchNotices(query, isRefresh = true)
    }

    fun searchNotices(query: String, isRefresh: Boolean = false) {
        val state = uiState.value

        if (query.isBlank()) return
        if (state.isPageLoading || (!isRefresh && state.isLastPage)) return

        updateState {
            copy(
                isPageLoading = true,
                currentPage = if (isRefresh) 0 else currentPage,
                isLastPage = if (isRefresh) false else isLastPage
            )
        }

        viewModelScope.launch {
            delay(500) // 가짜 네트워크 지연

            // 제목 또는 내용에 키워드가 포함된 항목 필터링
            val filtered = allDummyNotices.filter { notice ->
                notice.title.contains(query, ignoreCase = true) ||
                notice.content.contains(query, ignoreCase = true)
            }

            updateState {
                copy(
                    noticeList = if (isRefresh) filtered else noticeList + filtered,
                    currentPage = currentPage + 1,
                    isPageLoading = false,
                    isLastPage = true // 더미이므로 항상 마지막 페이지
                )
            }
        }
    }

    fun onImeAction(actionId: Int, text: String): Boolean {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            viewModelScope.launch {
                searchNotices(text, true)
            }
            return true
        }
        return false
    }

    fun loadNextPage() {
        if (!uiState.value.isPageLoading && !uiState.value.isLastPage) {
            searchNotices(uiState.value.searchText, isRefresh = false)
        }
    }

    fun onClickBack() {
        emitEvent(NoticeSearchResultEvent.MoveToBack)
    }

    fun onClickNotice(noticeId: Long) {
        emitEvent(NoticeSearchResultEvent.MoveToNoticeDetail(noticeId))
    }
}

data class NoticeSearchResultUiState(
    val noticeList: List<NoticeSummary> = emptyList(),
    val searchText: String = "",
    val currentPage: Int = 0,
    val isPageLoading: Boolean = false,
    val isLastPage: Boolean = false,
    val selectedGisu: Long = 0
) : UiState

sealed interface NoticeSearchResultEvent : UiEvent {
    data object MoveToBack : NoticeSearchResultEvent
    data class MoveToNoticeDetail(val noticeId: Long) : NoticeSearchResultEvent
}
