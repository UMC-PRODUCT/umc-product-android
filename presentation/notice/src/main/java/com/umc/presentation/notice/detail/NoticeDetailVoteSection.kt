package com.umc.presentation.notice.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey100
import com.umc.component.theme.grey200
import com.umc.component.theme.grey300
import com.umc.component.theme.grey400
import com.umc.component.theme.grey500
import com.umc.component.theme.grey600
import com.umc.component.theme.grey700
import com.umc.component.theme.grey800
import com.umc.component.theme.grey950
import com.umc.component.theme.indigo400
import com.umc.component.theme.indigo500
import com.umc.component.theme.yellow100
import com.umc.component.theme.yellow300
import com.umc.component.theme.yellow500
import com.umc.component.theme.yellow600
import com.umc.domain.model.enums.NoticeVoteStatus
import com.umc.domain.model.notice.NoticeVote
import com.umc.domain.model.notice.NoticeVoteOption

/**
 * 공지 상세의 투표 카드 (연회색 채움 카드 + 상태 배지).
 * - 진행중·미투표: 체크박스 선택 → 투표하기
 * - 진행중·투표함: 결과(득표수·게이지) + 체크박스 재선택 → 다시 투표하기
 * - 마감: 투표했으면 결과 표시, 아니면 항목만 표시 (버튼 없음)
 */
@Composable
fun NoticeDetailVoteSection(
    vote: NoticeVote,
    selectedOptionIds: List<Long>,
    showResult: Boolean,
    voteStatus: NoticeVoteStatus,
    onClickOption: (NoticeVoteOption) -> Unit = {},
    onClickButton: () -> Unit = {},
    onClickVoteStatus: () -> Unit = {},
) {
    val isOpen = voteStatus == NoticeVoteStatus.OPEN
    val isClosed = voteStatus == NoticeVoteStatus.CLOSED
    val hasVoted = vote.mySelectedOptionIds.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(grey100())
            .padding(16.dp),
    ) {
        // 헤더: 투표 제목 + 상태 배지
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.ic_vote),
                contentDescription = null,
                tint = indigo500(),
                modifier = Modifier.size(24.dp),
            )

            Spacer(modifier = Modifier.width(8.dp))

            UText(
                text = vote.title.ifBlank { AppStrings.NOTICE_WRITE_VOTE },
                style = UmcTypographyTokens.HeadlineBold,
                color = grey950(),
                modifier = Modifier.weight(1f),
            )

            VoteStatusBadge(status = voteStatus)
        }

        Spacer(modifier = Modifier.height(8.dp))

        UText(
            text = voteConditionText(vote),
            style = UmcTypographyTokens.Footnote,
            color = grey600(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        val totalCount = vote.options.sumOf { it.voteCount }

        vote.options.forEachIndexed { index, option ->
            if (index > 0) Spacer(modifier = Modifier.height(12.dp))

            VoteOptionCard(
                option = option,
                totalCount = totalCount,
                isSelected = option.optionId in selectedOptionIds,
                isMine = option.optionId in vote.mySelectedOptionIds,
                showResult = showResult,
                showCheckbox = isOpen,
                onClick = { onClickOption(option) },
            )
        }

        // 진행 중일 때만 버튼 노출. 시작 전에는 비활성 투표하기
        if (!isClosed) {
            Spacer(modifier = Modifier.height(16.dp))

            val enabled = isOpen && selectedOptionIds.isNotEmpty()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (enabled) indigo500() else grey200())
                    .clickable { if (enabled) onClickButton() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                UText(
                    text = if (hasVoted) AppStrings.NOTICE_DETAIL_REVOTE else AppStrings.NOTICE_DETAIL_VOTE,
                    style = UmcTypographyTokens.HeadlineBold,
                    color = if (enabled) grey000() else grey400(),
                )
            }
        }

        // 결과 상태에서 참여 인원. 실명 투표는 투표 현황으로 이동 가능
        if (showResult) {
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable(enabled = !vote.isAnonymous) { onClickVoteStatus() },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                UText(
                    text = AppStrings.NOTICE_DETAIL_VOTE_PARTICIPANT_COUNT.format(vote.totalParticipants),
                    style = UmcTypographyTokens.Footnote,
                    color = grey600(),
                )

                if (!vote.isAnonymous) {
                    Spacer(modifier = Modifier.width(6.dp))

                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_next),
                        contentDescription = null,
                        tint = grey400(),
                    )
                }
            }
        }
    }
}

/** 진행중(노랑)/마감(회색 테두리)/진행 전 상태 배지 */
@Composable
private fun VoteStatusBadge(status: NoticeVoteStatus) {
    val (label, backgroundColor, borderColor, textColor) = when (status) {
        NoticeVoteStatus.OPEN -> VoteBadgeStyle(
            AppStrings.NOTICE_DETAIL_VOTE_BADGE_OPEN, yellow100(), yellow300(), yellow500(),
        )

        NoticeVoteStatus.CLOSED -> VoteBadgeStyle(
            AppStrings.NOTICE_DETAIL_VOTE_BADGE_CLOSED, grey000(), grey200(), grey600(),
        )

        NoticeVoteStatus.NOT_STARTED -> VoteBadgeStyle(
            AppStrings.NOTICE_DETAIL_VOTE_BADGE_NOT_STARTED, grey000(), grey200(), grey600(),
        )
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        UText(
            text = label,
            style = UmcTypographyTokens.FootnoteBold,
            color = textColor,
        )
    }
}

