package com.umc.domain.repository.authentication

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.mypage.UserOAuthType

interface AuthenticationRepository {

    //내 OAuth 가져오기
    suspend fun getMyOAuth(): ApiState<List<UserOAuthType>>

    //소셜 계정 연동 추가(OAuth 검증 토큰 사용)
    suspend fun addMyOAuth(oAuthVerificationToken: String): ApiState<List<UserOAuthType>>
}