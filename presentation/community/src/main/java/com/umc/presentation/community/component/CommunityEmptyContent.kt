package com.umc.presentation.community.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.UmcTheme
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey400
import com.umc.component.theme.grey500
import com.umc.component.theme.grey600
import com.umc.component.theme.grey700
import com.umc.component.theme.grey800

@Composable
fun CommunityEmptyContent(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {

        Icon(
            painter = painterResource(
                id = R.drawable.ic_community_empty,
            ),
            contentDescription = null,
            tint = grey400(),
            modifier = Modifier.size(28.dp),
        )

        Spacer(modifier = Modifier.height(8.dp))

        UText(
            text = "아직 스레드가 없어요",
            style = UmcTypographyTokens.HeadlineBold,
            color = grey600(),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp),
        )

        UText(
            text = "첫 쓰레드를 만들어 우리 파트의 대화를 시작해보세요.",
            style = UmcTypographyTokens.Subheadline,
            color = grey500(),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CommunityEmptyContentPreview() {
    UmcTheme {
        CommunityEmptyContent()
    }
}