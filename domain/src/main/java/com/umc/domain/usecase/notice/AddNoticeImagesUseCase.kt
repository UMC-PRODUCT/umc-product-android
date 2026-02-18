package com.umc.domain.usecase.notice

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.notice.NoticeImageRequest
import com.umc.domain.repository.NoticeRepository
import javax.inject.Inject

class AddNoticeImagesUseCase @Inject constructor(
    private val noticeRepository: NoticeRepository
) {
    suspend operator fun invoke(
        noticeId: Long,
        imageIds: List<String>
    ): ApiState<Unit> {
        return noticeRepository.addNoticeImages(
            noticeId,
            NoticeImageRequest(imageIds = imageIds)
        )
    }
}
