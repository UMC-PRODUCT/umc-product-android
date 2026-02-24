package com.umc.domain.model.enums

enum class UserPart(val label: String) {
    PLAN("Plan"),
    DESIGN("Design"),
    WEB("Web"),
    IOS("IOS"),
    ANDROID("Android"),
    SPRINGBOOT("SpringBoot"),
    NODEJS("Node.js"),
    UNKNOWN("Unknown");


    companion object {
        fun from(value: String?): UserPart {
            if (value.isNullOrBlank()) return UNKNOWN

            return entries.firstOrNull {
                it.name.replace("_", "") == value.replace("_", "")
            } ?: UNKNOWN
        }

        fun getFilterLabels(): List<String> {
            return entries
                .filter { it != UNKNOWN }
                .map { it.label }
        }

    }

}