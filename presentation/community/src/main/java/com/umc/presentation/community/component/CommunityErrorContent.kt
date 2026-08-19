package com.umc.presentation.community.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.theme.UmcTheme
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey400
import com.umc.component.theme.grey500
import com.umc.component.theme.grey600
import com.umc.component.theme.indigo500

/**
 * 커뮤니티 스레드 목록 조회에 실패했을 때 표시하는 에러 화면
 *
 * 네트워크 오류 안내와 다시 시도하기 버튼을 제공합니다.
 */
@Composable
fun CommunityErrorContent(
    errorMessage: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {


    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // 스레드 조회 실패 아이콘
        Icon(
            painter = painterResource(
                id = R.drawable.ic_community_error,
            ),
            contentDescription = null,
            tint = grey400(),
            modifier = Modifier.size(32.dp),
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 에러 상태 제목
        UText(
            text = "스레드를 불러오지 못했어요.",
            style = UmcTypographyTokens.HeadlineBold,
            color = grey600(),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 네트워크 확인 안내 문구
        UText(
            text = "네트워크 연결을 확인하고 다시 시도해주세요.",
            style = UmcTypographyTokens.Subheadline,
            color = grey500(),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 스레드 목록 재조회 버튼
        UButton(
            text = "다시 시도하기",
            onClick = onRetryClick,
            modifier = Modifier
                .padding(top = 24.dp)
                .size(
                    width = 364.dp,
                    height = 52.dp,
                ),
            backgroundColor = indigo500(),
        )
    }
}

/**
 * 스레드 조회 에러 화면 Preview
 */
@Preview(showBackground = true)
@Composable
private fun CommunityErrorContentPreview() {
    UmcTheme {
        CommunityErrorContent(
            errorMessage = "",
            onRetryClick = {},
        )
    }
}