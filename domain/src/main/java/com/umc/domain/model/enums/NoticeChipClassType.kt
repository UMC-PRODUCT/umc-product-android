package com.umc.domain.model.enums

enum class NoticeChipClassType(val label: String, val hasBottomSheet: Boolean = false) {
    ALL("전체"),
    OPERATOR("운영진공지"),
    PART("파트"),
    BRANCH("지부", true),
    SCHOOL("학교", true);

    companion object {
        fun fromLabel(label: String): NoticeChipClassType {
            return NoticeChipClassType.values().find { it.label == label } ?: ALL
        }
    }
}