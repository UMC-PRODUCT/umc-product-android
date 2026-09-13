package com.umc.presentation.remotenotice

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTheme
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey500
import com.umc.component.theme.grey800
import com.umc.component.theme.grey900

/**
 * 앱 이용을 막는 전용 화면
 *
 * 원격 설정의 BLOCKING 안내(점검 등)에 쓴다. 아래 화면을 조작할 수 없도록 모든 터치를 이 화면이 받아 삼키고
 * 뒤로 가기도 막는다. 사용자가 할 수 있는 건 앱 종료뿐이고, 운영진이 설정을 끄면 앱을 다시 열 때 사라진다.
 */
@Composable
fun RemoteBlockingScreen(
    title: String,
    body: String,
    onExitApp: () -> Unit,
) {
    // 뒤로 가기로 아래 화면에 돌아가지 못하게 한다
    BackHandler(enabled = true) {}

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(grey000())
            // 아래 화면으로 터치가 새지 않도록 모든 입력을 소비한다
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent().changes.forEach { it.consume() }
                    }
                }
            }
            .safeDrawingPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Image(
            painter = painterResource(id = R.drawable.ic_logo),
            contentDescription = null,
        )

        Spacer(modifier = Modifier.height(24.dp))

        UText(
            text = title,
            style = UmcTypographyTokens.HeadlineBold,
            color = grey900(),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        UText(
            text = body,
            style = UmcTypographyTokens.Callout,
            color = grey500(),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.weight(1f))

        UButton(
            text = AppStrings.REMOTE_NOTICE_EXIT_APP,
            onClick = onExitApp,
            backgroundColor = grey800(),
            textColor = grey000(),
            textStyle = UmcTypographyTokens.HeadlineBold,
            contentPadding = PaddingValues(vertical = 16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RemoteBlockingScreenPreview() {
    UmcTheme {
        RemoteBlockingScreen(
            title = "서비스 점검 중이에요",
            body = "더 나은 서비스를 위해 점검하고 있어요. 잠시 후 다시 이용해주세요.",
            onExitApp = {},
        )
    }
}
