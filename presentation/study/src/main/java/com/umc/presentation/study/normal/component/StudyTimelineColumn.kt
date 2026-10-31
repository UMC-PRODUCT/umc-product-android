package com.umc.presentation.study.normal.component


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.*

/**
 * 각 커리큘럼 아이템 왼쪽에 표시되는 타임라인 컬럼
 *
 * - 잠금(미공개 주차): 자물쇠 아이콘 (흐리게)
 * - 그 외: 주차 번호 원형 뱃지
 */
@Composable
fun StudyTimelineColumn(
    week: Int,
    isLocked: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.width(32.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        if (isLocked) {
            Icon(
                painter = painterResource(R.drawable.ic_locked),
                // 상태 배지가 없어져 잠금 아이콘이 유일한 단서라 TalkBack 이 미공개 주차임을 읽도록 한다
                contentDescription = AppStrings.STUDY_LOCKED_WEEK_DESCRIPTION,
                modifier = Modifier.size(24.dp),
                tint = grey400()
            )
        } else {
            // 주차 번호 원형
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(main500()),
                contentAlignment = Alignment.Center
            ) {
                UText(
                    text = week.toString(),
                    style = UmcTypographyTokens.Caption1Bold,
                    color = grey50(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
