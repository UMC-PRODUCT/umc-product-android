package com.umc.presentation.study.admin.group.create

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.request.organization.CreateStudyGroupRequest
import com.umc.domain.usecase.organization.CreateStudyGroupUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminStudyGroupCreateViewModel @Inject constructor(
    private val createStudyGroupUseCase: CreateStudyGroupUseCase,
) : BaseViewModel<AdminStudyGroupCreateState, AdminStudyGroupCreateEvent>(
    AdminStudyGroupCreateState()
) {

    fun onAction(action: AdminStudyGroupCreateAction) {
        when (action) {
            is AdminStudyGroupCreateAction.OnGroupNameChanged -> {
                updateState {
                    copy(groupName = action.value)
                }
            }

            AdminStudyGroupCreateAction.OnPartClick -> {
                updateState {
                    copy(showPartBottomSheet = true)
                }
            }

            AdminStudyGroupCreateAction.OnPartLeaderClick -> {
                updateState {
                    copy(showPartLeaderBottomSheet = true)
                }
            }

            AdminStudyGroupCreateAction.OnMemberClick -> {
                updateState {
                    copy(showMemberBottomSheet = true)
                }
            }

            AdminStudyGroupCreateAction.OnRegisterClick -> {
                createStudyGroup()
            }

            AdminStudyGroupCreateAction.OnBackClick -> {
                emitEvent(AdminStudyGroupCreateEvent.NavigateBack)
            }
        }
    }

    fun setGisuId(gisuId: Long) {
        updateState {
            copy(gisuId = gisuId)
        }
    }

    private fun createStudyGroup() {
        val state = uiState.value

        if (!state.isRegisterEnabled) {
            android.util.Log.d(
                "StudyGroupCreate",
                "등록 불가: state=$state"
            )
            return
        }

        val gisuId = state.gisuId ?: run {
            android.util.Log.d(
                "StudyGroupCreate",
                "등록 실패: gisuId가 null입니다."
            )
            return
        }

        val selectedPart = state.selectedPart ?: run {
            android.util.Log.d(
                "StudyGroupCreate",
                "등록 실패: selectedPart가 null입니다."
            )
            return
        }

        val request = CreateStudyGroupRequest(
            name = state.groupName.trim(),
            gisuId = gisuId,
            part = selectedPart.value,
            mentorIds = state.selectedPartLeaders.map { it.id },
            memberIds = state.selectedMembers.map { it.id },
        )

        android.util.Log.d(
            "StudyGroupCreate",
            "등록 요청: $request"
        )

        viewModelScope.launch {
            updateState {
                copy(isLoading = true)
            }

            when (val result = createStudyGroupUseCase(request)) {
                is ApiState.Success -> {
                    android.util.Log.d(
                        "StudyGroupCreate",
                        "등록 성공"
                    )

                    updateState {
                        copy(isLoading = false)
                    }

                    emitEvent(
                        AdminStudyGroupCreateEvent.RegisterSuccess
                    )
                }

                is ApiState.Fail -> {
                    android.util.Log.d(
                        "StudyGroupCreate",
                        "등록 실패: ${result.failState.message}"
                    )

                    updateState {
                        copy(isLoading = false)
                    }

                    emitEvent(
                        AdminStudyGroupCreateEvent.RegisterFailure(
                            message = result.failState.message
                        )
                    )
                }
            }
        }
    }

    fun dismissBottomSheet() {
        updateState {
            copy(
                showPartBottomSheet = false,
                showPartLeaderBottomSheet = false,
                showMemberBottomSheet = false,
            )
        }
    }

    fun selectPart(part: AdminStudyGroupCreatePartUiModel) {
        updateState {
            copy(
                selectedPart = part,
                selectedPartLeaders = emptyList(),
                selectedMembers = emptyList(),
                showPartBottomSheet = false,
            )
        }
    }

    fun selectPartLeaders(
        leaders: List<AdminStudyGroupCreateMemberUiModel>,
    ) {
        updateState {
            copy(
                selectedPartLeaders = leaders,
                showPartLeaderBottomSheet = false,
            )
        }
    }

    fun selectMembers(
        members: List<AdminStudyGroupCreateMemberUiModel>,
    ) {
        updateState {
            copy(
                selectedMembers = members,
                showMemberBottomSheet = false,
            )
        }
    }
}