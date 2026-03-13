package com.umc.domain.repository.authentication

import com.umc.domain.model.base.ApiState
import com.umc.domain.model.mypage.UserOAuthType

interface AuthenticationRepository {

    //내 OAuth 가져오기
    suspend fun getMyOAuth(): ApiState<List<UserOAuthType>>
}