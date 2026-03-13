package com.umc.domain.usecase.notice

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.notice.NoticeReadStatus
import com.umc.domain.repository.NoticeRepository
import javax.inject.Inject

class GetNoticeReadStatusUseCase @Inject constructor(
    private val noticeRepository: NoticeRepository
) {
    suspend operator fun invoke(
        noticeId: Long,
        cursorId: Long? = null,
        filterType: String = "ALL",
        organizationIds: List<Long>? = null,
        status: String = "READ"
    ): ApiState<NoticeReadStatus> {
        return noticeRepository.getNoticeReadStatus(
            noticeId = noticeId,
            cursorId = cursorId,
            filterType = filterType,
            organizationIds = organizationIds,
            status = status
        )
    }
}
