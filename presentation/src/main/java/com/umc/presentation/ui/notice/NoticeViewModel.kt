package com.umc.presentation.ui.notice

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.UserInfo
import com.umc.domain.model.UserRole
import com.umc.domain.model.notice.NoticeChipState
import com.umc.domain.model.notice.NoticeSummary
import com.umc.domain.model.notice.NoticeTarget
import com.umc.domain.model.organization.GisuItem
import com.umc.domain.model.enums.UserChallengerRole
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoticeViewModel @Inject constructor(
    // 더미 데이터를 사용하므로 UseCase 주입 없음
) : BaseViewModel<NoticeUiState, NoticeEvent>(
    NoticeUiState(),
) {

    companion object {
        private val dummyNotices = listOf(
            // ── 9기 ──
            NoticeSummary(
                id = 1,
                title = "9기 활동 시작 안내",
                content = "안녕하세요 여러분! 9기 활동을 시작하게 되어 기쁘게 생각합니다. 이번 기수에서 함께 성장해봐요.",
                shouldSendNotification = true,
                viewCount = 1250,
                createdAt = "2025-02-20T10:30:00",
                targetInfo = NoticeTarget(targetGisuId = 9),
                authorChallengerId = 1,
                authorNickname = "운영진",
                authorName = "김운영"
            ),
            NoticeSummary(
                id = 2,
                title = "첫 번째 세션 일정 안내",
                content = "3월 1일 첫 번째 세션이 예정되어 있습니다. 장소와 시간 확인 부탁드립니다.",
                shouldSendNotification = true,
                viewCount = 980,
                createdAt = "2025-02-18T14:00:00",
                targetInfo = NoticeTarget(targetGisuId = 9, targetSchoolId = 1),
                authorChallengerId = 2,
                authorNickname = "세션팀",
                authorName = "이세션"
            ),
            NoticeSummary(
                id = 3,
                title = "과제 제출 마감일 연장",
                content = "여러분의 요청으로 과제 제출 마감일을 3월 10일까지 연장합니다.",
                shouldSendNotification = false,
                viewCount = 750,
                createdAt = "2025-02-15T09:00:00",
                targetInfo = NoticeTarget(targetGisuId = 9, targetParts = listOf("PM 파트", "Design 파트")),
                authorChallengerId = 3,
                authorNickname = "PM리더",
                authorName = "박매니"
            ),
            NoticeSummary(
                id = 4,
                title = "팀 빌딩 결과 발표",
                content = "드디어 팀 빌딩 결과가 나왔습니다! 자신의 팀을 확인해주세요.",
                shouldSendNotification = true,
                viewCount = 2100,
                createdAt = "2025-02-10T16:30:00",
                targetInfo = NoticeTarget(targetGisuId = 9),
                authorChallengerId = 1,
                authorNickname = "운영진",
                authorName = "김운영"
            ),
            NoticeSummary(
                id = 5,
                title = "와이어프레임 제출 안내",
                content = "와이어프레임 제출 방식에 대해 안내드립니다. 꼭 확인해주세요!",
                shouldSendNotification = false,
                viewCount = 560,
                createdAt = "2025-02-08T11:00:00",
                targetInfo = NoticeTarget(targetGisuId = 9, targetSchoolId = 1),
                authorChallengerId = 4,
                authorNickname = "디자인팀",
                authorName = "최디자인"
            ),
            NoticeSummary(
                id = 6,
                title = "중간 발표 준비사항",
                content = "4월 15일 중간 발표를 위한 준비사항입니다. PPT 템플릿도 함께 공유드립니다.",
                shouldSendNotification = true,
                viewCount = 890,
                createdAt = "2025-02-05T13:00:00",
                targetInfo = NoticeTarget(targetGisuId = 9, targetParts = listOf("PM 파트")),
                authorChallengerId = 3,
                authorNickname = "PM리더",
                authorName = "박매니"
            ),
            NoticeSummary(
                id = 7,
                title = "해커톤 일정 안내",
                content = "5월 해커톤 일정이 확정되었습니다. 모두 참여 부탁드립니다!",
                shouldSendNotification = true,
                viewCount = 1100,
                createdAt = "2025-02-01T10:00:00",
                targetInfo = NoticeTarget(targetGisuId = 9),
                authorChallengerId = 2,
                authorNickname = "세션팀",
                authorName = "이세션"
            ),
            NoticeSummary(
                id = 8,
                title = "멘토링 매칭 결과",
                content = "멘토-멘티 매칭이 완료되었습니다. 담당 멘토님과 연락하세요.",
                shouldSendNotification = true,
                viewCount = 670,
                createdAt = "2025-01-28T15:30:00",
                targetInfo = NoticeTarget(targetGisuId = 9, targetChapterId = 1),
                authorChallengerId = 5,
                authorNickname = "서울지부",
                authorName = "정지부"
            ),
            NoticeSummary(
                id = 9,
                title = "API 명세서 공유",
                content = "백엔드 API 명세서를 공유드립니다. 개발 참고해주세요.",
                shouldSendNotification = false,
                viewCount = 420,
                createdAt = "2025-01-25T09:30:00",
                targetInfo = NoticeTarget(targetGisuId = 9, targetParts = listOf("Server 파트", "Android 파트", "iOS 파트")),
                authorChallengerId = 6,
                authorNickname = "백엔드팀",
                authorName = "한백엔"
            ),
            NoticeSummary(
                id = 10,
                title = "최종 발표회 안내",
                content = "6월 20일 최종 발표회가 예정되어 있습니다. 준비 잘 해주세요!",
                shouldSendNotification = true,
                viewCount = 1500,
                createdAt = "2025-01-20T11:00:00",
                targetInfo = NoticeTarget(targetGisuId = 9),
                authorChallengerId = 1,
                authorNickname = "운영진",
                authorName = "김운영"
            ),
            // ── 8기 ──
            NoticeSummary(
                id = 11,
                title = "8기 OT 안내",
                content = "8기 오리엔테이션이 진행됩니다. 시간과 장소를 꼭 확인해 주세요.",
                shouldSendNotification = true,
                viewCount = 1800,
                createdAt = "2024-02-22T10:00:00",
                targetInfo = NoticeTarget(targetGisuId = 8),
                authorChallengerId = 1,
                authorNickname = "운영진",
                authorName = "김운영"
            ),
            NoticeSummary(
                id = 12,
                title = "8기 팀 빌딩 결과",
                content = "8기 팀 빌딩이 완료되었습니다. 팀 배정 결과를 확인해주세요.",
                shouldSendNotification = true,
                viewCount = 2300,
                createdAt = "2024-02-19T16:00:00",
                targetInfo = NoticeTarget(targetGisuId = 8),
                authorChallengerId = 1,
                authorNickname = "운영진",
                authorName = "김운영"
            ),
            NoticeSummary(
                id = 13,
                title = "8기 세션 일정 변경",
                content = "이번 주 세션 일정이 변경되었습니다. 아래 내용을 확인해 주세요.",
                shouldSendNotification = false,
                viewCount = 640,
                createdAt = "2024-02-15T09:30:00",
                targetInfo = NoticeTarget(targetGisuId = 8, targetParts = listOf("Android 파트", "iOS 파트")),
                authorChallengerId = 2,
                authorNickname = "세션팀",
                authorName = "이세션"
            ),
            NoticeSummary(
                id = 14,
                title = "8기 중간 발표 안내",
                content = "8기 중간 발표가 4월 20일에 예정되어 있습니다.",
                shouldSendNotification = true,
                viewCount = 910,
                createdAt = "2024-02-10T13:00:00",
                targetInfo = NoticeTarget(targetGisuId = 8),
                authorChallengerId = 3,
                authorNickname = "PM리더",
                authorName = "박매니"
            ),
            NoticeSummary(
                id = 15,
                title = "8기 멘토링 일정",
                content = "8기 멘토-멘티 매칭 결과 및 일정을 공유드립니다.",
                shouldSendNotification = false,
                viewCount = 530,
                createdAt = "2024-02-05T11:00:00",
                targetInfo = NoticeTarget(targetGisuId = 8, targetChapterId = 1),
                authorChallengerId = 5,
                authorNickname = "서울지부",
                authorName = "정지부"
            ),
            NoticeSummary(
                id = 16,
                title = "8기 최종 발표회 안내",
                content = "8기의 마지막 발표회가 6월 25일에 열립니다. 모두 함께 해요!",
                shouldSendNotification = true,
                viewCount = 1750,
                createdAt = "2024-01-30T10:00:00",
                targetInfo = NoticeTarget(targetGisuId = 8),
                authorChallengerId = 1,
                authorNickname = "운영진",
                authorName = "김운영"
            ),
            // ── 7기 ──
            NoticeSummary(
                id = 17,
                title = "7기 활동 시작 안내",
                content = "7기 활동을 시작합니다. 모두 환영합니다!",
                shouldSendNotification = true,
                viewCount = 2000,
                createdAt = "2023-02-20T10:00:00",
                targetInfo = NoticeTarget(targetGisuId = 7),
                authorChallengerId = 1,
                authorNickname = "운영진",
                authorName = "김운영"
            ),
            NoticeSummary(
                id = 18,
                title = "7기 과제 제출 안내",
                content = "7기 첫 번째 ��제 제출 안내입니다. 기한을 꼭 지켜주세요.",
                shouldSendNotification = false,
                viewCount = 870,
                createdAt = "2023-02-16T09:00:00",
                targetInfo = NoticeTarget(targetGisuId = 7, targetParts = listOf("Android 파트", "Server 파트")),
                authorChallengerId = 6,
                authorNickname = "백엔드팀",
                authorName = "한백엔"
            ),
            NoticeSummary(
                id = 19,
                title = "7기 해커톤 결과 공유",
                content = "7기 해커톤의 결과를 공유드립니다. 수고하셨습니다!",
                shouldSendNotification = true,
                viewCount = 1400,
                createdAt = "2023-02-10T14:00:00",
                targetInfo = NoticeTarget(targetGisuId = 7),
                authorChallengerId = 2,
                authorNickname = "세션팀",
                authorName = "이세션"
            ),
            NoticeSummary(
                id = 20,
                title = "7기 최종 발표회 일정",
                content = "7기 최종 발표회가 6월 30일에 열립니다. 발표 자료를 준비해주세요.",
                shouldSendNotification = true,
                viewCount = 1600,
                createdAt = "2023-01-25T10:00:00",
                targetInfo = NoticeTarget(targetGisuId = 7),
                authorChallengerId = 1,
                authorNickname = "운영진",
                authorName = "김운영"
            ),
            // ── 6기 ──
            NoticeSummary(
                id = 21,
                title = "6기 OT 안내",
                content = "6기 오리엔테이션 일정 및 장소를 안내드립니다.",
                shouldSendNotification = true,
                viewCount = 2200,
                createdAt = "2022-02-21T10:00:00",
                targetInfo = NoticeTarget(targetGisuId = 6),
                authorChallengerId = 1,
                authorNickname = "운영진",
                authorName = "김운영"
            ),
            NoticeSummary(
                id = 22,
                title = "6기 세션 자료 공유",
                content = "6기 첫 번째 세션 자료를 공유드립니다. 참고해 주세요.",
                shouldSendNotification = false,
                viewCount = 760,
                createdAt = "2022-02-14T14:00:00",
                targetInfo = NoticeTarget(targetGisuId = 6, targetParts = listOf("Android 파트", "iOS 파트", "Web 파트")),
                authorChallengerId = 2,
                authorNickname = "세션팀",
                authorName = "이세션"
            ),
            NoticeSummary(
                id = 23,
                title = "6기 팀 프로젝트 시작",
                content = "6기 팀 프로젝트가 본격적으로 시작됩니다. 각 팀별 일정을 확인하세요.",
                shouldSendNotification = true,
                viewCount = 1100,
                createdAt = "2022-02-08T09:00:00",
                targetInfo = NoticeTarget(targetGisuId = 6),
                authorChallengerId = 3,
                authorNickname = "PM리더",
                authorName = "박매니"
            ),
            NoticeSummary(
                id = 24,
                title = "6기 최종 발표회 안내",
                content = "6기의 대단원을 장식할 최종 발표회가 7월 1일에 열립니다!",
                shouldSendNotification = true,
                viewCount = 1900,
                createdAt = "2022-01-20T10:00:00",
                targetInfo = NoticeTarget(targetGisuId = 6),
                authorChallengerId = 1,
                authorNickname = "운영진",
                authorName = "김운영"
            )
        )
    }

    init {
        getDropDownList()
        getMyProfile()
    }

    private fun updateChipList(chipList: List<NoticeChipState>) {
        updateState {
            copy(
                chipList = chipList
            )
        }
    }

    private fun createChipsFromUserInfo(userInfo: UserInfo): List<NoticeChipState> {
        val chipList = mutableListOf<NoticeChipState>()
        chipList.add(NoticeChipState(text = "전체", isClicked = true))

        // 공지 작성 권한 확인 (MEMBER가 아닌 경우에만 권한 있음)
        val hasWritePermission = userInfo.roles.any { role ->
            UserChallengerRole.from(role.roleType) != UserChallengerRole.MEMBER
        }
        updateState { copy(canWriteNotice = hasWritePermission) }

        if (userInfo.schoolId != 0L) {
            chipList.add(
                NoticeChipState(
                    text = userInfo.schoolName,
                    schoolId = userInfo.schoolId
                )
            )
        }

        userInfo.roles.forEach { role ->
            val chapterName = role.chapterName
            if (!chapterName.isNullOrEmpty() && chipList.none { it.text == "$chapterName 지부" }) {
                chipList.add(
                    NoticeChipState(
                        text = "$chapterName 지부",
                        chapterId = role.chapterId
                    )
                )
            }

            val responsiblePart = role.responsiblePart
            if (!responsiblePart.isNullOrEmpty() && chipList.none { it.text == responsiblePart }) {
                chipList.add(
                    NoticeChipState(
                        text = responsiblePart,
                        part = responsiblePart
                    )
                )
            }
        }

        return chipList
    }

    fun onClickChip(clickedItem: NoticeChipState) {
        val newList = uiState.value.chipList.map { chip ->
            chip.copy(isClicked = (chip.text == clickedItem.text))
        }
        updateChipList(newList)

        if (clickedItem.text == "중앙운영사무국") {
            updateState { copy(isShowSubChip = true) }
        } else {
            updateState { copy(isShowSubChip = false) }
        }
        getNoticeList(
            chapterId = clickedItem.chapterId,
            schoolId = clickedItem.schoolId,
            part = clickedItem.part
        )
    }

    fun onClickSearch() {
        emitEvent(NoticeEvent.MoveToSearchEvent(uiState.value.selectedGisu))
    }

    fun updateNowTitle(title: String, gisu: Long) {
        updateState {
            copy(nowTitle = title, selectedGisu = gisu)
        }
        getNoticeList()
    }

    fun updateDropDownList(list: List<GisuItem>) {
        updateState {
            copy(dropdownList = list)
        }
    }

    fun onClickShowDropDown() {
        updateState {
            copy(isShowDropDown = !uiState.value.isShowDropDown)
        }
    }

    fun updateSubChip(filter: String) {
        updateState {
            copy(selectedSubChip = filter)
        }
    }

    fun onClickWriteNotice() {
        emitEvent(NoticeEvent.MoveToWriteEvent)
    }

    private fun getDropDownList() {
        // 더미 데이터 사용
        val dummyGisuList = listOf(
            GisuItem(gisuId = 9, generation = 9, isActive = true),
            GisuItem(gisuId = 8, generation = 8, isActive = false),
            GisuItem(gisuId = 7, generation = 7, isActive = false),
            GisuItem(gisuId = 6, generation = 6, isActive = false)
        )
        val nowTitle = "${dummyGisuList.find { it.isActive }?.generation}기 공지사항"
        updateNowTitle(nowTitle, dummyGisuList.find { it.isActive }?.gisuId?.toLong() ?: 9)
        updateDropDownList(dummyGisuList)
    }

    private fun getMyProfile() {
        // 더미 유저 정보 사용
        val dummyUserInfo = UserInfo(
            id = 1L,
            name = "김철수",
            nickname = "철수왕",
            schoolId = 1L,
            schoolName = "홍익대학교",
            profileImageLink = "",
            status = "ACTIVE",
            roles = listOf(
                UserRole(
                    id = 1L,
                    challengerId = 1L,
                    roleType = "STAFF",
                    chapterId = 1L,
                    chapterName = "서울",
                    organizationType = "CENTRAL",
                    organizationId = null,
                    responsiblePart = "PM 파트",
                    gisuId = 9L,
                    gisu = 9L
                )
            )
        )
        updateChipList(createChipsFromUserInfo(dummyUserInfo))
    }

    fun getNoticeList(
        chapterId: Long? = null,
        schoolId: Long? = null,
        part: String? = null,
        isRefresh: Boolean = true
    ) = viewModelScope.launch {
        val state = uiState.value

        if (state.isPageLoading || (!isRefresh && state.isLastPage)) return@launch

        updateState { copy(isPageLoading = true) }

        // 더미 데이터 사용 - 필터링 로직 적용
        val filteredNotices = dummyNotices.filter { notice ->
            val matchesGisu = notice.targetInfo.targetGisuId.toLong() == state.selectedGisu
            val matchesChapter = chapterId == null || notice.targetInfo.targetChapterId?.toLong() == chapterId
            val matchesSchool = schoolId == null || notice.targetInfo.targetSchoolId?.toLong() == schoolId
            val matchesPart = part == null || notice.targetInfo.targetParts.contains(part)
            matchesGisu && matchesChapter && matchesSchool && matchesPart
        }

        // 페이지네이션 적용
        val pageSize = 5
        val pageToFetch = if (isRefresh) 0 else state.currentPage
        val startIndex = pageToFetch * pageSize
        val endIndex = minOf(startIndex + pageSize, filteredNotices.size)
        val paginatedNotices = if (startIndex < filteredNotices.size) {
            filteredNotices.subList(startIndex, endIndex)
        } else {
            emptyList()
        }

        val hasNext = endIndex < filteredNotices.size

        updateState {
            copy(
                noticeList = if (isRefresh) paginatedNotices else noticeList + paginatedNotices,
                currentPage = pageToFetch + 1,
                isPageLoading = false,
                isLastPage = !hasNext
            )
        }
    }

    fun loadNextPage() {
        if (!uiState.value.isPageLoading && !uiState.value.isLastPage) {
            getNoticeList(isRefresh = false)
        }
    }
}

data class NoticeUiState(
    val isShowDropDown: Boolean = false,
    val isShowSubChip: Boolean = false,
    val nowTitle: String = "",
    val selectedGisu: Long = 0,
    val selectedSubChip: String = "파트",
    val dropdownList: List<GisuItem> = emptyList(),
    val chipList: List<NoticeChipState> = emptyList(),
    val noticeList: List<NoticeSummary> = emptyList(),
    val currentPage: Int = 0,
    val isPageLoading: Boolean = false,
    val isLastPage: Boolean = false,
    val canWriteNotice: Boolean = false
) : UiState

sealed interface NoticeEvent : UiEvent {
    object MoveToWriteEvent : NoticeEvent

    data class MoveToSearchEvent(val gisuId: Long) : NoticeEvent
}
