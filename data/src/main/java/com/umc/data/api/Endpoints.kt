package com.umc.data.api

object Endpoints {

    object Auth {
        //Authentication
        const val AUTH = "api/v1/auth"
        const val REISSUE = "$AUTH/token/renew"
        const val LOGIN_KAKAO = "$AUTH/login/kakao"
        const val LOGIN_GOOGLE = "$AUTH/login/google"
        const val LOGIN_EMAIL = "$AUTH/login/email"
        const val EMAIL_VERIFICATION = "$AUTH/email-verification"
        const val EMAIL_VERIFICATION_COMPLETE = "$EMAIL_VERIFICATION/code"
        const val PASSWORD_RESET = "$AUTH/password/reset"

        //Authorization
        const val AUTHORIZATION = "api/v1/authorization"
        const val AUTHORIZATION_CHECK = "$AUTHORIZATION/resource-permission"

    }

    //위의 Auth는 NormalRetrofit이고, 아래 Authentication은 AuthRetrofit이다.
    object Authentication{
        const val MEMBER_AUTH = "api/v1/member-oauth"
        const val MEMBER_AUTH_SHOW = "$MEMBER_AUTH/me"

    }

    object Attendance {
        const val SCHEDULES = "api/v2/schedules"
        const val AVAILABLE = "$SCHEDULES/me"
        const val CHECK = "$SCHEDULES/{scheduleId}/attendances/request"
        const val REASON = "$SCHEDULES/{scheduleId}/attendances/excuse"
        const val PENDING = "$SCHEDULES/{scheduleId}/attendance"
        const val DECIDE = "$SCHEDULES/{scheduleId}/attendances/decide"
        const val HISTORY = "$SCHEDULES/me"
    }

    object Challenger {
        const val CHALLENGER = "api/v1/challenger"
        const val DETAIL = "$CHALLENGER/{challengerId}"
        const val POINT = "$CHALLENGER/{challengerId}/points"
        const val DELETE_POINT = "$CHALLENGER/points/{challengerPointId}"

        const val SEARCH = "api/v2/challenger/search"

        const val CHALLENGER_RECORD = "api/v1/challenger-record"
        const val CHALLENGER_RECORD_MEMBER = "api/v1/challenger-record/member"

        const val CHALLENGER_POINT = "api/v1/challenger/{challengerId}/points"

    }

    object Member {
        const val MEMBER = "api/v1/member"
        const val MEMBER_V2 = "api/v2/member"
        const val MYPROFILE = "$MEMBER/me"

        const val MYPROFILE_V2 = "$MEMBER_V2/me"
        const val MEMBER_PROFILE = "$MEMBER/profile/{memberId}"
        const val MEMBER_REGISTER_OAUTH = "$MEMBER/register/oauth"
        const val MEMBER_REGISTER_EMAIL = "$MEMBER/register/email"
        const val MEMBER_PROFILE_LINK = "$MEMBER/profile/links"
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



        const val SCHEDULES_ME = "api/v2/schedules/me"
        const val DETAIL_V2 = "api/v2/schedules/{scheduleId}"
        const val FORCE_DELETE = "$DETAIL_V2/force"
        const val CAPABILITIES = "api/v2/schedules/capabilities"
        const val CREATE_V2 = "api/v2/schedules"

        const val ATTENDANCE_REQUEST = "api/v2/schedules/{scheduleId}/attendances/request"
        const val ATTENDANCE_DECIDE = "api/v2/schedules/{scheduleId}/attendances/decide"
        const val ATTENDANCE_EXCUSE = "api/v2/schedules/{scheduleId}/attendances/excuse"
        const val ATTENDANCE_HISTORY = "api/v2/schedules/attendance"
        const val ATTENDANCE_DETAIL = "api/v2/schedules/{scheduleId}/attendance"

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

        const val MY_POST = "$COMMUNITY/my"
        const val MY_COMMENT = "$COMMUNITY/commented"
        const val MY_SCRAP = "$COMMUNITY/scrapped"

        const val MODIFY_LIGHTNING = "$COMMUNITY/{postId}/lightning"
        const val REPORT_POST = "$COMMUNITY/{postId}/reports"
        const val REPORT_COMMENT = "api/v1/comments/{commentId}/reports"

        const val TROPHY = "api/v1/trophies"
        const val THREADS = "api/v1/community/threads"
        const val THREAD_DETAIL = "$THREADS/{threadId}"

        // 초대 가능한 회원 조회
        const val THREAD_INVITABLE =
            "$THREAD_DETAIL/invitable"

        // 스레드 회원 초대
        const val THREAD_INVITE =
            "$THREAD_DETAIL/invite"

        const val THREAD_LEAVE = "$THREAD_DETAIL/leave"
        const val THREAD_MUTE = "$THREAD_DETAIL/mute"
        const val THREAD_PIN = "$THREAD_DETAIL/pin"

        const val THREAD_MEMBER =
            "$THREAD_DETAIL/members/{memberId}"

        const val THREAD_MEMBERS =
            "$THREAD_DETAIL/members"

