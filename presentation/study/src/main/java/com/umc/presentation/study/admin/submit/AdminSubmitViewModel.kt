package com.umc.presentation.study.admin.submit

import com.umc.component.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AdminSubmitViewModel @Inject constructor() :
    BaseViewModel<AdminSubmitState, AdminSubmitEvent>(AdminSubmitState()) {

    init {
        loadDummy()
    }

    private fun loadDummy() {
        updateState {
            copy(
                items = listOf(
                    AdminSubmitItemUiModel(
                        id = 1L,
                        name = "홍길동",
                        nickname = "닉네임",
                        partLabel = "iOS",
                        weekText = "1주차",
                        studyTitle = "SwiftUI 클론 코딩",
                        schoolName = "중앙대",
                        status = "BEST",
                    ),
                    AdminSubmitItemUiModel(
                        id = 2L,
                        name = "홍길동",
                        nickname = "닉네임",
                        partLabel = "iOS",
                        weekText = "1주차",
                        studyTitle = "SwiftUI 클론 코딩",
                        schoolName = "중앙대",
                        status = "FAIL",
                    ),
                    AdminSubmitItemUiModel(
                        id = 3L,
                        name = "홍길동",
                        nickname = "닉네임",
                        partLabel = "iOS",
                        weekText = "1주차",
                        studyTitle = "SwiftUI 클론 코딩",
                        schoolName = "중앙대",
                        status = "PASS",
                    ),
                    AdminSubmitItemUiModel(
                        id = 4L,
                        name = "홍길동",
                        nickname = "닉네임",
                        partLabel = "iOS",
                        weekText = "1주차",
                        studyTitle = "SwiftUI 클론 코딩",
                        schoolName = "중앙대",
                        status = "SUBMITTED",
                    ),
                    AdminSubmitItemUiModel(
                        id = 5L,
                        name = "홍길동",
                        nickname = "닉네임",
                        partLabel = "iOS",
                        weekText = "1주차",
                        studyTitle = "SwiftUI 클론 코딩",
                        schoolName = "중앙대",
                        status = "SUBMITTED",
                    ),
                )
            )
        }
    }

    fun onAction(action: AdminSubmitAction) {
        when (action) {
            is AdminSubmitAction.SelectWeek ->
                updateState { copy(selectedWeek = action.week) }
            is AdminSubmitAction.SelectGroup ->
                updateState { copy(selectedGroupName = action.name) }
        }
    }
}