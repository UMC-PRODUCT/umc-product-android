package com.umc.domain.model.enums

enum class UserPart(val label: String) {
    PM("PM"),
    DESIGN("Design"),
    WEB("Web"),
    IOS("iOS"),
    ANDROID("Android"),
    SPRING_BOOT("SpringBoot"),
    NODE_JS("Node.js"),
    UNKNOWN("Unknown");



    companion object {
        fun from(value: String?): UserPart {
            if (value.isNullOrBlank()) return UNKNOWN

            return entries.firstOrNull {
                it.name.replace("_", "") == value.replace("_", "")
            } ?: UNKNOWN
        }
    }

}