        const val THREAD_MESSAGES = "$THREAD_DETAIL/messages"
        const val THREAD_MEMBER_ROLE = "$THREAD_MEMBER/role"
        const val MESSAGE_REPORT =
            "api/v1/community/messages/{messageId}/report"

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

        const val MANAGED_STUDY_GROUPS = "$STUDY_GROUP/managed"


        const val CREATE_STUDY_GROUP_SCHEDULE =
            "api/v1/study-groups/schedules"


        const val STUDY_GROUP_DETAIL =
            "$STUDY_GROUP/{studyGroupId}"

        const val STUDY_GROUP_MENTOR =
            "$STUDY_GROUP/{studyGroupId}/mentors/{mentorId}"

        const val STUDY_GROUP_MEMBER =
            "$STUDY_GROUP/{studyGroupId}/members/{memberId}"

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

        const val CHAPTER_ID = "$CHAPTER/{chapterId}"

    }

    object Terms{
        const val TERMS = "api/v1/terms"
        const val TERMS_TYPE = "$TERMS/type/{termsType}"
        const val TERMS_ID = "$TERMS/{termsId}"
    }

    object Curriculum {

        const val CURRICULUM_OVERVIEW =
            "api/v2/curriculums/overview"

        const val CHALLENGER_ME_PROGRESS =
            "api/v2/curriculums/progress/me"

        const val SUBMIT =
            "api/v2/curriculums/challenger-workbooks/submissions"

        const val WORKBOOK_SUBMISSIONS =
            "api/v1/curriculums/workbook-submissions"

        const val STUDY_GROUPS =
            "api/v1/curriculums/study-groups"

        const val AVAILABLE_WEEKS =
            "api/v1/curriculums/available-weeks"

        // 운영진 - 스터디원 제출 현황 조회
        const val WORKBOOK_SUBMISSIONS_V2 =
            "api/v2/curriculums/workbook-submissions"

        // 운영진 - 제출 현황 조회 가능 주차 목록
        const val WORKBOOK_SUBMISSION_WEEKS =
            "api/v2/curriculums/workbook-submissions/weeks"


        // 챌린저 워크북 상세 조회
        const val CHALLENGER_WORKBOOK_DETAIL =
            "api/v2/curriculums/challenger-workbooks/{challengerWorkbookId}"


        // 베스트 워크북 조회
        const val WEEKLY_BEST_WORKBOOKS =
            "api/v2/curriculums/weekly-best-workbooks"

        // 베스트 워크북 선정
        const val CREATE_WEEKLY_BEST_WORKBOOK =
            "api/v2/curriculums/challenger-workbooks/weekly-best"

        // 베스트 워크북 선정 사유 수정
        const val UPDATE_WEEKLY_BEST_WORKBOOK =
            "api/v2/curriculums/challenger-workbooks/weekly-best/{weeklyBestWorkbookId}"

        // 베스트 워크북 선정 철회
        const val DELETE_WEEKLY_BEST_WORKBOOK =
            "api/v2/curriculums/challenger-workbooks/weekly-best/{weeklyBestWorkbookId}"

        // 미션 피드백 작성
        const val CREATE_MISSION_FEEDBACK =
            "api/v2/curriculums/challenger-workbooks/missions/feedback"

        // 미션 피드백 수정
        const val UPDATE_MISSION_FEEDBACK =
            "api/v2/curriculums/challenger-workbooks/missions/feedback/{missionFeedbackId}"
    }

    object Workbook {
        const val BEST = "api/v1/workbooks/challenger/{challengerWorkbookId}/best"
        const val REVIEW = "api/v1/workbooks/challenger/{challengerWorkbookId}/review"

        const val CHALLENGER_SUBMISSION = "api/v1/workbooks/challenger/{challengerWorkbookId}/submissions"
    }


    object Notice {
        const val NOTICE = "api/v1/notices"
        const val NOTICE_SEARCH = "$NOTICE/search"
        const val NOTICE_DETAIL = "$NOTICE/{noticeId}"

        // 세부 리소스 경로
        const val NOTICE_VOTES = "$NOTICE_DETAIL/votes"
        const val NOTICE_VOTE = "$NOTICE_DETAIL/vote"
        const val NOTICE_REMINDERS = "$NOTICE_DETAIL/reminders"
        const val NOTICE_READ = "$NOTICE_DETAIL/read"
        const val NOTICE_READ_STATUS = "$NOTICE_DETAIL/read-status"
        const val NOTICE_READ_STATICS = "$NOTICE_DETAIL/read-statics"
        const val NOTICE_LINKS = "$NOTICE_DETAIL/links"
        const val NOTICE_IMAGES = "$NOTICE_DETAIL/images"
        const val VOTE_RESPONSES = "$NOTICE_DETAIL/votes/responses"
    }

    object Survey {
        const val SURVEY = "api/v1/surveys"
        const val VOTE_RESPONSES = "$SURVEY/votes/{voteId}/responses"
    }

    object Notification {
        const val NOTIFICATION = "api/v1/notification"
        const val FCM_TOKEN = "$NOTIFICATION/fcm/token"
    }
}
