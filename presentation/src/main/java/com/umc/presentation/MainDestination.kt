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
    data class ScheduleEdit(val scheduleId: Long) : MainDestination

    //일정 상세
    @Serializable
    data class ScheduleDetail(val scheduleId: Long, val plusDay: Int) : MainDestination


    /**마이 페이지 섹션**/
    @Serializable
    data object Mypage : MainDestination

    //내 활동 (showType: "MYPOST", "MYCOMMENT", "MYSCRAP")
    @Serializable
    data class MyContent(val showType: String) : MainDestination

    //프로필 페이지
    @Serializable
    data object MyProfile : MainDestination



}
