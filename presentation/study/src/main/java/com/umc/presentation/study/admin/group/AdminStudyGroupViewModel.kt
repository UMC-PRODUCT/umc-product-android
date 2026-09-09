package com.umc.presentation.study.admin.group

import com.umc.domain.model.enums.UserPart
import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.organization.UpdateStudyGroupRequest
import com.umc.domain.usecase.organization.AddStudyGroupMemberUseCase
import com.umc.domain.usecase.organization.DeleteStudyGroupMemberUseCase
import com.umc.domain.usecase.organization.DeleteStudyGroupUseCase
import com.umc.domain.usecase.organization.GetManagedStudyGroupsUseCase
import com.umc.domain.usecase.organization.UpdateStudyGroupUseCase
import com.umc.presentation.study.admin.group.create.AdminStudyGroupCreateMemberUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

/**
 * 관리자 스터디 그룹 화면의 ViewModel
 *
 * 관리자가 담당하는 스터디 그룹 목록과
 * 그룹 수정 / 삭제 / 멤버 수정 기능을 관리합니다.
 *
 * 주요 기능
 * - 관리자 스터디 그룹 목록 조회 및 갱신
 * - 그룹 생성 화면 이동
 * - 그룹 설정 메뉴 상태 관리
 * - 그룹 정보 수정
 * - 그룹 삭제
 * - 일정 등록 화면 이동
 * - 스터디원 추가 및 삭제
 * - 멤버 수정 BottomSheet 상태 관리
 */
