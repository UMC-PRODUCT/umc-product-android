package com.umc.domain.model.enums

enum class WorkbookMissionType {
    LINK,
    FILE,
    TEXT,
    UNKNOWN;

    companion object {
        fun from(value: String?): WorkbookMissionType {
            if (value.isNullOrBlank()) return UNKNOWN
            return entries.firstOrNull { it.name == value } ?: UNKNOWN
        }
    }
}
