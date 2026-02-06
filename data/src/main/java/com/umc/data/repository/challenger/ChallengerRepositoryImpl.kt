package com.umc.data.repository.challenger

import com.umc.data.dataSource.remote.challenger.ChallengerRemoteDataSource
import com.umc.data.response.challenger.ChallengerResponse.Companion.toManageModel
import com.umc.data.response.challenger.ChallengerResponse.Companion.toModel
import com.umc.domain.model.act.challenger.ChallengerInfoDialogModel
import com.umc.domain.model.act.challenger.ChallengerManageDialogModel
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.map
import com.umc.domain.model.request.challenger.ChallengerPointRequest
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

    override suspend fun grantChallengerPoint(
        id: Long,
        request: ChallengerPointRequest
    ): ApiState<ChallengerManageDialogModel> {
        return challengerRemoteDataSource.grantChallengerPoint(id, request).map {
            it.toManageModel()
        }
    }
}