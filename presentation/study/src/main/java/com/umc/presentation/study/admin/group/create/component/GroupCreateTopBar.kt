package com.umc.presentation.study.admin.group.create.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.*

/**
 * 스터디 그룹 생성 화면의 상단 TopBar
 *
 * 주요 기능
 * - 이전 화면으로 이동
 * - 화면 제목 표시
 * - 알림 버튼 표시
 * - 입력 조건 충족 여부에 따른 등록 버튼 활성화
 *
 * 등록 버튼은 스터디 그룹 생성에 필요한
 * 필수 입력값이 모두 선택된 경우에만 활성화됩니다.
 */
@Composable
fun GroupCreateTopBar(
    isRegisterEnabled: Boolean,
    onBackClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onNotificationClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 뒤로가기 버튼
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                painter = painterResource(
                    id = R.drawable.ic_back
                ),
                contentDescription = "뒤로가기",
                tint = grey800(),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(
            modifier = Modifier.width(6.dp)
        )

        // 화면 제목
        UText(
            text = "스터디 그룹 생성",
            style = UmcTypographyTokens.Title3Bold,
            color = grey800(),
            modifier = Modifier.weight(1f)
        )

        // 알림 버튼
        IconButton(
            onClick = onNotificationClick,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                painter = painterResource(
                    id = R.drawable.ic_alarm_filled
                ),
                contentDescription = "알림",
                tint = grey800(),
                modifier = Modifier.size(20.dp)
            )
        }

        // 스터디 그룹 등록 버튼
        UText(
            text = "등록",
            style = UmcTypographyTokens.SubheadlineBold,
            color = if (isRegisterEnabled) {
                indigo500()
            } else {
                grey400()
            },
            modifier = Modifier
                .padding(start = 12.dp)
                // 텍스트보다 넓은 터치 영역 확보
                .padding(8.dp)
                .clickable(
                    enabled = isRegisterEnabled,
                    onClick = onRegisterClick
                )
        )
    }
}