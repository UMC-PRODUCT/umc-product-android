package com.umc.presentation.community.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.umc.component.component.UText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTheme
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey400
import com.umc.component.theme.grey500
import com.umc.component.theme.grey600

/**
 * 커뮤니티에 생성된 스레드가 없을 때 표시하는 Empty 화면
 *
 * 스레드가 존재하지 않는다는 안내와
 * 새로운 스레드 생성을 유도하는 문구·버튼을 표시합니다. (Figma `Error / 스레드 없음`)
 *
 * @param onCreateThreadClick "스레드 만들기" 버튼 클릭 시 호출
 */
@Composable
fun CommunityEmptyContent(
    onCreateThreadClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // 스레드가 없는 상태를 나타내는 아이콘 (48dp 영역 가운데에 원본 크기로 배치)
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(
                    id = R.drawable.ic_community_empty,
                ),
                contentDescription = null,
                tint = grey400(),
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Empty 상태 제목
        UText(
            text = AppStrings.COMMUNITY_EMPTY_TITLE,
            style = UmcTypographyTokens.HeadlineBold,
            color = grey600(),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(4.dp))

        // 새로운 스레드 생성을 안내하는 설명
        UText(
            text = AppStrings.COMMUNITY_EMPTY_DESCRIPTION,
            style = UmcTypographyTokens.Subheadline,
            color = grey500(),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(40.dp))

        // 스레드 생성 화면으로 이동
        CommunityActionButton(
            text = AppStrings.COMMUNITY_CREATE_TITLE,
            onClick = onCreateThreadClick,
            textStyle = UmcTypographyTokens.HeadlineBold,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .height(52.dp),
        )
    }
}

/**
 * 스레드 Empty 화면 Preview
 */
@Preview(showBackground = true)
@Composable
private fun CommunityEmptyContentPreview() {
    UmcTheme {
        CommunityEmptyContent(
            onCreateThreadClick = {},
        )
    }
}
