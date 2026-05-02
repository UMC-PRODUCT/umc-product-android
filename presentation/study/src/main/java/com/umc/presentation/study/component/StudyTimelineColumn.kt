package com.umc.presentation.study.component


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
import com.umc.domain.model.enums.StudyStatus

/**
 * 각 스터디 아이템 왼쪽에 표시되는 타임라인 컬럼
 *
 * 상태별 표시:
 * - 잠금: 자물쇠 아이콘 (흐리게)
 * - PASS: 체크 아이콘 (primary 색상)
 * - FAIL: X 아이콘 (danger 색상)
 * - IN_PROGRESS: 주차 번호 원형 뱃지 (primary 색상)
 */
@Composable
fun StudyTimelineColumn(
    week: Int,
    status: StudyStatus,
    isLocked: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.width(32.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        when {
            isLocked -> {
                Icon(
                    painter = painterResource(R.drawable.ic_locked),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = neutral300()
                )
            }
            status == StudyStatus.PASS -> {
                Icon(
                    painter = painterResource(R.drawable.ic_check_success),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = success500()
                )
            }
            status == StudyStatus.FAIL -> {
                Icon(
                    painter = painterResource(R.drawable.ic_check_failed),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = danger500()
                )
            }
            else -> {
                // IN_PROGRESS: 주차 번호 원형
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(primary500()),
                    contentAlignment = Alignment.Center
                ) {
                    UText(
                        text = week.toString(),
                        style = UmcTypographyTokens.Caption1Bold,
                        color = neutral000(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}