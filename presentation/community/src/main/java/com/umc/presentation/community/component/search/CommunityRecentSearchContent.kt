package com.umc.presentation.community.component.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey400
import com.umc.component.theme.grey600
import com.umc.component.theme.grey950

/**
 * 커뮤니티 검색 화면에서 최근 검색어 목록을 표시하는 영역
 *
 * 최근 검색어 선택, 개별 삭제, 전체 삭제 기능을 제공합니다.
 */
@Composable
fun CommunityRecentSearchContent(
    recentSearches: List<String>,
    onRecentSearchClick: (String) -> Unit,
    onDeleteRecentSearchClick: (String) -> Unit,
    onClearAllClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        // 최근 검색어 제목 및 전체 삭제 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            UText(
                text = "최근 검색어",
                style = UmcTypographyTokens.Title3Bold,
                color = grey950(),
            )

            UText(
                text = "전체 삭제",
                style = UmcTypographyTokens.Callout,
                color = grey600(),
                modifier = Modifier.clickable(
                    onClick = onClearAllClick,
                ),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 저장된 최근 검색어 목록
        recentSearches.forEach { recentSearch ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onRecentSearchClick(recentSearch)
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                UText(
                    text = recentSearch,
                    style = UmcTypographyTokens.Callout,
                    color = grey950(),
                    modifier = Modifier.weight(1f),
                )

                // 개별 최근 검색어 삭제
                IconButton(
                    onClick = {
                        onDeleteRecentSearchClick(recentSearch)
                    },
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete),
                        contentDescription = null,
                        tint = grey400(),
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}