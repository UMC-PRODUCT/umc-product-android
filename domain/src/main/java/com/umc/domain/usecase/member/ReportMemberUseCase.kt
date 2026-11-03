package com.umc.domain.usecase.member

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.mypage.UserReportReason
import com.umc.domain.repository.member.MemberRepository
import javax.inject.Inject

class ReportMemberUseCase @Inject constructor(private val repository: MemberRepository) {
    suspend operator fun invoke(memberId: Long, reasons: Set<UserReportReason>, detail: String): ApiState<Unit> =
        repository.reportMember(memberId, reasons, detail)
}
