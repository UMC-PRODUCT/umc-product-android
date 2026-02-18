package com.umc.domain.usecase.notice

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.notice.NoticeSearch
import com.umc.domain.repository.NoticeRepository
import javax.inject.Inject

class SearchNoticeListUseCase @Inject constructor(
    private val noticeRepository: NoticeRepository
) {
    suspend operator fun invoke(
        keyword: String,
        gisuId: Long,
        chapterId: Long? = null,
        schoolId: Long? = null,
        part: String? = null,
        page: Int = 0,
        size: Int = 20
    ): ApiState<NoticeSearch> {
        return noticeRepository.searchNotices(
            keyword = keyword,
            gisuId = gisuId,
            chapterId = chapterId,
            schoolId = schoolId,
            part = part,
            page = page,
            size = size
        )
    }
}
