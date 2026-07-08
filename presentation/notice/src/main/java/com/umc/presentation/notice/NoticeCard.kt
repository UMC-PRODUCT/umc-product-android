package com.umc.presentation.notice

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.green100
import com.umc.component.theme.green600
import com.umc.component.theme.grey000
import com.umc.component.theme.grey100
import com.umc.component.theme.grey200
import com.umc.component.theme.grey500
import com.umc.component.theme.grey600
import com.umc.component.theme.grey950
import com.umc.component.theme.indigo100
import com.umc.component.theme.indigo600
import com.umc.component.theme.red500
import com.umc.component.theme.yellow100
import com.umc.component.theme.yellow600
import com.umc.domain.model.notice.NoticeSummary
import com.umc.domain.model.notice.NoticeTarget

/** 공지 목록 카드. 공지 탭·검색 결과에서 공용으로 사용 */
@Composable
fun NoticeCard(
    notice: NoticeSummary,
    isRead: Boolean = false,
    onClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(grey000())
            .border(1.dp, grey200(), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            NoticeTags(target = notice.targetInfo)

            Spacer(modifier = Modifier.weight(1f))

            if (notice.shouldSendNotification) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_notice_notification),
                    contentDescription = null,
                    tint = Color.Unspecified,
                )
            }

            if (!isRead) {
                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(red500()),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        UText(
            text = notice.title,
            style = UmcTypographyTokens.HeadlineBold,
            color = grey950(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(6.dp))

        UText(
            text = notice.content,
            style = UmcTypographyTokens.Subheadline,
            color = grey600(),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            UText(
                text = notice.authorNickname.ifEmpty { notice.authorName },
                style = UmcTypographyTokens.Footnote,
                color = grey500(),
            )

            Spacer(modifier = Modifier.weight(1f))

            UText(
                text = "${formatNoticeDate(notice.createdAt)} | ${AppStrings.NOTICE_VIEW_COUNT} ${notice.viewCount}",
                style = UmcTypographyTokens.Footnote,
                color = grey500(),
            )
        }
    }
}

/** 공지 대상 정보에 따라 중앙/지부/학교/파트 태그 노출 */
@Composable
private fun NoticeTags(target: NoticeTarget) {
    val isCentral = target.targetGisuId != 0 ||
            (target.targetChapterId == null && target.targetSchoolId == null && target.targetParts.isEmpty())

    Row(verticalAlignment = Alignment.CenterVertically) {
        if (isCentral) {
            NoticeTag(text = AppStrings.CENTRAL, backgroundColor = yellow100(), textColor = yellow600())
            Spacer(modifier = Modifier.width(6.dp))
        }

        if (target.targetChapterId != null) {
            NoticeTag(text = AppStrings.BRANCH, backgroundColor = grey100(), textColor = grey600())
            Spacer(modifier = Modifier.width(6.dp))
        }

        if (target.targetSchoolId != null) {
            NoticeTag(text = AppStrings.SCHOOL, backgroundColor = indigo100(), textColor = indigo600())
            Spacer(modifier = Modifier.width(6.dp))
        }

        if (target.targetParts.isNotEmpty()) {
            NoticeTag(text = AppStrings.PART, backgroundColor = green100(), textColor = green600())
        }
    }
}

@Composable
private fun NoticeTag(
    text: String,
    backgroundColor: Color,
    textColor: Color,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        UText(
            text = text,
            style = UmcTypographyTokens.Caption1Bold,
            color = textColor,
        )
    }
}

/** createdAt(ISO)에서 yyyy.MM.dd 형태로 변환 */
fun formatNoticeDate(createdAt: String): String {
    return createdAt.take(10).replace("-", ".")
}

@Preview(showBackground = true)
@Composable
private fun NoticeCardPreview() {
    NoticeCard(
        notice = NoticeSummary(
            id = 1L,
            title = "3월 정기 세션 뒤풀이 장소 안내",
            content = "이번주 토요일 세션 후 뒤풀이가 있습니다. 장소는 강남역 인근 맛있는 고기집입니다.",
            shouldSendNotification = true,
            viewCount = 1000,
            createdAt = "2024-03-01T00:00:00",
            targetInfo = NoticeTarget(
                targetGisuId = 1,
                targetSchoolId = 1,
                targetParts = listOf("ANDROID"),
            ),
            authorChallengerId = 1L,
            authorNickname = "중앙대 운영진",
            authorName = "홍길동",
        ),
    )
}
