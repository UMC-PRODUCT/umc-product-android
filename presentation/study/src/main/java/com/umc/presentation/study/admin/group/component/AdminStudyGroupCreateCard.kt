package com.umc.presentation.study.admin.group.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.*
import com.umc.component.theme.UmcTypographyTokens.HeadlineBold

/**
 * 관리자 스터디 그룹 목록의 그룹 생성 카드
 *
 * 클릭 시 새로운 스터디 그룹 생성 화면으로 이동합니다.
 */
@Composable
fun AdminStudyGroupCreateCard(
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(
                top = 16.dp,
                bottom = 16.dp
            )
            .background(
                grey000(),
                RoundedCornerShape(8.dp)
            )
            .border(
                1.dp,
                grey000(),
                RoundedCornerShape(8.dp)
            )
            .clickable {
                onClick()
            }
            .padding(
                horizontal = 16.dp,
                vertical = 16.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 스터디 그룹 생성 아이콘
        Icon(
            painter = painterResource(
                R.drawable.ic_book_filled
            ),
            contentDescription = null,
            tint = indigo500(),
            modifier = Modifier.size(24.dp)
        )

        Spacer(
            modifier = Modifier.width(8.dp)
        )

        UText(
            text = "스터디 그룹 생성하기",
            style = HeadlineBold,
            color = grey800(),
            modifier = Modifier.weight(1f)
        )

        // 생성 화면 이동 아이콘
        Icon(
            painter = painterResource(
                R.drawable.ic_arrow_next
            ),
            contentDescription = null,
            tint = grey400(),
            modifier = Modifier.size(14.dp)
        )
    }
}