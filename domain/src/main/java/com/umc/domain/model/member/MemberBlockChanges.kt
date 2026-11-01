package com.umc.domain.model.member

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** 열린 화면에만 전달한다. 계정별 차단 변경을 다음 세션에 재생하지 않는다. */
data class MemberBlockChange(val ownerId: Long, val memberId: Long, val blocked: Boolean)

object MemberBlockChanges {
    private val changes = MutableSharedFlow<MemberBlockChange>()
    val events = changes.asSharedFlow()
    suspend fun publish(change: MemberBlockChange) = changes.emit(change)
}
