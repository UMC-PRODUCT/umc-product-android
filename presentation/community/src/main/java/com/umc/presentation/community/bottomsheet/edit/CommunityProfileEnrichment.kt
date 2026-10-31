package com.umc.presentation.community.bottomsheet.edit

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withTimeoutOrNull

/** 부가 정보 조회가 느려도 목록은 제한 시간 안에 기본 정보로 표시한다. */
internal suspend fun <T> enrichCommunityProfilesWithinBudget(
    members: List<T>,
    timeoutMillis: Long = 1_500,
    enrich: suspend (T) -> T,
): List<T> = withTimeoutOrNull(timeoutMillis) {
    val requests = Semaphore(4)
    coroutineScope {
        members.map { member -> async { requests.withPermit { enrich(member) } } }.awaitAll()
    }
} ?: members
