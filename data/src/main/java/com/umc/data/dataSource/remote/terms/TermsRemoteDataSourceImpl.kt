package com.umc.data.dataSource.remote.terms

import com.umc.data.api.TermsApi
import com.umc.data.dataSource.base.apiCall
import com.umc.data.response.terms.TermsResponse
import com.umc.domain.model.base.ApiState
import javax.inject.Inject

class TermsRemoteDataSourceImpl @Inject constructor(
    private val termsApi: TermsApi
) : TermsRemoteDataSource {
    override suspend fun getTermsByType(termsType: String): ApiState<TermsResponse> {
        return apiCall { termsApi.getTermsByType(termsType) }

    }

    override suspend fun getTermsById(termsId: Long): ApiState<TermsResponse> {
        return apiCall { termsApi.getTermsById(termsId) }

    }
}