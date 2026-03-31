package com.umc.presentation

import kotlinx.serialization.Serializable

sealed interface MainDestination {
    @Serializable
    data object Splash : MainDestination
}
