package com.umc.domain.usecase.community

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.home.ParticipantSearchPage
import com.umc.domain.repository.ChallengerRepository
import javax.inject.Inject

class SearchCommunityCreateMembersUseCase @Inject constructor(
    private val challengerRepository: ChallengerRepository,
) {

    suspend operator fun invoke(
        cursor: Long? = null,
        size: Int = 20,
        keyword: String? = null,
    ): ApiState<ParticipantSearchPage> {
        return challengerRepository.searchChallengerSchedule(
            cursor = cursor,
            size = size,
            keyword = keyword,
        )
    }
}