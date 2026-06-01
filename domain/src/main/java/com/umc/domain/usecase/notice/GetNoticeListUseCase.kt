package com.umc.domain.usecase.notice

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.notice.NoticeSearch
import com.umc.domain.repository.NoticeRepository
import javax.inject.Inject

class GetNoticeListUseCase @Inject constructor(
    private val noticeRepository: NoticeRepository
) {
    suspend operator fun invoke(
        gisuId: Long,
        noticeTab: String = "CHALLENGER",
        chapterId: Long? = null,
        schoolId: Long? = null,
        part: String? = null,
        page: Int = 0,
        size: Int = 20
    ): ApiState<NoticeSearch> {
        return noticeRepository.getNotices(
            gisuId, noticeTab, chapterId, schoolId, part, page, size
        )
    }
}