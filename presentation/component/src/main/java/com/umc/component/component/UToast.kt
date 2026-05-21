package com.umc.component.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.neutral000
import com.umc.component.theme.neutral800
import kotlinx.coroutines.delay

/** 토스트 상태. CHECK=성공 아이콘, ERROR=에러 아이콘, NONE=아이콘 없음 */
enum class UToastState { CHECK, ERROR, NONE }

/** [UToastHost]에 전달할 토스트 데이터 */
data class UToastData(
    val message: String,
    val state: UToastState,
)

/**
 * UMC 공용 토스트 컴포넌트. UToast(XML)의 Compose 마이그레이션 버전.
 *
 * 화면 하단에 [UToastHost]로 렌더링하면 자동 소멸 처리가 함께 동작함.
 * 직접 사용 시 [UToast]만 단독 배치 가능.
 *
 * @param message 표시할 텍스트
 * @param state 아이콘 종류. CHECK=체크, ERROR=에러, NONE=아이콘 없음
 * @param modifier 외부 레이아웃 지정
 */
@Composable
fun UToast(
    message: String,
    state: UToastState,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .background(neutral800(), RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (state != UToastState.NONE) {
            Icon(
                painter = painterResource(
                    id = if (state == UToastState.CHECK) R.drawable.ic_toast_check
                    else R.drawable.ic_toast_error
                ),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(24.dp)
                    .padding(end = 0.dp),
            )
            UText(
                text = message,
                style = UmcTypographyTokens.Callout,
                color = neutral000(),
                modifier = Modifier.padding(start = 16.dp),
            )
        } else {
            UText(
                text = message,
                style = UmcTypographyTokens.Callout,
                color = neutral000(),
            )
        }
    }
}

/**
 * [UToastData]를 받아 [UToast]를 슬라이드 애니메이션으로 표시하고 자동 소멸시키는 호스트.
 *
 * 사용 예: 화면 루트 Box 안에서 Alignment.BottomCenter + padding(bottom=56.dp) 로 배치.
 *
 * @param data 표시할 토스트 데이터. null이면 숨김
 * @param onDismiss 자동 소멸 또는 외부에서 닫을 때 호출
 * @param modifier 위치/여백 지정
 * @param durationMillis 토스트 표시 유지 시간 (밀리초). 기본값 2000ms
 */
@Composable
fun UToastHost(
    data: UToastData?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    durationMillis: Long = 2000L,
) {
    LaunchedEffect(data) {
        if (data != null) {
            delay(durationMillis)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = data != null,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it },
        modifier = modifier,
    ) {
        data?.let {
            UToast(message = it.message, state = it.state)
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF888888)
@Composable
private fun UToastCheckPreview() {
    UToast(message = "인증 코드가 발송되었습니다.", state = UToastState.CHECK)
}

@Preview(showBackground = true, backgroundColor = 0xFF888888)
@Composable
private fun UToastErrorPreview() {
    UToast(message = "이메일 형식이 올바르지 않습니다.", state = UToastState.ERROR)
}

@Preview(showBackground = true, backgroundColor = 0xFF888888)
@Composable
private fun UToastNonePreview() {
    UToast(message = "알림 메세지입니다.", state = UToastState.NONE)
}
