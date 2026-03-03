package com.umc.presentation.ui.act.study.group.create.model

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.organization.CreateStudyGroupRequest
import com.umc.domain.repository.OrganizationRepository
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.ui.act.study.common.model.MemberUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.umc.domain.model.enums.UserPart

@HiltViewModel
class AdminStudyGroupAddViewModel @Inject constructor(
    private val organizationRepository: OrganizationRepository,
) : BaseViewModel<AdminStudyGroupAddState, AdminStudyGroupAddEvent>(
    initialPageState = AdminStudyGroupAddState()
) {


    fun onClickBack() = emitEvent(AdminStudyGroupAddEvent.ClickBack)
    fun onClickPickLeader() = emitEvent(AdminStudyGroupAddEvent.ClickPickLeader)
    fun onClickPickMembers() = emitEvent(AdminStudyGroupAddEvent.ClickPickMembers)


    fun onGroupNameChanged(name: String) {
        updateState { copy(groupName = name, errorMessage = null) }
    }

    fun onPartChanged(partLabel: String) {
        updateState { copy(partLabel = partLabel, errorMessage = null) }
    }

    fun setLeader(leader: MemberUiModel) {
        updateState { copy(leader = leader, errorMessage = null) }
    }

    fun setMembers(list: List<MemberUiModel>) {
        val distinct = list.distinctBy { it.challengerId }
        updateState { copy(selectedMembers = distinct, errorMessage = null) }
    }

    fun deleteMember(member: MemberUiModel) {
        updateState {
            copy(selectedMembers = selectedMembers.filterNot { it.challengerId == member.challengerId })
        }
    }

    fun submitCreateStudyGroup() {
        val state = uiState.value

        if (!state.canRegister) return

        val leaderChallengerId = state.leader!!.challengerId

        val partEnum = UserPart.from(state.partLabel)

        if (partEnum == UserPart.UNKNOWN) {
            emitEvent(AdminStudyGroupAddEvent.ShowToast("파트를 선택해 주세요."))
            return
        }

        val request = CreateStudyGroupRequest(
            name = state.groupName.trim(),
            part = partEnum.name,
            leaderId = leaderChallengerId,
            memberIds = state.selectedMembers.map { it.challengerId }.distinct()
        )

        updateState { copy(isSubmitting = true, errorMessage = null) }

        viewModelScope.launch {
            val res: ApiState<Unit> = organizationRepository.createStudyGroup(request)

            resultResponse(
                response = res,
                successCallback = {
                    updateState { copy(isSubmitting = false) }
                    emitEvent(AdminStudyGroupAddEvent.CreateSuccess)
                },
                errorCallback = { fail ->
                    updateState { copy(isSubmitting = false, errorMessage = fail.message) }
                    emitEvent(AdminStudyGroupAddEvent.ShowToast(fail.message ?: "그룹 생성에 실패했어요."))
                }
            )
        }
    }
}

