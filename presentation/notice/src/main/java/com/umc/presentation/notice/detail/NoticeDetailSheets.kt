package com.umc.presentation.notice.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
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
import com.umc.component.theme.grey950
import com.umc.component.theme.green500
import com.umc.component.theme.indigo500
import com.umc.component.theme.red500
import com.umc.domain.model.notice.ChallengerReadInfo
import com.umc.domain.model.notice.NoticeReadStatistics
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import androidx.compose.material3.Icon
import com.umc.component.theme.grey600

/**
 * 공지 열람 현황 바텀시트 (작성자 전용).
 * 미확인/확인 세그먼트 탭으로 인원을 나눠 보여주고, 미확인 탭에서 재알림을 보낼 수 있다.
 * 재알림 발송 후에는 각 인원에 빨간 점이 표시되고 버튼이 비활성화됨
 */
@Composable
fun NoticeReadStatusSheetContent(
    statistics: NoticeReadStatistics?,
    unreadList: ImmutableList<ChallengerReadInfo>,
    readList: ImmutableList<ChallengerReadInfo>,
    isReminderSent: Boolean,
    isSendingReminder: Boolean,
    onLoadMore: (isRead: Boolean) -> Unit = {},
    onClickSendReminder: () -> Unit = {},
) {
    // true = 확인 탭. 기본은 미확인 탭 (재알림 대상 관리가 주 목적)
    var isReadTab by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            UText(
                text = AppStrings.NOTICE_DETAIL_NOW_CONFIRMED_PEOPLE,
                style = UmcTypographyTokens.Title3Bold,
                color = grey950(),
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(27.dp))

        // 미확인/확인 세그먼트 탭
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(grey100())
                .padding(4.dp),
        ) {
            ReadStatusSegment(
                label = AppStrings.NOTICE_DETAIL_READ_TAB_UNCONFIRMED,
                count = statistics?.unreadCount ?: unreadList.size,
                isSelected = !isReadTab,
                onClick = { isReadTab = false },
                modifier = Modifier.weight(1f),
            )

            ReadStatusSegment(
                label = AppStrings.NOTICE_DETAIL_READ_TAB_CONFIRMED,
                count = statistics?.readCount ?: readList.size,
                isSelected = isReadTab,
                onClick = { isReadTab = true },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        val people = if (isReadTab) readList else unreadList

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 160.dp, max = 420.dp),
        ) {
            itemsIndexed(people) { index, person ->
                // 목록 끝에 도달하면 다음 페이지 로드
                if (index == people.lastIndex) {
                    onLoadMore(isReadTab)
                }

                if (index > 0) Spacer(modifier = Modifier.height(8.dp))

                ReadStatusPersonRow(
                    person = person,
                    isReadTab = isReadTab,
                    isReminderSent = isReminderSent,
                )
            }

            if (people.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        UText(
                            text = AppStrings.NOTICE_VOTE_PARTICIPANTS_EMPTY,
                            style = UmcTypographyTokens.Subheadline,
                            color = grey400(),
                        )
                    }
                }
            }
        }

        // 재알림은 미확인 탭에서만
        if (!isReadTab) {
            Spacer(modifier = Modifier.height(16.dp))

            val enabled = !isReminderSent && !isSendingReminder && unreadList.isNotEmpty()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (enabled) grey950() else grey100())
                    .clickable { if (enabled) onClickSendReminder() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                UText(
                    text = AppStrings.NOTICE_DETAIL_SEND_NOTIFICATION,
                    style = UmcTypographyTokens.HeadlineBold,
                    color = if (enabled) grey000() else grey400(),
                )
            }
        }
    }
}

/** 세그먼트 탭 조각. 활성 시 흰 알약 배경 + 카운트 강조 */
@Composable
private fun ReadStatusSegment(
    label: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(if (isSelected) grey000() else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UText(
            text = label,
            style = UmcTypographyTokens.CalloutBold,
            color = if (isSelected) grey950() else grey400(),
        )

        Spacer(modifier = Modifier.width(4.dp))

        UText(
            text = "$count",
            style = UmcTypographyTokens.CalloutBold,
            color = if (isSelected) indigo500() else grey400(),
        )
    }
}

/** 인원 행 (연회색 카드). 확인 탭은 초록 체크, 재알림 발송 후 미확인 인원은 빨간 점 표시 */
@Composable
private fun ReadStatusPersonRow(
    person: ChallengerReadInfo,
    isReadTab: Boolean,
    isReminderSent: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(grey100())
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProfileImage(url = person.profileImageUrl, size = 20)

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            UText(
                text = formatNameWithNickname(name = person.name, nickname = ""),
                style = UmcTypographyTokens.CalloutBold,
                color = grey950(),
            )

            Spacer(modifier = Modifier.height(4.dp))

            UText(
                text = listOf(person.chapterName, person.schoolName)
                    .filter { it.isNotBlank() }
                    .joinToString(" "),
                style = UmcTypographyTokens.Footnote,
                color = grey600(),
            )
        }

        if (isReadTab) {
            Icon(
                painter = painterResource(id = R.drawable.ic_check_success),
                contentDescription = null,
                tint = green500(),
                modifier = Modifier.size(24.dp),
            )
        } else if (isReminderSent) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(red500()),
            )
        }
    }
}

/** "이름(닉네임)" 표기. 닉네임이 없으면 이름만 */
internal fun formatNameWithNickname(name: String, nickname: String): String {
    return if (nickname.isBlank() || nickname == name) {
        name
    } else {
        AppStrings.NOTICE_DETAIL_NAME_NICKNAME_FORMAT.format(name, nickname)
    }
}

/** 원형 프로필 이미지. url이 없거나 프리뷰에서는 기본 사람 아이콘 표시 */
@Composable
internal fun ProfileImage(url: String, size: Int) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(grey000())
            .border(1.dp, grey200(), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        // 프리뷰(레이아웃립)에서는 coil을 로드하지 않으므로 기본 아이콘으로 대체
        if (url.isBlank() || LocalInspectionMode.current) {
            Icon(
                painter = painterResource(id = R.drawable.ic_person),
                contentDescription = null,
                tint = grey300(),
                modifier = Modifier.size((size * 0.6).dp),
            )
        } else {
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReadStatusSheetPreview() {
    NoticeReadStatusSheetContent(
        statistics = NoticeReadStatistics(totalCount = 15, readCount = 10, unreadCount = 5, readRate = 66.7),
        unreadList = List(4) {
            ChallengerReadInfo(it.toLong(), "홍길동", "", "ANDROID", 1L, "대학교", 1L, "지부이름")
        }.toImmutableList(),
        readList = persistentListOf(),
        isReminderSent = true,
        isSendingReminder = false,
    )
}
