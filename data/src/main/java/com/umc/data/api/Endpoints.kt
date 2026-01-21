package com.umc.data.api

object Endpoints {

    object Auth {
        const val AUTH = "auth"
        const val REISSUE = "$AUTH/auth/renew"
        const val LOGIN_KAKAO = "$AUTH/login/kakao"
        const val LOGIN_GOOGLE = "$AUTH/login/google"
        const val EMAIL_VERIFICATION = "$AUTH/email-verification"
        const val EMAIL_VERIFICATION_COMPLETE = "$EMAIL_VERIFICATION/complete"
    }
    // TODO 경로명 입력
}
