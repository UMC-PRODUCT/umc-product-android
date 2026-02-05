package com.umc.data.repository

import com.umc.data.dataSource.ChallengerRemoteDataSource
import com.umc.data.response.ChallengerResponse.Companion.toManageModel
import com.umc.data.response.ChallengerResponse.Companion.toModel
import com.umc.domain.model.act.challenger.ChallengerInfoDialogModel
import com.umc.domain.model.act.challenger.ChallengerManageDialogModel
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.map
import com.umc.domain.repository.ChallengerRepository
import javax.inject.Inject

class ChallengerRepositoryImpl @Inject constructor(
    private val challengerRemoteDataSource: ChallengerRemoteDataSource
) : ChallengerRepository {
    override suspend fun getChallengerInfo(id: Long): ApiState<ChallengerInfoDialogModel> {
        return challengerRemoteDataSource.getChallengerDetail(id).map {
            it.toModel()
        }
    }

    override suspend fun getAdminChallengerInfo(id: Long): ApiState<ChallengerManageDialogModel> {
        return challengerRemoteDataSource.getChallengerDetail(id).map {
            it.toManageModel()
        }
    }
}