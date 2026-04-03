package com.umc.presentation

import kotlinx.serialization.Serializable

sealed interface MainDestination {
    @Serializable
    data object Splash : MainDestination

    //홈 화면
    @Serializable
    data object Home : MainDestination

}
