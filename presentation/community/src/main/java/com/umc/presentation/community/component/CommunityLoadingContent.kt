package com.umc.presentation.community.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/**
 * 커뮤니티 스레드 목록을 불러오는 동안 표시하는 로딩 화면
 *
 * 실제 스레드 대신 Skeleton Item을 여러 개 표시합니다.
 */
@Composable
fun CommunityLoadingContent() {

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(
            horizontal = 16.dp,
            vertical = 16.dp,
        ),
    ) {

        // 로딩 중인 스레드 카드 형태의 Skeleton UI 표시
        items(5) {
            CommunitySkeletonItem()
        }
    }
}