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
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey50
import com.umc.component.theme.grey800
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
 * @param backgroundColor 토스트 배경색. 기본값 grey800
 * @param textColor 메시지 글자색. 기본값 grey50
 */
@Composable
fun UToast(
    message: String,
    state: UToastState,
    modifier: Modifier = Modifier,
    backgroundColor: Color = grey800(),
    textColor: Color = grey50(),
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .background(backgroundColor, RoundedCornerShape(8.dp))
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
                color = textColor,
                modifier = Modifier.padding(start = 16.dp),
            )
        } else {
            UText(
                text = message,
                style = UmcTypographyTokens.Callout,
                color = textColor,
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
 * @param backgroundColor 토스트 배경색. 기본값 grey800
 * @param textColor 메시지 글자색. 기본값 grey50
 */
@Composable
fun UToastHost(
    data: UToastData?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    durationMillis: Long = 2000L,
    backgroundColor: Color = grey800(),
    textColor: Color = grey50(),
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
            UToast(
                message = it.message,
                state = it.state,
                backgroundColor = backgroundColor,
                textColor = textColor,
            )
        }
    }
}

/**
 * 앱 전체에서 하나만 두는 토스트 상태. MainActivity 의 UmcApp 이 [LocalUToast] 로 내려주고 [UToastOverlay] 로 그린다.
 *
 * 화면마다 호스트를 두는 대신 이것을 쓰면, 저장 직후 뒤로 가기처럼 토스트를 띄우자마자 화면을 떠나도 토스트가 남는다.
 * 바텀시트·다이얼로그는 별도 창이라 이 토스트가 그 아래에 가려진다. 시트·다이얼로그가 열린 채로 띄울 때는
 * 그 안에 [UToastHost] 를 따로 둔다.
 */
@Stable
class UToastController {
    var current: UToastData? by mutableStateOf(null)
        private set

    /** 같은 문구를 연달아 띄워도 다시 나타나도록 띄울 때마다 바뀐다. */
    var generation: Int by mutableIntStateOf(0)
        private set

    fun show(message: String, state: UToastState = UToastState.NONE) {
        if (message.isBlank()) return
        current = UToastData(message, state)
        generation++
    }

    fun dismiss() {
        current = null
    }

    /** [generation] 번째로 띄운 토스트가 아직 떠 있을 때만 닫는다. 앞 토스트의 타이머가 새 토스트를 지우지 않게 한다. */
    fun dismiss(generation: Int) {
        if (generation == this.generation) current = null
    }
}

/** UmcApp 밖(미리보기·스크린샷 테스트)에서는 화면에 그리지 않는 컨트롤러가 쓰인다. */
val LocalUToast = staticCompositionLocalOf { UToastController() }

/**
 * [controller] 의 토스트를 그린다. 위치는 호출하는 쪽이 [modifier] 로 정한다.
 * 바텀시트·다이얼로그 안에도 두면, 그것이 열린 동안 띄운 앱 토스트가 시트 위에 보인다(앱 화면 쪽 토스트는 시트에 가려진다).
 */
@Composable
fun UToastOverlay(controller: UToastController, modifier: Modifier = Modifier, durationMillis: Long = 2000L) {
    val generation = controller.generation
    val data = controller.current
    LaunchedEffect(generation, data) {
        if (data != null) {
            delay(durationMillis)
            controller.dismiss(generation)
        }
    }
    // 사라지는 애니메이션 동안에도 내용이 보이도록 마지막 토스트를 들고 있는다.
    var shown by remember { mutableStateOf(data) }
    if (data != null) shown = data
    AnimatedVisibility(
        visible = data != null,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it },
        modifier = modifier,
    ) {
        shown?.let { UToast(message = it.message, state = it.state) }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF888888)
@Composable
internal fun UToastCheckPreview() {
    UToast(message = "인증 코드가 발송되었습니다.", state = UToastState.CHECK)
}

@Preview(showBackground = true, backgroundColor = 0xFF888888)
@Composable
internal fun UToastErrorPreview() {
    UToast(message = "이메일 형식이 올바르지 않습니다.", state = UToastState.ERROR)
}

@Preview(showBackground = true, backgroundColor = 0xFF888888)
@Composable
internal fun UToastNonePreview() {
    UToast(message = "알림 메세지입니다.", state = UToastState.NONE)
}
