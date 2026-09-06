package com.umc.component.base

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * 세션 만료(재로그인 필요) 전역 신호.
 *
 * 만료 판정은 어느 화면에서든 발생할 수 있지만 대응(스플래시로 보내 재로그인)은 앱에 한 곳만
 * 있으면 된다. ViewModel별 이벤트로 두면 화면마다 구독을 심어야 하고, 실제로 그 구독이
 * 한 곳도 없어 만료 복구 경로가 통째로 죽어 있었다. 그래서 전역 신호로 올리고 NavHost
 * 최상단에서 한 번만 관찰한다.
 *
 * SharedFlow(replay=0)로 두면 안 된다 — extraBufferCapacity는 '느린 구독자'를 위한 버퍼일 뿐
 * 구독 전에 발행된 값을 새 구독자에게 전달하지 않는다. 만료는 NavHost가 재구성 중인 순간에도
 * 발생할 수 있으므로 CONFLATED Channel로 마지막 신호를 보관했다가 구독이 붙는 즉시 넘긴다.
 * (만료 신호는 여러 번 와도 결과가 같으므로 최신 1건만 유지하면 충분하다)
 */
object SessionExpiryBus {
    private val _expired = Channel<Unit>(Channel.CONFLATED)
    val expired: Flow<Unit> = _expired.receiveAsFlow()

    fun notifyExpired() {
        _expired.trySend(Unit)
    }
}
