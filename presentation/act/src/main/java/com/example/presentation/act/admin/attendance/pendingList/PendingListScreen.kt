package com.example.presentation.act.admin.attendance.pendingList

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTheme
import com.umc.component.theme.UmcTypographyTokens.CalloutBold
import com.umc.component.theme.UmcTypographyTokens.Footnote
import com.umc.component.theme.UmcTypographyTokens.SubheadlineBold
import com.umc.component.theme.UmcTypographyTokens.Title3Bold
import com.umc.component.theme.neutral000
import com.umc.component.theme.neutral100
import com.umc.component.theme.neutral200
import com.umc.component.theme.neutral300
import com.umc.component.theme.neutral600
import com.umc.component.theme.neutral700
import com.umc.component.theme.neutral800
import com.umc.domain.model.act.check.AdminPendingUser

@Composable
fun PendingListRoute() {
    PendingListScreen(users = sampleList())
}


@Composable
fun PendingListScreen(
    users: List<AdminPendingUser>,
    onSelectApproveClick: () -> Unit = {},
    onReasonClick: (AdminPendingUser) -> Unit = {},
    onRejectClick: (AdminPendingUser) -> Unit = {},
    onApproveClick: (AdminPendingUser) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(neutral000())
            .padding(horizontal = 16.dp)
    ) {
        DragHeader()

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            UText(
                text = AppStrings.ADMIN_CHECK_STATS_PENDING_TITLE,
                style = Title3Bold,
                color = neutral800()
            )
            UButton(
                prevIcon = painterResource(id = R.drawable.ic_check_success),
                prevIconTint = neutral800(),
                prevIconSize = DpSize(18.dp, 18.dp),
                text = AppStrings.ADMIN_CHECK_STATS_CHOOSE_PENDING,
                textStyle = SubheadlineBold,
                textColor = neutral700(),
                backgroundColor = neutral100(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                cornerRadius = 8.dp,
                onClick = onSelectApproveClick
            )
        }

        Spacer(Modifier.height(24.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 520.dp),
        ) {
            itemsIndexed(items = users, key = { _, user -> user.id }) { index, user ->
                HorizontalDivider(modifier = Modifier.fillMaxWidth().height(1.dp), color = neutral200())

                PendingUserRow(
                    user = user,
                    onReasonClick = { onReasonClick(user) },
                    onRejectClick = { onRejectClick(user) },
                    onApproveClick = { onApproveClick(user) }
                )
            }
        }
    }
}

@Composable
private fun DragHeader(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(neutral600())
        )
    }
}

@Composable
private fun PendingUserRow(
    user: AdminPendingUser,
    onReasonClick: () -> Unit,
    onRejectClick: () -> Unit,
    onApproveClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .border(width = 1.dp, color = neutral300(), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_person),
                contentDescription = null,
                tint = neutral300(),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            UText(
                text = "${user.name}(${user.nickname})",
                style = CalloutBold,
                color = neutral800()
            )
            Spacer(modifier = Modifier.height(4.dp))
            UText(
                text = "${user.university}  ${user.requestTime} 요청",
                style = Footnote,
                color = neutral600()
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if(user.hasLateReason) {
                ActionIcon(
                    iconRes = R.drawable.ic_warning,
                    onClick = onReasonClick
                )
            }

            ActionIcon(
                iconRes = R.drawable.ic_check_failed,
                onClick = onRejectClick
            )
            ActionIcon(
                iconRes = R.drawable.ic_check_success,
                onClick = onApproveClick
            )
        }
    }
}

@Composable
private fun ActionIcon(
    iconRes: Int,
    onClick: () -> Unit,
    tint: Color = Color.Unspecified,
) {
    Icon(
        painter = painterResource(id = iconRes),
        contentDescription = null,
        tint = tint,
        modifier = Modifier
            .size(30.dp)
            .clickable(onClick = onClick)
            .padding(3.dp)
    )
}

private fun sampleList(): List<AdminPendingUser> = listOf(
    AdminPendingUser(
        id = 1L,
        name = "홍길동",
        nickname = "닉네임",
        university = "중앙대학교",
        profileImageUrl = null,
        requestTime = "14:05",
        hasLateReason = false,
        lateReason = "지하철 지연으로 늦었습니다."
    ),
    AdminPendingUser(
        id = 2L,
        name = "홍길동",
        nickname = "닉네임",
        university = "중앙대학교",
        profileImageUrl = null,
        requestTime = "14:05",
        hasLateReason = true,
        lateReason = "병원 방문으로 지각했습니다."
    ),
    AdminPendingUser(
        id = 3L,
        name = "홍길동",
        nickname = "닉네임",
        university = "중앙대학교",
        profileImageUrl = null,
        requestTime = "14:05",
        hasLateReason = true,
        lateReason = "수업 종료가 지연됐습니다."
    ),
    AdminPendingUser(
        id = 4L,
        name = "홍길동",
        nickname = "닉네임",
        university = "중앙대학교",
        profileImageUrl = null,
        requestTime = "14:05",
        hasLateReason = true,
        lateReason = "버스 환승 정체로 늦었습니다."
    ),
    AdminPendingUser(
        id = 5L,
        name = "홍길동",
        nickname = "닉네임",
        university = "중앙대학교",
        profileImageUrl = null,
        requestTime = "14:05",
        hasLateReason = true,
        lateReason = "팀 미팅이 길어졌습니다."
    ),
    AdminPendingUser(
        id = 6L,
        name = "홍길동",
        nickname = "닉네임",
        university = "중앙대학교",
        profileImageUrl = null,
        requestTime = "14:05",
        hasLateReason = true,
        lateReason = "교통 체증으로 지연되었습니다."
    ),
    AdminPendingUser(
        id = 7L,
        name = "홍길동",
        nickname = "닉네임",
        university = "중앙대학교",
        profileImageUrl = null,
        requestTime = "14:05",
        hasLateReason = true,
        lateReason = "이동 중 분실물 처리로 늦었습니다."
    )
)

@Preview(showBackground = false)
@Composable
private fun PendingListScreenPreview() {
    UmcTheme(darkTheme = false) {
        PendingListScreen(users = sampleList())
    }
}
