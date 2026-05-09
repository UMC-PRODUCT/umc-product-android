package com.umc.presentation.ui.act.study.group.model

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.organization.StudyGroupPage
import com.umc.domain.model.request.organization.ChallengerListRequest
import com.umc.domain.model.request.organization.EditStudyGroupRequest
import com.umc.domain.repository.OrganizationRepository
import com.umc.domain.repository.member.MemberRepository
import com.umc.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminStudyGroupViewModel @Inject constructor(
    private val organizationRepository: OrganizationRepository,
    private val memberRepository: MemberRepository,
) : BaseViewModel<AdminStudyGroupState, AdminStudyGroupEvent>(AdminStudyGroupState()) {

    private var loaded = false
    private var canManageStudyGroup: Boolean = false

    init {
        loadMyPermission()
    }

    private fun loadMyPermission() {
        viewModelScope.launch {
            when (val res = memberRepository.getMyProfile()) {
                is ApiState.Success -> {
                    canManageStudyGroup = true //권한 OK
                }

                is ApiState.Fail -> {
                    canManageStudyGroup = false
                }
            }
        }
    }

    fun onClickCreateGroup() {
//        if (!canManageStudyGroup) {
//            emitEvent(AdminStudyGroupEvent.ShowToast("현재 권한으로는 스터디 그룹을 생성할 수 없습니다."))
//            return
//        }

        emitEvent(AdminStudyGroupEvent.ClickCreateGroup)
    }

    fun onClickEditMembers(groupId: Long) {
        emitEvent(AdminStudyGroupEvent.ClickEditMembers(groupId))
    }

    fun replaceGroupMembers(
        groupId: Long,
        pickedMemberChallengerIds: List<Long>,
    ) {
        viewModelScope.launch {
            val req = ChallengerListRequest(
                challengerIds = pickedMemberChallengerIds.distinct()
            )

            val res = organizationRepository.changeGroupMember(groupId, req)

            resultResponse(
                response = res,
                successCallback = {
                    emitEvent(AdminStudyGroupEvent.ShowToast("스터디원이 수정됐어요."))
                    loadGroups(force = true)
                },
                errorCallback = { fail ->
                    emitEvent(AdminStudyGroupEvent.ShowToast(fail.message))
                }
            )
        }
    }

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
                    val dummy = AdminStudyGroupItemUiModel(
                        groupId = -999L,
                        title = "[더미] 안드로이드 스터디",
                        partLabel = "Android",
                        leaderName = "더미 리더",
                        leaderChallengerId = -1L,
                        leaderProfileImageUrl = null,
                        members = listOf(
                            AdminStudyGroupMemberUiModel(challengerId = -1L, name = "더미 멤버1", profileImageUrl = null),
                            AdminStudyGroupMemberUiModel(challengerId = -2L, name = "더미 멤버2", profileImageUrl = null),
                        ),
                        memberChallengerIds = listOf(-1L, -2L),
                        createdAtRaw = "2026-05-10",
                        memberCount = 2,
                        leaderUniv = "UMC 대학교",
                    )

                    val initialList = page.content.map { summary ->

                        val leaderName = summary.leader?.name.orEmpty()
                        val leaderId = summary.leader?.challengerId ?: -1L
                        val leaderProfile = summary.leader?.profileImageUrl

                        val members = summary.members.map { m ->
                            AdminStudyGroupMemberUiModel(
                                challengerId = m.challengerId,
                                name = m.name,
                                profileImageUrl = m.profileImageUrl
                            )
                        }

                        val memberIds = members.map { it.challengerId }.distinct()

                        AdminStudyGroupItemUiModel(
                            groupId = summary.groupId,
                            title = summary.name,
                            partLabel = "",
                            leaderName = leaderName,
                            leaderChallengerId = leaderId,
                            leaderProfileImageUrl = leaderProfile,
                            members = members,
                            memberChallengerIds = memberIds,
                            createdAtRaw = "",
                            memberCount = memberIds.size,
                            leaderUniv = "",
                        )
                    }

                    updateState { copy(groups = listOf(dummy) + initialList) }

                    initialList.forEach { item ->
                        loadGroupDetailAndMerge(item.groupId)
                    }
                },
                errorCallback = { }
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
                errorCallback = { }
            )
        }
    }

    fun editGroup(groupId: Long, request: EditStudyGroupRequest) {
        startLoading()
        viewModelScope.launch {
            val res = organizationRepository.editGroup(groupId, request)

            resultResponse(
                response = res,
                successCallback = { loadGroups(force = true) },
                errorCallback = { }
            )
        }
    }

    private fun loadGroupDetailAndMerge(groupId: Long) {
        viewModelScope.launch {
            when (val res = organizationRepository.getStudyGroupDetail(groupId)) {
                is ApiState.Success -> {
                    val detail = res.data

                    val leaderName = detail.leader?.name.orEmpty()
                    val leaderId = detail.leader?.challengerId ?: -1L
                    val leaderProfile = detail.leader?.profileImageUrl

                    val members = detail.members.map { m ->
                        AdminStudyGroupMemberUiModel(
                            challengerId = m.challengerId,
                            name = m.name,
                            profileImageUrl = m.profileImageUrl
                        )
                    }

                    val memberIds = members.map { it.challengerId }.distinct()

                    updateState {
                        copy(
                            groups = groups.map { item ->
                                if (item.groupId != groupId) item
                                else item.copy(
                                    partLabel = detail.part.toPartUiLabel(),
                                    createdAtRaw = detail.createdAt,
                                    leaderUniv = detail.schools.firstOrNull()?.schoolName.orEmpty(),
                                    leaderName = leaderName,
                                    leaderChallengerId = leaderId,
                                    leaderProfileImageUrl = leaderProfile,
                                    members = members,
                                    memberChallengerIds = memberIds,
                                    memberCount = memberIds.size
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

private fun String.toPartUiLabel(): String {
    val part = UserPart.from(this)
    return if (part == UserPart.UNKNOWN) this else part.label
}