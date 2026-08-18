package com.umc.presentation.study.admin.group.create.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.*

/**
 * 스터디 그룹 생성 화면에서
 * 선택형 입력 항목을 표시하는 공통 컴포넌트
 *
 * 파트, 담당 파트장, 스터디원 등
 * 직접 값을 입력하는 것이 아니라
 * 바텀시트를 열어 값을 선택하는 항목에 사용합니다.
 *
 * 주요 기능
 * - 항목 제목 표시
 * - 현재 선택된 값 표시
 * - 선택된 값이 없을 경우 placeholder 표시
 * - 클릭 시 선택 바텀시트 호출
 */
@Composable
fun GroupCreateSelectRow(
    title: String,
    value: String,
    placeholder: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 선택 항목 제목
        UText(
            text = title,
            style = UmcTypographyTokens.SubheadlineBold,
            color = grey800()
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        // 선택 영역
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clickable {
                    onClick()
                },
            shape = RoundedCornerShape(8.dp),
            color = grey000(),
            border = BorderStroke(
                width = 1.dp,
                color = grey200()
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 선택된 값 또는 placeholder
                UText(
                    text = value.ifBlank {
                        placeholder
                    },
                    style = UmcTypographyTokens.Subheadline,
                    color = if (value.isBlank()) {
                        grey400()
                    } else {
                        grey800()
                    },
                    modifier = Modifier.weight(1f)
                )

                // 선택 화면 이동 아이콘
                Icon(
                    painter = painterResource(
                        id = R.drawable.ic_arrow_next
                    ),
                    contentDescription = null,
                    tint = grey500(),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}