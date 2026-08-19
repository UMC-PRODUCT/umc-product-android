package com.umc.presentation.community.component.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.component.component.UText
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey400
import com.umc.component.theme.grey500
import com.umc.component.R

/**
 * 커뮤니티 검색 결과가 없을 때 표시하는 Empty 화면
 */
@Composable
fun CommunitySearchEmptyContent(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // 검색 결과 없음 아이콘
        Icon(
            painter = painterResource(
                id = R.drawable.ic_search,
            ),
            contentDescription = null,
            tint = grey400(),
            modifier = Modifier.size(32.dp),
        )

        Spacer(modifier = Modifier.height(12.dp))

        UText(
            text = "검색 결과가 없습니다.",
            style = UmcTypographyTokens.HeadlineBold,
            color = grey500(),
        )
    }
}