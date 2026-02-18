package com.umc.domain.usecase.notice

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.notice.NoticeLinkRequest
import com.umc.domain.repository.NoticeRepository
import javax.inject.Inject

class AddNoticeLinksUseCase @Inject constructor(
    private val noticeRepository: NoticeRepository
) {
    suspend operator fun invoke(
        noticeId: Long,
        links: List<String>
    ): ApiState<Unit> {
        return noticeRepository.editNoticeLinks(
            noticeId,
            NoticeLinkRequest(links = links)
        )
    }
}