@HiltViewModel
class AdminStudyGroupViewModel @Inject constructor(
    private val getManagedStudyGroupsUseCase: GetManagedStudyGroupsUseCase,
    private val updateStudyGroupUseCase: UpdateStudyGroupUseCase,
    private val deleteStudyGroupUseCase: DeleteStudyGroupUseCase,
    private val addStudyGroupMemberUseCase: AddStudyGroupMemberUseCase,
    private val deleteStudyGroupMemberUseCase: DeleteStudyGroupMemberUseCase,
) : BaseViewModel<
        AdminStudyGroupState,
        AdminStudyGroupEvent,
        >(
    AdminStudyGroupState()
) {

    /**
     * ViewModel 생성 시 관리자 스터디 그룹 목록을 최초 조회합니다.
     */
    init {
        loadManagedStudyGroups()
    }

    /**
     * 외부에서 스터디 그룹 목록 갱신이 필요한 경우 호출합니다.
     *
     * 예)
     * - 관리자 스터디 탭 재진입
     * - 그룹 생성 후 목록 복귀
     * - 그룹 수정/삭제 후 갱신
     */
    fun refreshGroups() {
        loadManagedStudyGroups()
    }

    /**
     * 관리자 스터디 그룹 화면에서 발생한 사용자 액션 처리
     */
    fun onAction(
        action: AdminStudyGroupAction,
    ) {
        when (action) {

            /**
             * 스터디 그룹 목록을 다시 조회합니다.
             */
            is AdminStudyGroupAction.LoadGroups -> {
                loadManagedStudyGroups()
            }

            /**
             * 스터디 그룹 생성 화면으로 이동합니다.
             */
            is AdminStudyGroupAction.ClickCreateGroup -> {
                emitEvent(
                    AdminStudyGroupEvent.NavigateCreateGroup
                )
            }

            /**
             * 특정 그룹의 설정 메뉴를 표시합니다.
             */
            is AdminStudyGroupAction.ClickSetting -> {
                updateState {
                    copy(
                        selectedSettingItem = action.item
                    )
                }
            }

            /**
             * 현재 열려 있는 그룹 설정 Popup을 닫습니다.
             */
            is AdminStudyGroupAction.DismissSettingPopup -> {
                updateState {
                    copy(
                        selectedSettingItem = null
                    )
                }
            }

            /**
             * 선택한 그룹의 스터디 일정 등록 화면으로 이동합니다.
             *
             * 네비게이션에 필요한 그룹 ID, 이름, 파트 정보를
             * Event로 Route에 전달합니다.
             */
            is AdminStudyGroupAction.ClickAddSchedule -> {
                emitEvent(
                    AdminStudyGroupEvent.NavigateAddSchedule(
                        groupId = action.item.groupId,
                        groupTitle = action.item.title,
                        groupPart = action.item.studyPart,
                    )
                )
            }

            /**
             * 멤버 수정 화면을 외부에서 열어야 하는 경우
             * 선택한 그룹 정보를 Event로 전달합니다.
             */
            is AdminStudyGroupAction.ClickEditMembers -> {
                emitEvent(
                    AdminStudyGroupEvent.OpenEditMembers(
                        action.item
                    )
                )
            }

            /**
             * 그룹 정보 수정 Dialog를 엽니다.
             *
             * 수정 대상 그룹의 현재 이름과 파트를
             * 수정용 상태값으로 초기화합니다.
             */
            is AdminStudyGroupAction.OpenEditDialog -> {
                updateState {
                    copy(
                        // 설정 Popup 닫기
                        selectedSettingItem = null,

                        // 수정 대상 그룹 저장
                        editTargetItem = action.item,

                        // 현재 그룹 이름 초기값
                        editGroupName = action.item.title,

                        // 현재 파트 초기값
                        editPartLabel = action.item.partLabel
                            .ifBlank {
                                DEFAULT_EDIT_PART.label
                            },
                    )
                }
            }

            /**
             * 그룹 정보 수정 Dialog를 닫고
             * 수정용 임시 상태를 초기화합니다.
             */
            is AdminStudyGroupAction.CloseEditDialog -> {
                updateState {
                    copy(
                        editTargetItem = null,
                        editGroupName = "",
                        editPartLabel = DEFAULT_EDIT_PART.label,
                    )
                }
            }

            /**
             * 그룹 정보 수정 중 그룹 이름 변경값을 저장합니다.
             */
            is AdminStudyGroupAction.OnEditGroupNameChanged -> {
                updateState {
                    copy(
                        editGroupName = action.name
                    )
                }
            }

            /**
             * 그룹 정보 수정 중 선택된 파트를 저장합니다.
             */
            is AdminStudyGroupAction.OnEditPartChanged -> {
                updateState {
                    copy(
                        editPartLabel = action.partLabel
                    )
                }
            }

            /**
             * 그룹 정보 수정을 확정합니다.
             *
             * 현재 수정 대상 그룹과 입력값을 이용해
             * UpdateStudyGroupUseCase를 호출합니다.
             */
            is AdminStudyGroupAction.ConfirmEditGroup -> {
                val target =
                    uiState.value.editTargetItem
                        ?: return

                val newName =
                    uiState.value.editGroupName.trim()

                val newPart =
                    uiState.value.editPartLabel
                        .toPartApiValue()

                // 그룹 이름이 비어 있으면 수정 요청하지 않음
                if (newName.isBlank()) {
                    return
                }

                viewModelScope.launch {
                    when (
                        val result = updateStudyGroupUseCase(
                            studyGroupId = target.groupId,
                            request = UpdateStudyGroupRequest(
                                name = newName,
                                part = newPart,
                            ),
                        )
                    ) {
                        /**
                         * 그룹 정보 수정 성공
                         */
                        is ApiState.Success -> {
                            // 수정된 내용 반영을 위해 목록 갱신
                            loadManagedStudyGroups()

                            // 수정 Dialog 상태 초기화
                            updateState {
                                copy(
                                    editTargetItem = null,
                                    editGroupName = "",
                                    editPartLabel = DEFAULT_EDIT_PART.label,
                                )
                            }

                            emitEvent(
                                AdminStudyGroupEvent.ShowToast(
                                    "그룹 정보가 수정됐어요."
                                )
                            )
                        }

                        /**
                         * 그룹 정보 수정 실패
                         */
                        is ApiState.Fail -> {
                            emitEvent(
                                AdminStudyGroupEvent.ShowToast(
                                    "수정에 실패했어요."
                                )
                            )
                        }
                    }
                }
            }

            /**
             * 그룹 삭제 확인 Dialog를 엽니다.
             */
            is AdminStudyGroupAction.OpenDeleteDialog -> {
                updateState {
                    copy(
                        // 설정 Popup 닫기
                        selectedSettingItem = null,

                        // 삭제 대상 그룹 저장
                        deleteTargetItem = action.item,
                    )
                }
            }

            /**
             * 그룹 삭제 확인 Dialog를 닫습니다.
             */
            is AdminStudyGroupAction.CloseDeleteDialog -> {
                updateState {
                    copy(
                        deleteTargetItem = null
                    )
                }
            }

            /**
             * 선택된 스터디 그룹 삭제를 확정합니다.
             */
            is AdminStudyGroupAction.ConfirmDeleteGroup -> {
                val target =
                    uiState.value.deleteTargetItem
                        ?: return

                viewModelScope.launch {
                    when (
                        val result = deleteStudyGroupUseCase(
                            target.groupId
                        )
                    ) {
                        /**
                         * 그룹 삭제 성공
                         */
                        is ApiState.Success -> {
                            // 삭제 Dialog 닫기
                            updateState {
                                copy(
                                    deleteTargetItem = null
                                )
                            }

                            // 삭제된 그룹을 반영하도록 목록 갱신
                            loadManagedStudyGroups()

                            emitEvent(
                                AdminStudyGroupEvent.ShowToast(
                                    "그룹이 삭제됐어요."
                                )
                            )
                        }

                        /**
                         * 그룹 삭제 실패
                         */
                        is ApiState.Fail -> {
                            emitEvent(
                                AdminStudyGroupEvent.ShowToast(
                                    "삭제에 실패했어요."
                                )
                            )
                        }
                    }
                }
            }

            /**
             * 그룹 카드의 파란색 + 버튼을 눌렀을 때
             * 멤버 수정 BottomSheet를 엽니다.
             *
             * 현재 그룹 멤버 정보를
             * AdminStudyGroupCreateMemberUiModel로 변환하여
             * BottomSheet의 초기 선택값으로 전달합니다.
             */
            is AdminStudyGroupAction.OpenMemberBottomSheet -> {
                val targetGroup = action.item

                val currentMembers =
                    targetGroup.members.map { member ->
                        AdminStudyGroupCreateMemberUiModel(
                            // Managed API에서는 실제로 memberId 값
                            id = member.challengerId,

                            name = member.name,

                            // 현재 Managed API에서는 nickname 정보가 없으므로
                            // 이름만 displayName으로 사용
                            displayName = member.name,

                            // 그룹의 파트를 멤버 파트로 사용
                            partLabel = targetGroup.partLabel,

                            school = member.school,

                            // 기존 그룹 멤버의 프로필 이미지 전달
                            profileImageUrl =
                                member.profileImageUrl,
                        )
                    }.toImmutableList()

                updateState {
                    copy(
                        // 현재 멤버를 수정 중인 그룹
                        memberEditTargetItem = targetGroup,

                        // BottomSheet에 표시할 기존 스터디원
                        editingMembers = currentMembers,
                    )
                }
            }

            /**
             * 멤버 수정 BottomSheet를 닫고
             * 수정 대상 및 임시 멤버 목록을 초기화합니다.
             */
            AdminStudyGroupAction.CloseMemberBottomSheet -> {
                updateState {
                    copy(
                        memberEditTargetItem = null,
                        editingMembers = persistentListOf(),
                    )
                }
            }

            /**
             * 멤버 수정 BottomSheet에서 확정한 최종 멤버 목록을 반영합니다.
             *
             * 기존 멤버 ID와 새 멤버 ID를 비교하여
             * 추가해야 할 멤버와 삭제해야 할 멤버를 계산합니다.
             */
            is AdminStudyGroupAction.ConfirmMemberChanges -> {
                val targetGroup =
                    uiState.value.memberEditTargetItem
                        ?: return

                val updatedMembers =
                    action.members

                /**
                 * 기존 그룹 멤버 ID 집합
                 */
                val oldMemberIds =
                    targetGroup.members
                        .map { member ->
                            member.challengerId
                        }
                        .toSet()

                /**
                 * 수정 완료 후 최종 멤버 ID 집합
                 */
                val newMemberIds =
                    updatedMembers
                        .map { member ->
                            member.id
                        }
                        .toSet()

                /**
                 * 새롭게 추가된 멤버
                 *
                 * 새 목록에는 있지만 기존 목록에는 없는 ID
                 */
                val memberIdsToAdd =
                    newMemberIds - oldMemberIds

                /**
                 * 삭제된 멤버
                 *
                 * 기존 목록에는 있지만 새 목록에는 없는 ID
                 */
                val memberIdsToDelete =
                    oldMemberIds - newMemberIds

                viewModelScope.launch {
                    var hasFailed = false

                    /**
                     * 삭제 대상 멤버 API 호출
                     */
                    memberIdsToDelete.forEach { memberId ->
                        when (
                            deleteStudyGroupMemberUseCase(
                                studyGroupId =
                                    targetGroup.groupId,
                                memberId = memberId,
                            )
                        ) {
                            is ApiState.Success -> Unit

                            is ApiState.Fail -> {
                                hasFailed = true
                            }
                        }
                    }

                    /**
                     * 추가 대상 멤버 API 호출
                     */
                    memberIdsToAdd.forEach { memberId ->
                        when (
                            addStudyGroupMemberUseCase(
                                studyGroupId =
                                    targetGroup.groupId,
                                memberId = memberId,
                            )
                        ) {
                            is ApiState.Success -> Unit

                            is ApiState.Fail -> {
                                hasFailed = true
                            }
                        }
                    }

                    /**
                     * 멤버 추가/삭제 중 하나라도 실패한 경우
                     * 성공 처리하지 않습니다.
                     */
                    if (hasFailed) {
                        return@launch
                    }

                    // 멤버 수정 BottomSheet 닫기
                    updateState {
                        copy(
                            memberEditTargetItem = null,
                            editingMembers = persistentListOf(),
                        )
                    }

                    // 변경된 멤버 목록 반영
                    loadManagedStudyGroups()

                    emitEvent(
                        AdminStudyGroupEvent.ShowToast(
                            "스터디원이 수정됐어요."
                        )
                    )
                }
            }
        }
    }

    /**
     * 관리자가 담당하는 스터디 그룹 목록 조회
     *
     * 서버에서 ManagedStudyGroup 목록을 가져온 뒤
     * 화면에서 사용하는 AdminStudyGroupItemUiModel로 변환합니다.
     */
    private fun loadManagedStudyGroups() {
        // 이미 로딩 중이라면 중복 요청 방지
        if (uiState.value.isLoading) {
            return
        }

        viewModelScope.launch {
            updateState {
                copy(
                    isLoading = true
                )
            }

            when (
                val result =
                    getManagedStudyGroupsUseCase(
                        cursor = null,
                        size = PAGE_SIZE,
                    )
            ) {
                /**
                 * 그룹 목록 조회 성공
                 */
                is ApiState.Success -> {
                    val page = result.data

                    updateState {
                        copy(
                            // Domain 모델을 화면용 UI 모델로 변환
                            groups = page.content.map { group ->
                                group.toUiModel()
                            }.toImmutableList(),

                            nextCursor =
                                page.nextCursor,

                            hasNext =
                                page.hasNext,

                            isLoading =
                                false,
                        )
                    }
                }

                /**
                 * 그룹 목록 조회 실패
                 */
                is ApiState.Fail -> {
                    updateState {
                        copy(
                            isLoading = false
                        )
                    }

                    emitEvent(
                        AdminStudyGroupEvent.ShowToast(
                            "스터디 그룹 목록을 불러오지 못했어요."
                        )
                    )
                }
            }
        }
    }

    /**
     * 화면 표시용 파트 이름을 서버 API 값으로 바꿉니다.
     *
     * 매핑은 [UserPart] 한 곳에서만 합니다. 예전에는 여기 when 에 없는 라벨("Spring")이 들어오면
     * `uppercase()` 로 흘러가 서버에 없는 "SPRING" 을 보내는 버그가 있었습니다.
     */
    private fun String.toPartApiValue(): String = UserPart.from(this).serverValue

    companion object {

        /** 수정 Dialog 의 파트 초기값 */
        private val DEFAULT_EDIT_PART = UserPart.WEB

        /**
         * 스터디 그룹 목록 한 번 조회 시 요청하는 최대 개수
         */
        private const val PAGE_SIZE = 20
    }
}