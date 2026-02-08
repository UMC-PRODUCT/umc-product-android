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

    object Attendance {
        const val ATTENDANCE = "api/v1/attendances"
        const val AVAILABLE = "$ATTENDANCE/available"
    }

    object Challenger {
        const val CHALLENGER = "api/v1/challenger"
        const val DETAIL = "$CHALLENGER/{challengerId}"
        const val POINT = "$CHALLENGER/{challengerId}/points"
    }

    object Member{
        const val MEMBER = "api/v1/member"
        const val MYPROFILE = "$MEMBER/me"
        const val MEMBER_PROFILE = "$MEMBER/profile/{memberId}"
    }

    object Kakao{
        const val SEARCH_LOCATION = "v2/local/search/keyword.json"
    }

    object Schedule{
        const val SCHEDULE = "api/v1/schedules"
        const val DETAIL = "$SCHEDULE/{scheduleId}"
        const val MONTH = "$SCHEDULE/my-list"
    }

    object Community{
        const val COMMUNITY = "api/v1/posts"
        const val POST_SEARCH = "$COMMUNITY/search"
        const val POST_DETAIL = "$COMMUNITY/{postId}"
        const val POST_COMMENT = "$COMMUNITY/{postId}/comments"

        const val LIGHTNING = "$COMMUNITY/lightning"


    }
}
