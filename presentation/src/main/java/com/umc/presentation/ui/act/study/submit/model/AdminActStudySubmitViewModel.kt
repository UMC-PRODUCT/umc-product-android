package com.umc.presentation.ui.act.study.submit.model

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.curriculum.StudyGroup
import com.umc.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminActStudySubmitViewModel @Inject constructor() :
    BaseViewModel<AdminActStudySubmitState, AdminActStudySubmitEvent>(
        AdminActStudySubmitState()
    ) {

    private val allDummy: List<DummySubmission> = buildDummyAll()

    init {
        initFakeFilterData()
        loadWorkbookSubmissions()
    }



    fun onAction(action: AdminActStudySubmitAction) {
        when (action) {

            is AdminActStudySubmitAction.SelectWeek -> {
                val text = if (action.week == 0) "전체 주차" else "${action.week}주차"
                updateState { copy(selectedWeek = action.week, selectedWeekText = text) }
                loadWorkbookSubmissions()
            }


            is AdminActStudySubmitAction.SelectGroupName -> {
                updateState { copy(selectedGroupId = mapGroupNameToId(action.name)) }
                loadWorkbookSubmissions()
            }

            is AdminActStudySubmitAction.SelectGroup -> {
                updateState { copy(selectedGroupId = action.groupId) }
                loadWorkbookSubmissions()
            }

            is AdminActStudySubmitAction.ClickBest -> {
                updateState { copy(bestDialogTarget = action.item) }
                emitEvent(AdminActStudySubmitEvent.ShowBestDialog(action.item))
            }

            is AdminActStudySubmitAction.ClickReview -> {
                updateState { copy(reviewDialogTarget = action.item) }
                emitEvent(AdminActStudySubmitEvent.ShowReviewDialog(action.item))
            }

            AdminActStudySubmitAction.DismissBestDialog ->
                updateState { copy(bestDialogTarget = null) }

            AdminActStudySubmitAction.DismissReviewDialog ->
                updateState { copy(reviewDialogTarget = null) }

            is AdminActStudySubmitAction.ConfirmBest -> {
                val target = uiState.value.bestDialogTarget ?: return
                emitEvent(
                    AdminActStudySubmitEvent.ShowToast(
                        "${target.nickname} 베스트 설정(더미)"
                    )
                )
                updateState { copy(bestDialogTarget = null) }
            }

            is AdminActStudySubmitAction.SubmitReview -> {
                val target = uiState.value.reviewDialogTarget ?: return
                emitEvent(
                    AdminActStudySubmitEvent.ShowToast(
                        if (action.pass)
                            "${target.nickname} 통과 처리(더미)"
                        else
                            "${target.nickname} 반려 처리(더미)"
                    )
                )
                updateState { copy(reviewDialogTarget = null) }
            }
        }
    }


    private fun initFakeFilterData() {
        val weeks = listOf(0) + (1..10).toList()
        val groups = buildDummyGroups()

        updateState {
            copy(
                availableWeeks = weeks,
                selectedWeek = 0,
                selectedWeekText = "전체 주차",
                studyGroups = groups,
                selectedGroupId = null
            )
        }
    }



    fun loadWorkbookSubmissions() {
        viewModelScope.launch {

            val state = uiState.value
            val week = state.selectedWeek
            val gid = state.selectedGroupId

            val filtered = allDummy
                .asSequence()
                .filter { week == 0 || it.week == week }
                .filter { gid == null || it.groupId == gid }
                .toList()
                .map { it.toUiModel() }

            updateState {
                copy(
                    items = filtered,
                    nextCursor = null,
                    hasNext = false
                )
            }
        }
    }



    fun getGroupNames(state: AdminActStudySubmitState): List<String> {
        return listOf("전체 그룹") + state.studyGroups.map { it.name }
    }

    fun getSelectedGroupName(state: AdminActStudySubmitState?): String {
        val safe = state ?: return "전체 그룹"
        val id = safe.selectedGroupId ?: return "전체 그룹"
        return safe.studyGroups.firstOrNull { it.id == id }?.name ?: "전체 그룹"
    }

    private fun mapGroupNameToId(name: String): Long? {
        if (name == "전체 그룹") return null
        return uiState.value.studyGroups.firstOrNull { it.name == name }?.id
    }


    private data class DummySubmission(
        val challengerWorkbookId: Long,
        val userId: Long,
        val name: String,
        val nickname: String,
        val schoolName: String,
        val groupId: Long,
        val week: Int,
        val studyTitle: String,
        val submitUrl: String,
    ) {
        fun toUiModel(): AdminActStudySubmitItemUiModel {
            return AdminActStudySubmitItemUiModel(
                challengerWorkbookId = challengerWorkbookId,
                userId = userId,
                name = name,
                nickname = nickname,
                partLabel = "ANDROID",
                weekText = "${week}주차",
                studyTitle = studyTitle,
                submitUrl = submitUrl,
                schoolName = schoolName,
                profileImageUrl = null
            )
        }
    }


    private fun buildDummyGroups(): List<StudyGroup> {
        return listOf(
            StudyGroup(id = 1L, name = "서울여대"),
            StudyGroup(id = 2L, name = "명지대"),
            StudyGroup(id = 3L, name = "숭실대"),
            StudyGroup(id = 4L, name = "중앙대"),
            StudyGroup(id = 5L, name = "단국대"),
        )
    }



    private fun buildDummyAll(): List<DummySubmission> {

        fun title(week: Int) = when (week) {
            1 -> "Kotlin 기초"
            2 -> "Android 기본 구조"
            3 -> "UI 구성"
            4 -> "네트워크 통신"
            5 -> "MVVM 패턴"
            else -> "주차 미션"
        }

        return listOf(
            DummySubmission(1001, 501, "양지애", "나루", "서울여자대학교", 1L, 1, title(1), ""),
            DummySubmission(1002, 502, "김도연", "도리", "서울여자대학교", 1L, 1, title(1), ""),
            DummySubmission(1003, 503, "조경석", "조나단", "명지대학교", 2L, 1, title(1), ""),
            DummySubmission(1004, 504, "박유수", "어헛차", "숭실대학교", 3L, 1, title(1), ""),
            DummySubmission(1005, 505, "이준호", "준", "중앙대학교", 4L, 1, title(1), ""),
            DummySubmission(1006, 506, "최민서", "민", "단국대학교", 5L, 1, title(1), ""),

            DummySubmission(2001, 507, "윤서연", "서리", "서울여자대학교", 1L, 2, title(2), ""),
            DummySubmission(2002, 508, "김태훈", "태", "명지대학교", 2L, 2, title(2), ""),
            DummySubmission(2003, 509, "정하늘", "하늘", "중앙대학교", 4L, 2, title(2), ""),
            DummySubmission(2004, 510, "오지훈", "지훈", "숭실대학교", 3L, 2, title(2), ""),

            DummySubmission(3001, 511, "강수빈", "수빈", "단국대학교", 5L, 3, title(3), ""),
            DummySubmission(4001, 512, "한유진", "유진", "명지대학교", 2L, 4, title(4), ""),
            DummySubmission(5001, 513, "배지수", "지수", "중앙대학교", 4L, 5, title(5), ""),
        )
    }
}
