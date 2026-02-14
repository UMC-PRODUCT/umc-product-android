package com.umc.domain.usecase.challenger

import com.umc.domain.repository.ChallengerRepository
import javax.inject.Inject

//일정 생성용 유저 검색 usecase
//쿼리를 받아, 이름과 닉네임 동시에 검색을 진행한다.
class SearchChallengerScheduleUseCase @Inject constructor(
    private val repository: ChallengerRepository
){
    suspend operator fun invoke(cursor: Long?, size: Int, name: String?) =
        repository.searchChallengerSchedule(cursor, size, name, name)
}

