package com.umc.presentation.ui.act.study.group.model

import com.umc.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel


class AdminStudyGroupViewModel :
    BaseViewModel<AdminStudyGroupState, AdminStudyGroupEvent>(
        AdminStudyGroupState()
    ) {

    val dummyGroups = listOf(
        AdminStudyGroupItemUiModel(
            groupId = 1L,
            title = "React A팀",
            partLabel = "Web",
            leaderName = "홍길동",
            members = listOf("홍길동", "홍길순", "홍길자"),
            createdAtText = "2024.03.01",
            memberCount = 3,
            leaderUniv = "중앙대학교",
        ),
        AdminStudyGroupItemUiModel(
            groupId = 2L,
            title = "Android A팀",
            partLabel = "Android",
            leaderName = "박유수",
            members = listOf("김도연", "박유수", "조나단", "나루"),
            createdAtText = "2024.03.05",
            memberCount = 4,
            leaderUniv = "숭실대",
        )
    )
}
