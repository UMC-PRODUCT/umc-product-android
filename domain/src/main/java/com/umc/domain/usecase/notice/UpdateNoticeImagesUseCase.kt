package com.umc.domain.usecase.notice

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.notice.NoticeImageRequest
import com.umc.domain.repository.NoticeRepository
import javax.inject.Inject

/**
 * 공지 이미지를 전달한 목록으로 전부 교체한다.
 * 추가(POST)와 달리 수정 화면에서 쓰면 기존 이미지가 중복되지 않고,
 * 빈 목록을 보내면 전부 삭제된다 (서버 스펙)
 */
class UpdateNoticeImagesUseCase @Inject constructor(
    private val noticeRepository: NoticeRepository
) {
    suspend operator fun invoke(
        noticeId: Long,
        imageIds: List<String>
    ): ApiState<Unit> {
        return noticeRepository.updateNoticeImages(
            noticeId,
            NoticeImageRequest(imageIds = imageIds)
        )
    }
}
