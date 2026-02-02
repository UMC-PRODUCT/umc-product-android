package com.umc.domain.model.enums

enum class VoteCondition {
    ANONYMITY, BLINDNESS, SINGLE_VOTE, MULTIPLE_VOTE;

    companion object {
        fun buildVoteConditionText(conditions: List<VoteCondition>): String {
            val identity = when {
                VoteCondition.ANONYMITY in conditions -> "익명 투표"
                else -> "실명 투표"
            }

            val selectType = when {
                VoteCondition.MULTIPLE_VOTE in conditions -> "복수 선택"
                else -> "단일 선택"
            }

            return (listOf(identity, selectType)).joinToString(" • ")
        }
    }

}