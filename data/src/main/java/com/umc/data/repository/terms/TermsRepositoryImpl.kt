package com.umc.data.repository.terms

import com.umc.data.dataSource.remote.terms.TermsRemoteDataSource
import com.umc.data.response.terms.TermsResponse.Companion.toDomain
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.base.map
import com.umc.domain.model.terms.TermsData
import com.umc.domain.repository.terms.TermsRepository
import javax.inject.Inject

class TermsRepositoryImpl @Inject constructor(
    private val termsRemoteDataSource: TermsRemoteDataSource
) : TermsRepository {

    override suspend fun getTermsByType(termsType: String): ApiState<TermsData> {
        return termsRemoteDataSource.getTermsByType(termsType).map { it.toDomain() }
    }

    override suspend fun getTermsById(termsId: Long): ApiState<TermsData> {
        return termsRemoteDataSource.getTermsById(termsId).map { it.toDomain() }
    }

}