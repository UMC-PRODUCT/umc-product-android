package com.umc.domain.usecase.community

import com.umc.domain.repository.community.CommunityRepository
import javax.inject.Inject

class GetCommunityTrophyUseCase @Inject constructor(
    private val communityRepository: CommunityRepository
) {
    suspend operator fun invoke(week: Int?, school: String?, part: String?) =
        communityRepository.getTrophies(week, school, part)
}