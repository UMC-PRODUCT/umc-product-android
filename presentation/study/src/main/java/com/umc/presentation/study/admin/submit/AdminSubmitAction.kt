package com.umc.presentation.study.admin.submit

sealed interface AdminSubmitAction {
    data class SelectWeek(val week: Int) : AdminSubmitAction
    data class SelectGroup(val name: String) : AdminSubmitAction
}