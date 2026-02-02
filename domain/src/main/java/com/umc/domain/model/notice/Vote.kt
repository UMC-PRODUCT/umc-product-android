package com.umc.domain.model.notice

import com.umc.domain.model.enums.VoteCondition
import com.umc.domain.model.enums.VoteState

data class Vote(
    val title: String = "",
    val state: VoteState = VoteState.PROGRESS,
    val condition: List<VoteCondition> = emptyList(),
    val conditionText: String = VoteCondition.buildVoteConditionText(condition),
    val item: List<VoteItem> = emptyList()
)

data class VoteItem(
    val isChecked: Boolean,
    val name: String
)
