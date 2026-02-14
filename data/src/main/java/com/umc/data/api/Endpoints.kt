package com.umc.data.api

object Endpoints {

    object Auth {
        const val AUTH = "api/v1/auth"
        const val REISSUE = "$AUTH/auth/renew"
        const val LOGIN_KAKAO = "$AUTH/login/kakao"
        const val LOGIN_GOOGLE = "$AUTH/login/google"
        const val EMAIL_VERIFICATION = "$AUTH/email-verification"
        const val EMAIL_VERIFICATION_COMPLETE = "$EMAIL_VERIFICATION/code"
    }

    object Attendance {
        const val ATTENDANCE = "api/v1/attendances"
        const val AVAILABLE = "$ATTENDANCE/available"
        const val CHECK = "$ATTENDANCE/check"
        const val REASON = "$ATTENDANCE/reason"
        const val PENDING = "$ATTENDANCE/pending/{scheduleId}"
        const val APPROVE = "$ATTENDANCE/{recordId}/approve"
        const val REJECT = "$ATTENDANCE/{recordId}/reject"
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
        const val MEMBER_REGISTER = "$MEMBER/register"
    }

    object Kakao {
        const val SEARCH_LOCATION = "v2/local/search/keyword.json"
    }

    object Schedule {
        const val SCHEDULE = "api/v1/schedules"
        const val DETAIL = "$SCHEDULE/{scheduleId}"
        const val MONTH = "$SCHEDULE/my-list"

        const val SCHEDULE_WITH_ATTENDANCE = "$SCHEDULE/with-attendance"

        const val DELETE = "$SCHEDULE/{scheduleId}/with-attendance"

        const val LOCATION = "$SCHEDULE/{scheduleId}/location"

    }

    object Community{
        const val COMMUNITY = "api/v1/posts"
        const val POST_SEARCH = "$COMMUNITY/search"
        const val POST_DETAIL = "$COMMUNITY/{postId}"
        const val POST_COMMENT = "$COMMUNITY/{postId}/comments"

        const val POST_LIKE = "$COMMUNITY/{postId}/like"

        const val POST_SCRAP = "$COMMUNITY/{postId}/scrap"

        const val LIGHTNING = "$COMMUNITY/lightning"

        const val POST_COMMENT_DETAIL = "$POST_COMMENT/{commentId}"

    }

    object Storage{
        const val STORAGE = "api/v1/storage"

        const val PRE_UPLOAD = "$STORAGE/prepare-upload"

        const val CONFIRM_UPLOAD = "$STORAGE/{fileId}/confirm"

        const val FILE_DELETE = "$STORAGE/{fileId}"

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
        const val STUDY_MEMBER = "$STUDY_GROUD_ID/members"
        const val GISU = "api/v1/gisu"
        const val GISU_ID = "$GISU/{gisuId}"
        const val GISU_ALL = "$GISU/all"
        const val GISU_ACTIVE = "$GISU/active"
        const val GISU_ACTIVE_ID = "$GISU_ID/active"

        const val CHAPTER = "api/v1/chapters"
        const val CHAPTER_WITH_SCHOOL = "$CHAPTER/with-schools"

    }
}
