package com.umc.data.api

object Endpoints {

    object Auth {
        const val AUTH = "api/v1/auth"
        const val REISSUE = "$AUTH/auth/renew"
        const val LOGIN_KAKAO = "$AUTH/login/kakao"
        const val LOGIN_GOOGLE = "$AUTH/login/google"
        const val EMAIL_VERIFICATION = "$AUTH/email-verification"
        const val EMAIL_VERIFICATION_COMPLETE = "$EMAIL_VERIFICATION/complete"
    }

    object Challenger {
        const val CHALLENGER = "api/v1/challenger"
        const val DETAIL = "$CHALLENGER/{challengerId}"
    }
    // TODO 경로명 입력


    object Member{
        const val MEMBER = "api/v1/member"
        const val MYPROFILE = "$MEMBER/me"
        const val MEMBER_PROFILE = "$MEMBER/profile/{memberId}"
    }

    object Kakao{
        const val SEARCH_LOCATION = "v2/local/search/keyword.json"
    }
}
