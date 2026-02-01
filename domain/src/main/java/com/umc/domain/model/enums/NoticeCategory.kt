package com.umc.domain.model.enums

enum class NoticeCategory(val label: String) {
    CENTRAL_OFFICE("중앙"), BRANCH("지부"), SCHOOL("학교"), PART("파트");

    companion object {
        fun find(value: String): NoticeCategory {
            return NoticeCategory.values().find {
                it.label == value
            } ?: NoticeCategory.CENTRAL_OFFICE
        }
    }
}