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

    object Member {
        const val MEMBER = "api/v1/member"
        const val MYPROFILE = "$MEMBER/me"
        const val MEMBER_PROFILE = "$MEMBER/profile/{memberId}"
    }

    object Kakao {
        const val SEARCH_LOCATION = "v2/local/search/keyword.json"
    }

    object Schedule {
        const val SCHEDULE = "api/v1/schedules"
        const val DETAIL = "$SCHEDULE/{scheduleId}"
        const val MONTH = "$SCHEDULE/my-list"
    }

    object Organization {
        const val SCHOOL = "api/v1/schools"
        const val SCHOOL_ID = "$SCHOOL/{schoolId}"
        const val SCHOOL_UNASSIGNED = "$SCHOOL/unassigned"
        const val SCHOOL_UNASSIGN = "$SCHOOL_ID/unassign"
        const val SCHOOL_ASSIGN = "$SCHOOL_ID/assign"
        const val SCHOOL_LINK = "$SCHOOL/link/{schoolId}"
        const val SCHOOL_ALL = "$SCHOOL/all"

        const val STUDY_GROUP = "api/v1/study-groups"
        const val STUDY_GROUD_ID = "$STUDY_GROUP/{groupId}"
        const val STUDY_GROUD_NAME = "$STUDY_GROUP/names"
        const val STUDY_MEMVER = "$STUDY_GROUD_ID/members"
        const val GISU = "api/v1/gisu"
        const val GISU_ID = "$GISU/{gisuId}"
        const val GISU_ALL = "$GISU/all"
        const val GISU_ACTIVE = "$GISU/active"
        const val GISU_ACTIVE_ID = "$GISU_ID/active"

        const val CHAPTER = "api/v1/chapters"
        const val CHAPTER_WITH_SCHOOL = "$CHAPTER/with-schools"

    }
}
