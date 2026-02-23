package com.umc.domain.usecase.community

import com.umc.domain.model.community.TrophyWrite
import com.umc.domain.repository.community.CommunityRepository
import javax.inject.Inject

class CreateCommunityTrophyUseCase @Inject constructor(
    private val communityRepository: CommunityRepository
) {
    suspend operator fun invoke(request: TrophyWrite) =
        communityRepository.createTrophy(request)
}