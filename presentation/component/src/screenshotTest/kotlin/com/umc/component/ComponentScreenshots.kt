package com.umc.component

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.umc.component.component.UButton
import com.umc.component.component.UChip
import com.umc.component.component.UInfoChip
import com.umc.component.component.USwitch
import com.umc.component.component.UText
import com.umc.component.component.UTextField
import com.umc.component.component.UToast
import com.umc.component.component.UToastState
import com.umc.component.theme.UmcTheme
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey50
import com.umc.domain.model.enums.UserPart

/**
 * 공용 컴포넌트를 라이트·다크 테마와 글자 크기 1.5배로 찍는다.
 * 색 함수가 isSystemInDarkTheme()를 직접 읽으므로 다크는 uiMode 로 나눈다. UText 는 fontScale 을 1로 고정하므로
 * 1.5배에서 크기가 바뀌는 곳은 UText 를 거치지 않는 텍스트다.
 */
@Preview(name = "라이트", showBackground = true)
@Preview(name = "다크", showBackground = true, backgroundColor = 0xFF121212, uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Preview(name = "큰글자", showBackground = true, fontScale = 1.5f)
annotation class ComponentPreviews

@Composable
private fun Showcase(content: @Composable () -> Unit) {
    UmcTheme {
        Column(
            modifier = Modifier.background(grey50()).padding(16.dp).width(360.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) { content() }
    }
}

@PreviewTest
@ComponentPreviews
@Composable
fun UButtonScreenshot() = Showcase {
    UButton(text = "다시 시도하기", modifier = Modifier.width(328.dp).padding(vertical = 0.dp))
    UButton(text = "비활성 버튼", enabled = false, modifier = Modifier.width(328.dp))
}

@PreviewTest
@ComponentPreviews
@Composable
fun UChipScreenshot() = Showcase {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        UChip(text = "전체")
        UChip(text = "선택한 필터", showCloseIcon = true, onCloseClick = {})
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        UInfoChip(part = UserPart.ANDROID)
        UInfoChip(part = UserPart.MOBILE_PRODUCT_ENGINEER)
        UInfoChip(part = UserPart.UNKNOWN)
    }
}

@PreviewTest
@ComponentPreviews
@Composable
fun USwitchScreenshot() = Showcase {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        USwitch(checked = true, onCheckedChange = {})
        USwitch(checked = false, onCheckedChange = {})
    }
}

@PreviewTest
@ComponentPreviews
@Composable
fun UTextFieldScreenshot() = Showcase {
    UTextField(value = "", onValueChange = {}, placeholder = "이메일을 입력해주세요")
    UTextField(value = "challenger@umc.com", onValueChange = {})
}

@PreviewTest
@ComponentPreviews
@Composable
fun UTextScreenshot() = Showcase {
    UText(text = "스레드를 불러오지 못했어요.", style = UmcTypographyTokens.HeadlineBold)
    UText(text = "네트워크 연결을 확인하고 다시 시도해주세요.", style = UmcTypographyTokens.Subheadline)
}

@PreviewTest
@ComponentPreviews
@Composable
fun UToastScreenshot() = Showcase {
    UToast(message = "저장했어요", state = UToastState.CHECK)
    UToast(message = "저장하지 못했어요", state = UToastState.ERROR)
}
