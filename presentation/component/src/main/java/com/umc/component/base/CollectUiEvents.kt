package com.umc.component.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow

/**
 * 단발성 이벤트를 **라이프사이클 인지** 방식으로 수집한다.
 *
 * LaunchedEffect 안에서 그냥 collect하면 구독이 컴포지션 수명만 따르기 때문에, 앱이
 * 백그라운드로 내려간 뒤 도착한 이벤트도 그대로 처리된다. 그 결과 다른 앱을 쓰는 중에
 * 토스트가 뜨거나, 사용자가 누른 적 없는 화면으로 이동해 있는 일이 생긴다.
 *
 * STARTED 아래에서는 수집을 멈추고, 다시 보이는 시점에 재개한다. 이벤트 소스가
 * Channel(BUFFERED) 기반이라 멈춘 구간의 이벤트는 버퍼에 남았다가 재개 시 전달된다
 * — SharedFlow였다면 이 방식이 오히려 유실을 키웠을 것이다.
 */
@Composable
fun <T> CollectUiEvents(events: Flow<T>, onEvent: suspend (T) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val handler by rememberUpdatedState(onEvent)
    LaunchedEffect(events, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            events.collect { handler(it) }
        }
    }
}
