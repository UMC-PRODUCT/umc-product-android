package com.umc.presentation.ui.act.study.group.model

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.organization.StudyGroupPage
import com.umc.domain.repository.OrganizationRepository
import com.umc.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminStudyGroupViewModel @Inject constructor(
    private val organizationRepository: OrganizationRepository,
) : BaseViewModel<AdminStudyGroupState, AdminStudyGroupEvent>(AdminStudyGroupState()) {

    private var loaded = false

    fun loadGroups(force: Boolean = false) {
        if (loaded && !force) return
        loaded = true

        startLoading()
        viewModelScope.launch {
            val res: ApiState<StudyGroupPage> =
                organizationRepository.getMyStudyGroup(cursor = null, size = 20)

            resultResponse(
                response = res,
                successCallback = { page ->
                    val initialList = page.content.map { summary ->
                        AdminStudyGroupItemUiModel(
                            groupId = summary.groupId,
                            title = summary.name,
                            partLabel = "",
                            leaderName = summary.leader.name,
                            members = summary.members.map { it.name },
                            createdAtRaw = "",
                            memberCount = summary.members.size,
                            leaderUniv = "",
                        )
                    }
                    updateState { copy(groups = initialList) }

                    initialList.forEach { item ->
                        loadGroupDetailAndMerge(item.groupId)
                    }
                },
                errorCallback = {}
            )
        }
    }

    fun deleteGroup(groupId: Long) {
        startLoading()
        viewModelScope.launch {
            val res = organizationRepository.deleteStudyGroup(groupId)

            resultResponse(
                response = res,
                successCallback = {

                    updateState { copy(groups = groups.filterNot { it.groupId == groupId }) }

                    loadGroups(force = true)
                },
                errorCallback = {}
            )
        }
    }

    fun editGroup(groupId: Long, request: com.umc.domain.model.request.organization.EditStudyGroupRequest) {
        startLoading()
        viewModelScope.launch {
            val res = organizationRepository.editGroup(groupId, request)

            resultResponse(
                response = res,
                successCallback = {
                    loadGroups(force = true)
                },
                errorCallback = {}
            )
        }
    }

    private fun loadGroupDetailAndMerge(groupId: Long) {
        viewModelScope.launch {
            when (val res = organizationRepository.getStudyGroupDetail(groupId)) {
                is ApiState.Success -> {
                    val detail = res.data
                    updateState {
                        copy(
                            groups = groups.map { item ->
                                if (item.groupId != groupId) item
                                else item.copy(
                                    partLabel = detail.part.toPartUiLabel(),
                                    createdAtRaw = detail.createdAt,
                                    leaderUniv = detail.schools.firstOrNull()?.schoolName.orEmpty(),
                                    members = detail.members.map { it.name },

                                    memberCount = detail.members.size
                                )
                            }
                        )
                    }
                }
                is ApiState.Fail -> Unit
            }
        }
    }
}

private fun String.toPartUiLabel(): String = when (this.uppercase()) {
    "WEB" -> "Web"
    "ANDROID" -> "Android"
    "IOS" -> "iOS"
    "SERVER" -> "Server"
    "DESIGN" -> "Design"
    "PLAN" -> "Plan"
    else -> this
}