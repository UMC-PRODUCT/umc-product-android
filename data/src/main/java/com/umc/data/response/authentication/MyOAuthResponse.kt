package com.umc.data.response.authentication

import com.google.gson.annotations.SerializedName
import com.umc.domain.model.mypage.UserOAuthType

data class MyOAuthResponse(
    @SerializedName("memberOAuthId")
    val memberOAuthId: Long,
    @SerializedName("memberId")
    val memberId: Long,
    @SerializedName("provider")
    val provider: String
) {
    companion object {
        // 단일 객체를 도메인으로 변환
        fun MyOAuthResponse.toDomain(): UserOAuthType {
            return UserOAuthType(
                memberOAuthId = memberOAuthId,
                memberId = memberId,
                provider = provider
            )
        }

    }
}

