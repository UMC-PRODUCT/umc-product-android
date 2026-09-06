package com.umc.presentation.notice.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey200
import com.umc.component.theme.grey900
import com.umc.component.theme.grey950
import com.umc.component.theme.indigo100
import com.umc.component.theme.indigo500
import com.umc.domain.model.notice.NoticeVoteParticipant
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

/**
 * 투표 현황 전체 화면. 옵션별 참여자를 2열 그리드로 표시 (실명 투표 전용)
 */
@Composable
fun NoticeVoteStatusScreen(
    sections: ImmutableList<VoteOptionParticipants>,
    isLoading: Boolean,
    onClickBack: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(grey000()),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 22.dp, top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier
                    .padding(12.dp)
                    .clickable { onClickBack() },
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = null,
                tint = Color.Unspecified,
            )

            Spacer(modifier = Modifier.width(4.dp))

            UText(
                text = AppStrings.NOTICE_VOTE_PARTICIPANTS_TITLE,
                style = UmcTypographyTokens.Title2Bold,
                color = grey950(),
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = indigo500(), trackColor = indigo100())
            }
            return@Column
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            sections.forEachIndexed { index, section ->
                if (index > 0) {
                    Spacer(modifier = Modifier.height(24.dp))

                    HorizontalDivider(thickness = 1.dp, color = grey200())

                    Spacer(modifier = Modifier.height(24.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    UText(
                        text = section.optionTitle,
                        style = UmcTypographyTokens.HeadlineBold,
                        color = grey950(),
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    UText(
                        text = AppStrings.NOTICE_VOTE_PEOPLE_COUNT.format(section.participants.size),
                        style = UmcTypographyTokens.Subheadline,
                        color = indigo500(),
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 참여자 2열 그리드
                section.participants.chunked(2).forEach { rowParticipants ->
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth()) {
                        rowParticipants.forEach { participant ->
                            VoteParticipantCell(
                                participant = participant,
                                modifier = Modifier.weight(1f),
                            )
                        }

                        if (rowParticipants.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun VoteParticipantCell(
    participant: NoticeVoteParticipant,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProfileImage(url = participant.profileImageUrl, size = 32)

        Spacer(modifier = Modifier.width(8.dp))

        UText(
            text = formatNameWithNickname(name = participant.name, nickname = participant.nickname),
            style = UmcTypographyTokens.BodyBold,
            color = grey950(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NoticeVoteStatusScreenPreview() {
    NoticeVoteStatusScreen(
        sections = persistentListOf(
            VoteOptionParticipants(
                optionId = 1L,
                optionTitle = "항목 1",
                participants = List(4) {
                    NoticeVoteParticipant(memberId = it.toLong(), nickname = "닉네임", name = "홍길동")
                }.toImmutableList(),
            ),
            VoteOptionParticipants(
                optionId = 2L,
                optionTitle = "항목 2",
                participants = List(3) {
                    NoticeVoteParticipant(memberId = it.toLong(), nickname = "닉네임", name = "홍길동")
                }.toImmutableList(),
            ),
        ),
        isLoading = false,
    )
}
