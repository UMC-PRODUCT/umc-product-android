package com.umc.presentation.community.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey950
import com.umc.presentation.community.model.CommunityCategory

/**
 * 커뮤니티 메인 화면에서 사용하는 상단바
 *
 * 화면 제목과 카테고리 필터 버튼,
 * 스레드 검색 화면 이동 버튼을 제공합니다.
 */
@Composable
fun CommunityTopBar(
    isFilterMenuExpanded: Boolean,
    selectedCategory: CommunityCategory,
    onFilterClick: () -> Unit,
    onFilterDismissRequest: () -> Unit,
    onCategorySelected: (CommunityCategory) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 화면 제목
        UText(
            text = AppStrings.COMMUNITY_TITLE,
            style = UmcTypographyTokens.Title2Bold.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.35.sp,
            ),
            color = grey950(),
        )

        Spacer(modifier = Modifier.weight(1f))

        // 카테고리 필터 버튼 및 필터 메뉴
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable(
                        interactionSource = remember {
                            MutableInteractionSource()
                        },
                        indication = null,
                        onClick = onFilterClick,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(
                        id = R.drawable.ic_community_filter,
                    ),
                    contentDescription = "필터",
                    tint = grey950(),
                    modifier = Modifier.size(24.dp),
                )
            }

            // 선택한 카테고리를 변경하는 Popup 메뉴
            CommunityFilterMenu(
                expanded = isFilterMenuExpanded,
                selectedCategory = selectedCategory,
                onDismissRequest = onFilterDismissRequest,
                onCategorySelected = onCategorySelected,
            )
        }

        // 커뮤니티 검색 화면 이동 버튼
        IconButton(
            onClick = onSearchClick,
            modifier = Modifier.size(48.dp),
        ) {
            Icon(
                painter = painterResource(
                    id = R.drawable.ic_search,
                ),
                contentDescription = "검색",
                tint = grey950(),
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
