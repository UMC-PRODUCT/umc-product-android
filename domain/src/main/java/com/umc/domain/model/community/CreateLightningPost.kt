package com.umc.domain.model.community

data class CreateLightningPost (
    val title: String,
    val content: String,
    val meetAt : String,
    val location : String,
    val maxParticipants : Int,
    val region: String = "",
    val anonymous: Boolean = false,
)