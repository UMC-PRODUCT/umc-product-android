package com.umc.presentation

import kotlinx.serialization.Serializable

sealed interface MainDestination {
    @Serializable
    data object Splash : MainDestination

    @Serializable
    data object Login : MainDestination

    @Serializable
    data class SignUp(val oAuthVerificationToken: String) : MainDestination
}
