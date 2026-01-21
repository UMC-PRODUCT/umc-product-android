package com.umc.domain.model.home

import java.util.UUID

data class ParticipantItem(
    val name : String = "",
    val id: String = UUID.randomUUID().toString()
)