private data class VoteBadgeStyle(
    val label: String,
    val backgroundColor: Color,
    val borderColor: Color,
    val textColor: Color,
)

/** "실명 투표 • 단일 선택" 형태의 조건 요약 */
private fun voteConditionText(vote: NoticeVote): String {
    val anonymity = if (vote.isAnonymous) {
        AppStrings.NOTICE_WRITE_ANONYMITY
    } else {
        AppStrings.NOTICE_DETAIL_VOTE_REAL_NAME
    }
    val choice = if (vote.allowMultipleChoice) {
        AppStrings.NOTICE_DETAIL_VOTE_MULTIPLE_CHOICE
    } else {
        AppStrings.NOTICE_DETAIL_VOTE_SINGLE_CHOICE
    }
    return "$anonymity • $choice"
}

/**
 * 투표 항목 카드 (흰 배경).
 * 진행 중에는 왼쪽 체크박스로 선택, 결과 상태에서는 득표수와 게이지 표시.
 * 마감 후 내가 고른 항목은 체크 마크로 표시
 */
@Composable
private fun VoteOptionCard(
    option: NoticeVoteOption,
    totalCount: Int,
    isSelected: Boolean,
    isMine: Boolean,
    showResult: Boolean,
    showCheckbox: Boolean,
    onClick: () -> Unit = {},
) {
    val highlight = if (showCheckbox) isSelected else isMine

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(grey000())
            .border(
                width = 1.dp,
                color = if (highlight) indigo400() else grey300(),
                shape = RoundedCornerShape(8.dp),
            )
            .clickable(enabled = showCheckbox) { onClick() }
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showCheckbox) {
                Icon(
                    painter = painterResource(
                        id = if (isSelected) R.drawable.ic_check_box else R.drawable.ic_check_box_empty
                    ),
                    contentDescription = null,
                    tint = if (isSelected) indigo400() else grey400(),
                    modifier = Modifier.size(24.dp),
                )
            } else if (showResult && isMine) {
                // 마감 후 내가 고른 항목 표시
                Icon(
                    painter = painterResource(id = R.drawable.ic_toast_check),
                    contentDescription = null,
                    tint = indigo500(),
                    modifier = Modifier.size(24.dp),
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            UText(
                text = option.content,
                style = UmcTypographyTokens.Subheadline,
                color = grey950(),
                modifier = Modifier.weight(1f),
            )

            if (showResult) {
                UText(
                    text = AppStrings.NOTICE_VOTE_PEOPLE_COUNT.format(option.voteCount),
                    style = if (isMine) UmcTypographyTokens.SubheadlineBold else UmcTypographyTokens.Footnote,
                    color = if (isMine) indigo500() else grey600(),
                )
            }
        }

        if (showResult) {
            Spacer(modifier = Modifier.height(12.dp))

            val rate = if (totalCount > 0) option.voteCount.toFloat() / totalCount else 0f

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(grey200()),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(rate)
                        .clip(CircleShape)
                        .background(if (isMine) indigo500() else grey700()),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VoteSectionInProgressPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // 진행중 · 미투표 (하나 선택)
        NoticeDetailVoteSection(
            vote = NoticeVote(
                voteId = 1L,
                title = "회식 메뉴 투표",
                options = listOf(
                    NoticeVoteOption(optionId = 1L, content = "삼겹살 목살"),
                    NoticeVoteOption(optionId = 2L, content = "치킨"),
                    NoticeVoteOption(optionId = 3L, content = "피자"),
                ),
            ),
            selectedOptionIds = listOf(3L),
            showResult = false,
            voteStatus = NoticeVoteStatus.OPEN,
        )

        // 진행중 · 투표함 (결과 + 다시 투표하기)
        NoticeDetailVoteSection(
            vote = NoticeVote(
                voteId = 1L,
                title = "회식 메뉴 투표",
                totalParticipants = 72,
                options = listOf(
                    NoticeVoteOption(optionId = 1L, content = "삼겹살 목살", voteCount = 12),
                    NoticeVoteOption(optionId = 2L, content = "치킨", voteCount = 30),
                    NoticeVoteOption(optionId = 3L, content = "피자", voteCount = 30),
                ),
                mySelectedOptionIds = listOf(1L),
            ),
            selectedOptionIds = listOf(1L),
            showResult = true,
            voteStatus = NoticeVoteStatus.OPEN,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun VoteSectionClosedPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // 마감 · 투표함
        NoticeDetailVoteSection(
            vote = NoticeVote(
                voteId = 1L,
                title = "회식 메뉴 투표",
                totalParticipants = 72,
                options = listOf(
                    NoticeVoteOption(optionId = 1L, content = "삼겹살 목살", voteCount = 12),
                    NoticeVoteOption(optionId = 2L, content = "치킨", voteCount = 30),
                ),
                mySelectedOptionIds = listOf(1L),
            ),
            selectedOptionIds = emptyList(),
            showResult = true,
            voteStatus = NoticeVoteStatus.CLOSED,
        )

        // 마감 · 미투표 (결과 비공개)
        NoticeDetailVoteSection(
            vote = NoticeVote(
                voteId = 1L,
                title = "회식 메뉴 투표",
                options = listOf(
                    NoticeVoteOption(optionId = 1L, content = "삼겹살 목살"),
                    NoticeVoteOption(optionId = 2L, content = "치킨"),
                ),
            ),
            selectedOptionIds = emptyList(),
            showResult = false,
            voteStatus = NoticeVoteStatus.CLOSED,
        )
    }
}
