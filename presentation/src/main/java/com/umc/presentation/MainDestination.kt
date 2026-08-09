package com.umc.presentation

import kotlinx.serialization.Serializable

sealed interface MainDestination {
    @Serializable
    data object Splash : MainDestination

    @Serializable
    data object Login : MainDestination

    @Serializable
    data object EmailLogin : MainDestination

    // 비밀번호 찾기 (이메일 인증 후 새 비밀번호 설정)
    @Serializable
    data object FindPassword : MainDestination

    // 개인정보 입력 단계. signUpType(SOCIAL/EMAIL)에 따라 회원가입 API가 분기됨
    @Serializable
    data class SignUp(
        val signUpType: String,
        val oAuthVerificationToken: String = "",
        val emailVerificationToken: String = "",
        val rawPassword: String = "",
    ) : MainDestination

    @Serializable
    data class SocialSignUp(val oAuthVerificationToken: String) : MainDestination

    @Serializable
    data object EmailSignUp : MainDestination

    @Serializable
    data object Permission : MainDestination

    @Serializable
    data object SignUpFail : MainDestination

    @Serializable
    data object SignUpFailCode : MainDestination

    /**공지 섹션**/
    //공지 목록
    @Serializable
    data object Notice : MainDestination

    //공지 검색
    @Serializable
    data class NoticeSearch(val gisuId: Long) : MainDestination

    //운영진 공지
    @Serializable
    data class AdminNotice(val gisuId: Long) : MainDestination

    //공지 작성. noticeId가 있으면 수정 모드
    @Serializable
    data class NoticeWrite(val noticeId: Long = 0L) : MainDestination

    //공지 상세
    @Serializable
    data class NoticeDetail(val noticeId: Long) : MainDestination

    /**홈 화면 섹션**/
    //홈 화면
    @Serializable
    data object Home : MainDestination

    @Serializable
    data object Act : MainDestination

    @Serializable
    data class AdminChallengerDetail(val challengerId: Long) : MainDestination

    //공지 화면
    @Serializable
    data object Notification : MainDestination

    //일정 생성
    @Serializable
    data object ScheduleAdd : MainDestination

    //일정 수정
    @Serializable
    data class ScheduleEdit(val scheduleId: Long = -1L) : MainDestination

    //일정 상세
    @Serializable
    data class ScheduleDetail(val scheduleId: Long = -1L, val plusDay: Int) : MainDestination


    /**마이 페이지 섹션**/

    //신 마이페이지
    @Serializable
    data class Mycard(
        val memberId: String? = null
    ) : MainDestination

    //(구 마이페이지) -> (신 설정)
    @Serializable
    data object Mypage : MainDestination

    //내 활동 (showType: "MYPOST", "MYCOMMENT", "MYSCRAP")
    @Serializable
    data class MyContent(val showType: String) : MainDestination

    //프로필 페이지
    @Serializable
    data object MyProfile : MainDestination

    /**내 qr코드 페이지**/
    @Serializable
    data object Qrcode : MainDestination

    //받은 명함 페이지
    @Serializable
    data object ReceivedCard : MainDestination



    /** 커뮤니티 섹션 **/
    @Serializable
    data object Community : MainDestination

    @Serializable
    data object CommunitySearch : MainDestination

    @Serializable
    data object CommunityCreate : MainDestination

    @Serializable
    data class CommunityEdit(
        val threadId: String,
    ) : MainDestination

}
