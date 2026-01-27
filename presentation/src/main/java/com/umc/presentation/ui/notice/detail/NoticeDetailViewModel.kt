package com.umc.presentation.ui.notice.detail

import com.umc.domain.model.enums.NoticeCategory
import com.umc.domain.model.enums.VoteCondition
import com.umc.domain.model.enums.VoteState
import com.umc.domain.model.notice.NoticeDetail
import com.umc.domain.model.notice.User
import com.umc.domain.model.notice.Vote
import com.umc.domain.model.notice.VoteItem
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NoticeDetailViewModel @Inject
constructor() : BaseViewModel<NoticeFragmentUiState, NoticeFragmentEvent>(
    NoticeFragmentUiState()
) {

    init {
        updateState {
            copy(detail = getDummy())
        }
    }

    fun onClickBackPressed() {
        emitEvent(NoticeFragmentEvent.MoveBackPressedEvent)
    }

    private fun getDummy(): NoticeDetail {
        return NoticeDetail(
            mustRead = true,
            category = NoticeCategory.CENTRAL_OFFICE,
            title = "[필독] 12기 활동 규정 안내",
            profileImage = "",
            author = "운영진",
            date = "2026.01.01",
            viewCount = 100,
            receiver = "12기/전체",
            content = "대충 내용이 들어갈 자리",
            imageList = emptyList(),
            link = "https://example.com/guideline",
            vote = Vote(
                title = "회식 메뉴 투표",
                state = VoteState.PROGRESS,
                condition = listOf(VoteCondition.BLINDNESS, VoteCondition.SINGLE_VOTE),
                conditionText = VoteCondition.buildVoteConditionText(
                    listOf(
                        VoteCondition.BLINDNESS,
                        VoteCondition.SINGLE_VOTE
                    )
                ),
                item = listOf(
                    VoteItem(
                        isChecked = false,
                        name = "삼겹살 목살"
                    ),
                    VoteItem(
                        isChecked = false,
                        name = "치킨 피자"
                    ),
                    VoteItem(
                        isChecked = false,
                        name = "족발 보쌈"
                    )
                )
            ),
            allReceiverCount = 1000,
            nowReceiverCount = 801,
            receiverText = "/ 1000명 (80%)",
            userList = listOf(
                User(
                    id = 1,
                    name = "김서준",
                    nickName = "서니",
                    branch = "서울",
                    school = "경희대학교",
                    part = "Android",
                    isSendNotification = true,
                    isCheck = false
                ),
                User(
                    id = 2,
                    name = "이하은",
                    nickName = "하니",
                    branch = "부산",
                    school = "부산대학교",
                    part = "Design",
                    isSendNotification = false,
                    isCheck = true
                ),
                User(
                    id = 3,
                    name = "박민준",
                    nickName = "민",
                    branch = "대구",
                    school = "영남대학교",
                    part = "Web",
                    isSendNotification = true,
                    isCheck = true
                ),
                User(
                    id = 4,
                    name = "최유진",
                    nickName = "유지니",
                    branch = "인천",
                    school = "인하대학교",
                    part = "PM",
                    isSendNotification = false,
                    isCheck = false
                ),
                User(
                    id = 5,
                    name = "정도현",
                    nickName = "도도",
                    branch = "대전",
                    school = "충남대학교",
                    part = "Node.js",
                    isSendNotification = true,
                    isCheck = false
                ),
                User(
                    id = 6,
                    name = "윤지아",
                    nickName = "지아",
                    branch = "광주",
                    school = "전남대학교",
                    part = "iOS",
                    isSendNotification = true,
                    isCheck = true
                ),
                User(
                    id = 7,
                    name = "한지훈",
                    nickName = "지훈",
                    branch = "울산",
                    school = "울산대학교",
                    part = "SpringBoot",
                    isSendNotification = false,
                    isCheck = false
                ),
                User(
                    id = 8,
                    name = "오수빈",
                    nickName = "수비니",
                    branch = "수원",
                    school = "아주대학교",
                    part = "Android",
                    isSendNotification = true,
                    isCheck = true
                ),
                User(
                    id = 9,
                    name = "임현우",
                    nickName = "현우",
                    branch = "세종",
                    school = "고려대학교(세종)",
                    part = "Web",
                    isSendNotification = false,
                    isCheck = false
                ),
                User(
                    id = 10,
                    name = "강예린",
                    nickName = "예린",
                    branch = "제주",
                    school = "제주대학교",
                    part = "Design",
                    isSendNotification = true,
                    isCheck = false
                )
            )

        )
    }

    fun onClickVoteItem(clicked: VoteItem) {
        updateState {
            val detail = uiState.value.detail
            val vote = detail.vote

            val isSingle = VoteCondition.SINGLE_VOTE in vote.condition
            val isMultiple = VoteCondition.MULTIPLE_VOTE in vote.condition

            val newItems = when {
                isSingle -> {
                    vote.item.map { item ->
                        item.copy(isChecked = item.name == clicked.name)
                    }

                    val clickedWasChecked =
                        vote.item.any { it.name == clicked.name && it.isChecked }
                    vote.item.map { item ->
                        when {
                            item.name == clicked.name -> item.copy(isChecked = !clickedWasChecked)
                            else -> item.copy(isChecked = false)
                        }
                    }
                }

                isMultiple -> {
                    vote.item.map { item ->
                        if (item.name == clicked.name) item.copy(isChecked = !item.isChecked) else item
                    }
                }

                else -> {
                    vote.item.map { item ->
                        if (item.name == clicked.name) item.copy(isChecked = !item.isChecked) else item
                    }
                }
            }

            copy(
                detail = detail.copy(
                    vote = vote.copy(
                        item = newItems
                    )
                )
            )
        }
    }

    fun onClickShowBottomSheet() {
        emitEvent(NoticeFragmentEvent.ShowBottomSheetEvent)
    }


}


data class NoticeFragmentUiState(
    val detail: NoticeDetail = NoticeDetail()
) : UiState

sealed interface NoticeFragmentEvent : UiEvent {
    object ShowBottomSheetEvent : NoticeFragmentEvent
    object MoveBackPressedEvent : NoticeFragmentEvent
}