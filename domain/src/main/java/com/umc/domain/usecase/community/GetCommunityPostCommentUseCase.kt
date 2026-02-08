package com.umc.domain.usecase.community

import com.umc.domain.repository.community.CommunityRepository
import javax.inject.Inject

//게시글의 댓글 리스트를 불러오기
class GetCommunityPostCommentUseCase @Inject constructor(
    private val communityRepository: CommunityRepository
) {
    suspend operator fun invoke(postId: Long) = communityRepository.getComments(postId)
}