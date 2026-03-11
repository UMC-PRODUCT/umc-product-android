package com.umc.presentation.ui.act.challenge

import com.umc.domain.model.act.challenger.AdminChallenger
import com.umc.domain.model.act.challenger.UserPartCount
import com.umc.domain.model.enums.UserPart

data class AdminChallengerGroupUIModel(
    val part: UserPart,
    val items: List<AdminChallenger>,
    val partCount: UserPartCount
)