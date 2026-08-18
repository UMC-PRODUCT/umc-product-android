package com.umc.presentation.study.admin.group.component

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
import com.umc.component.theme.UmcTypographyTokens.CalloutBold

/**
 * 스터디 그룹 카드에서 사용하는 일정 등록 버튼
 *
 * 클릭 시 선택한 스터디 그룹의 일정 등록 화면으로 이동합니다.
 */
@Composable
fun AdminStudyGroupScheduleButton(
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = indigo400(),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(
                onClick = onClick
            )
            .padding(
                horizontal = 12.dp,
                vertical = 10.dp
            ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 일정 등록 아이콘
        Icon(
            painter = painterResource(
                R.drawable.ic_study_caendar
            ),
            contentDescription = null,
            tint = indigo500(),
            modifier = Modifier.size(20.dp)
        )

        Spacer(
            modifier = Modifier.width(4.dp)
        )

        UText(
            text = "스터디 일정 등록하기",
            style = CalloutBold,
            color = grey700()
        )
    }
}