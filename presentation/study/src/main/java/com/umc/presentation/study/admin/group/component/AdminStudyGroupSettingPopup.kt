package com.umc.presentation.study.admin.group.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.*
import com.umc.component.theme.UmcTypographyTokens.Subheadline

/**
 * 스터디 그룹 설정 메뉴의 Popup 콘텐츠
 *
 * 그룹 정보 수정 및 그룹 삭제 기능을 제공합니다.
 */
@Composable
fun AdminStudyGroupSettingPopupContent(
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Column(
        modifier = Modifier.width(208.dp)
    ) {
        // 그룹 정보 수정
        SettingPopupItem(
            text = "정보 수정",
            iconRes = R.drawable.ic_study_edit,
            textColor = grey800(),
            iconTint = grey700(),
            onClick = onEditClick,
        )

        // 메뉴 구분선
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(grey100())
        )

        // 그룹 삭제
        SettingPopupItem(
            text = "그룹 삭제",
            iconRes = R.drawable.ic_study_delete,
            textColor = red500(),
            iconTint = red500(),
            onClick = onDeleteClick,
        )
    }
}

/**
 * 스터디 그룹 설정 Popup의 개별 메뉴 항목
 */
@Composable
private fun SettingPopupItem(
    text: String,
    iconRes: Int,
    textColor: Color,
    iconTint: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clickable {
                onClick()
            }
            .padding(
                horizontal = 14.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 메뉴 아이콘
        Icon(
            painter = painterResource(
                iconRes
            ),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(16.dp),
        )

        Spacer(
            modifier = Modifier.width(8.dp)
        )

        UText(
            text = text,
            style = Subheadline,
            color = textColor,
            modifier = Modifier.weight(1f),
        )

        // 상세 메뉴 이동 표시
        Icon(
            painter = painterResource(
                R.drawable.ic_study_arrow_right
            ),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(20.dp),
        )
    }
}