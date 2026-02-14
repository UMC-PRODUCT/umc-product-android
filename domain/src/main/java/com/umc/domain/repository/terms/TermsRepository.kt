package com.umc.domain.repository.terms

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.terms.TermsData

interface TermsRepository {

    //타입으로 약관 가져오기
    suspend fun getTermsByType(termsType: String): ApiState<TermsData>

    //id로 약관 가져오기
    suspend fun getTermsById(termsId: Long): ApiState<TermsData>


}