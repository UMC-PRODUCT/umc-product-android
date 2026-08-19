package com.umc.presentation.community.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey100
import com.umc.component.theme.grey200
import com.umc.component.theme.grey800
import com.umc.component.theme.grey950
import com.umc.component.theme.indigo500
import com.umc.presentation.community.model.CommunityCategory

/**
 * 커뮤니티 스레드 목록의 카테고리 필터 메뉴
 *
 * 전체, 안읽음 및 각 스레드 카테고리를 선택할 수 있으며,
 * 현재 선택된 카테고리에는 체크 아이콘을 표시합니다.
 */
@Composable
fun CommunityFilterMenu(
    expanded: Boolean,
    selectedCategory: CommunityCategory,
    onDismissRequest: () -> Unit,
    onCategorySelected: (CommunityCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 메뉴가 닫힌 상태라면 Popup을 표시하지 않음
    if (!expanded) return

    // 필터 버튼 아래에 메뉴가 표시되도록 Y 위치 설정
    val menuYOffset = with(LocalDensity.current) {
        44.dp.roundToPx()
    }

    // 화면 바깥 클릭 또는 뒤로가기 시 닫히는 필터 Popup
    Popup(
        alignment = Alignment.TopEnd,
        offset = IntOffset(
            x = 0,
            y = menuYOffset,
        ),
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(
            focusable = true,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        Column(
            modifier = modifier
                .width(151.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(12.dp),
                    clip = false,
                )
                .background(
                    color = grey000(),
                    shape = RoundedCornerShape(12.dp),
                )
                .border(
                    width = 1.dp,
                    color = grey200(),
                    shape = RoundedCornerShape(12.dp),
                )
                .padding(
                    horizontal = 4.dp,
                    vertical = 6.dp,
                ),
        ) {
            // 전체 스레드 필터
            CommunityFilterMenuItem(
                text = CommunityCategory.ALL.label,
                isSelected = selectedCategory == CommunityCategory.ALL,
                onClick = {
                    onCategorySelected(CommunityCategory.ALL)
                    onDismissRequest()
                },
            )

            // 읽지 않은 스레드 필터
            CommunityFilterMenuItem(
                text = CommunityCategory.UNREAD.label,
                isSelected = selectedCategory == CommunityCategory.UNREAD,
                onClick = {
                    onCategorySelected(CommunityCategory.UNREAD)
                    onDismissRequest()
                },
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 기본 필터와 카테고리 필터를 구분하는 구분선
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .height(1.dp)
                    .background(grey100()),
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 파트공지 카테고리
            CommunityFilterMenuItem(
                text = CommunityCategory.PROJECT.label,
                isSelected = selectedCategory == CommunityCategory.PROJECT,
                onClick = {
                    onCategorySelected(CommunityCategory.PROJECT)
                    onDismissRequest()
                },
            )

            // 스터디 카테고리
            CommunityFilterMenuItem(
                text = CommunityCategory.STUDY.label,
                isSelected = selectedCategory == CommunityCategory.STUDY,
                onClick = {
                    onCategorySelected(CommunityCategory.STUDY)
                    onDismissRequest()
                },
            )

            // 질문 카테고리
            CommunityFilterMenuItem(
                text = CommunityCategory.QNA.label,
                isSelected = selectedCategory == CommunityCategory.QNA,
                onClick = {
                    onCategorySelected(CommunityCategory.QNA)
                    onDismissRequest()
                },
            )

            // 자유 카테고리
            CommunityFilterMenuItem(
                text = CommunityCategory.FREE.label,
                isSelected = selectedCategory == CommunityCategory.FREE,
                onClick = {
                    onCategorySelected(CommunityCategory.FREE)
                    onDismissRequest()
                },
            )
        }
    }
}

/**
 * 필터 메뉴에서 사용하는 개별 카테고리 항목
 *
 * 현재 선택된 카테고리에는 우측에 체크 아이콘을 표시합니다.
 */
@Composable
private fun CommunityFilterMenuItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        UText(
            text = text,
            style = UmcTypographyTokens.Subheadline,
            color = grey950(),
        )

        // 현재 선택된 필터 표시
        if (isSelected) {
            Icon(
                painter = painterResource(
                    id = R.drawable.ic_indigo_check,
                ),
                contentDescription = "선택됨",
                tint = indigo500(),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(18.dp),
            )
        }
    }
}