package com.umc.domain.model.enums

enum class UserPart(val label: String) {
    PLAN("Plan"),
    DESIGN("Design"),
    WEB("Web"),
    IOS("IOS"),
    ANDROID("Android"),
    SPRINGBOOT("SpringBoot"),
    NODEJS("Node.js"),
    ADMIN("Admin"),
    UNKNOWN("Unknown");


    companion object {
        fun from(value: String?): UserPart {
            if (value.isNullOrBlank()) return UNKNOWN

            val v = value.trim()

            entries.firstOrNull {
                it.name.replace("_", "")
                    .equals(v.replace("_", ""), ignoreCase = true)
            }?.let { return it }

            entries.firstOrNull {
                it.label.equals(v, ignoreCase = true)
            }?.let { return it }

            return UNKNOWN
        }

        fun getFilterLabels(): List<String> {
            return entries
                .filter { it != UNKNOWN }
                .map { it.label }
        }
    }

}