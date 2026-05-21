package com.umc.presentation

import kotlinx.serialization.Serializable

sealed interface MainDestination {
    @Serializable
    data object Splash : MainDestination

    @Serializable
    data object Login : MainDestination

    @Serializable
    data class SignUp(val oAuthVerificationToken: String) : MainDestination

    /**홈 화면 섹션**/
    //홈 화면
    @Serializable
    data object Home : MainDestination

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
