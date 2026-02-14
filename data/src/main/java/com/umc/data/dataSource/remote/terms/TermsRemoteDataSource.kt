package com.umc.data.dataSource.remote.terms

import com.umc.data.response.terms.TermsResponse
import com.umc.domain.model.base.ApiState

interface TermsRemoteDataSource {

    //약관 타입으로 가져오기
    suspend fun getTermsByType(termsType: String): ApiState<TermsResponse>

    suspend fun getTermsById(termsId: Long): ApiState<TermsResponse>


}