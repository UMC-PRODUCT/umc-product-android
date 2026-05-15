package com.umc.presentation

import kotlinx.serialization.Serializable

sealed interface MainDestination {
    @Serializable
    data object Splash : MainDestination

    @Serializable
    data object Login : MainDestination

    @Serializable
    data class SignUp(val oAuthVerificationToken: String) : MainDestination

    @Serializable
    data object Permission : MainDestination

    @Serializable
    data object SignUpFail : MainDestination

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


}
