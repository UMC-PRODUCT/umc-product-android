package com.umc.presentation.community.chatting

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.FailState
import com.umc.domain.model.community.thread.CommunityMessageReportReason

internal data class CommunityMessageReportResults(
    val accepted: Set<CommunityMessageReportReason>,
    val failures: Map<CommunityMessageReportReason, FailState>,
)

/** 기존 단일 사유 API를 선택한 사유마다 한 번씩 순차 호출한다. */
internal suspend fun submitCommunityMessageReports(
    reasons: Set<CommunityMessageReportReason>,
    submit: suspend (CommunityMessageReportReason) -> ApiState<*>,
): CommunityMessageReportResults {
    val accepted = linkedSetOf<CommunityMessageReportReason>()
    val failures = linkedMapOf<CommunityMessageReportReason, FailState>()
    reasons.sortedBy { it.ordinal }.forEach { reason ->
        when (val response = submit(reason)) {
            is ApiState.Success -> accepted += reason
            is ApiState.Fail -> failures[reason] = response.failState
        }
    }
    return CommunityMessageReportResults(accepted, failures)
}
