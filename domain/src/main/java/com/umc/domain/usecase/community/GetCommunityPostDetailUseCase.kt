package com.umc.domain.usecase.community

import com.umc.domain.repository.community.CommunityRepository
import javax.inject.Inject

//게시글의 상세 정보를 가져오는 USECASE
class GetCommunityPostDetailUseCase @Inject constructor(
    private val communityRepository: CommunityRepository
) {
    suspend operator fun invoke(postId: Long) = communityRepository.getPostDetail(postId)
}