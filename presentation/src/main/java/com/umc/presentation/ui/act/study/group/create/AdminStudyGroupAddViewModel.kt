package com.umc.presentation.ui.act.study.group.create

import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.ui.act.study.common.model.MemberUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AdminStudyGroupAddViewModel @Inject constructor() :
    BaseViewModel<AdminStudyGroupAddState, AdminStudyGroupAddEvent>(
        initialPageState = AdminStudyGroupAddState()
    ) {

    fun onGroupNameChanged(name: String) {
        updateState { copy(groupName = name) }
    }

    fun onPartChanged(part: String) {
        updateState { copy(part = part) }
    }

    fun setLeader(leader: MemberUiModel) {
        updateState { copy(leader = leader) }
    }

    fun setMembers(list: List<MemberUiModel>) {
        val distinct = list.distinctBy { it.id }
        updateState { copy(selectedMembers = distinct) }
    }

    fun deleteMember(member: MemberUiModel) {
        updateState {
            copy(
                selectedMembers = selectedMembers.filterNot { it.id == member.id }
            )
        }
    }

    fun onClickPickLeader() = emitEvent(AdminStudyGroupAddEvent.ClickPickLeader)
    fun onClickPickMembers() = emitEvent(AdminStudyGroupAddEvent.ClickPickMembers)
    fun onClickBack() = emitEvent(AdminStudyGroupAddEvent.ClickBack)
    fun onClickRegister() = emitEvent(AdminStudyGroupAddEvent.ClickRegister)
}
