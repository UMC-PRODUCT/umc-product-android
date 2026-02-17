package com.umc.domain.usecase

import com.umc.domain.model.home.getGisuSummaryList
import com.umc.domain.repository.AppDataStoreRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

//유저의 가장 최신 challengerId를 가져오는 usecase
class GetChallengerIdUseCase @Inject constructor(
    private val repository: AppDataStoreRepository
){
    suspend operator fun invoke(): Long {
        val userInfo = repository.getUserInfo().first()

        val gisuSummaryList = userInfo.getGisuSummaryList()

        //최신 기수 가져오기
        val latestGisu = gisuSummaryList.maxByOrNull { it.gisu }

        val challengerId = latestGisu?.let { summary ->
            summary.fromRoles.firstOrNull()?.challengerId
                ?: summary.fromRecords.firstOrNull()?.challengerId
        }

        return challengerId ?: -1L
    }
}