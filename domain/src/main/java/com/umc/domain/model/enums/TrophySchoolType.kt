package com.umc.domain.model.enums

enum class TrophySchoolType(val korName: String) {
    CAU("중앙대학교"),
    SWU("서울여자대학교"),
    ALL("전체");


    companion object {

        fun getSchoolLabels(): List<String> {
            return entries
                .filter { it != ALL }
                .map { it.korName }
                .sorted()
        }
    }

}