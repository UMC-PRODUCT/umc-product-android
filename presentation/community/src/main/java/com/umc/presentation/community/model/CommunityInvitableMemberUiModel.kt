package com.umc.presentation.community.model

data class CommunityInvitableMemberUiModel(
    val memberId: String,
    val challengerId: String,
    val name: String,
    val part: String,
    val generation: String,
) {
    val partLabel: String
        get() = when (part) {
            "PLAN" -> "Plan"
            "DESIGN" -> "Design"
            "WEB" -> "Web"
            "ANDROID" -> "Android"
            "IOS" -> "iOS"
            "NODEJS" -> "Node.js"
            "SPRINGBOOT" -> "Spring Boot"
            "ADMIN" -> "Admin"
            else -> part
        }
}