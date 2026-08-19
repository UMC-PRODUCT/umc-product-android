package com.umc.presentation.study.admin.group.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.umc.component.component.UText
import com.umc.component.theme.*
import com.umc.component.theme.UmcTypographyTokens.Caption1Bold
import com.umc.presentation.study.admin.group.AdminStudyGroupMemberUiModel

/**
 * 스터디 그룹 카드에서 개별 스터디원을 표시하는 칩
 *
 * 멤버의 프로필 이미지와 이름을 표시합니다.
 */
@Composable
fun AdminStudyGroupMemberChip(
    member: AdminStudyGroupMemberUiModel,
) {
    Row(
        modifier = Modifier
            .background(grey000(), CircleShape)
            .border(
                1.dp,
                grey200(),
                CircleShape
            )
            .padding(
                horizontal = 8.dp,
                vertical = 6.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AdminStudyGroupProfileImage(
            profileImageUrl = member.profileImageUrl,
            size = 18,
        )

        Spacer(
            modifier = Modifier.width(4.dp)
        )

        UText(
            text = member.name,
            style = Caption1Bold,
            color = grey800()
        )
    }
}