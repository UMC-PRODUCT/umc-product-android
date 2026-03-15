package com.umc.presentation.ui.act.challenge

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.umc.domain.model.request.challenger.ChallengerPointRequest
import com.umc.domain.model.enums.PointType
import com.umc.domain.usecase.challenger.DeleteChallengerPointUseCase
import com.umc.domain.usecase.challenger.GetAdminChallengerDetailUseCase
import com.umc.domain.usecase.challenger.GrantChallengerPointUseCase
import com.umc.presentation.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminChallengerDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getAdminChallengerDetailUseCase: GetAdminChallengerDetailUseCase,
    private val grantChallengerPointUseCase: GrantChallengerPointUseCase,
    private val deleteChallengerPointUseCase: DeleteChallengerPointUseCase
) : BaseViewModel<AdminChallengerDetailUiState, AdminChallengerDetailEvent>(
    AdminChallengerDetailUiState()
) {

    private val challengerId: Long = savedStateHandle.get<Long>("challengerId") ?: -1L

    init {
        fetchDetail()
    }

    fun fetchDetail() {
        viewModelScope.launch {
            updateState { copy(isLoading = true) }

            resultResponse(
                response = getAdminChallengerDetailUseCase(challengerId),
                successCallback = { data ->
                    val formattedHistory = data.history.map { point ->
                        point.copy(date = com.umc.presentation.util.UFormat.parseDateTime(point.date).first)
                    }
                    updateState {
                        copy(
                            isLoading = false,
                            model = data.copy(history = formattedHistory)
                        )
                    }
                },
                errorCallback = { fail ->
                    updateState { copy(isLoading = false) }
                    emitEvent(AdminChallengerDetailEvent.ShowToast(fail.message, true))
                }
            )
        }
    }

    fun toggleEditMode() {
        updateState { copy(isEditMode = !isEditMode) }
    }

    fun exitEditMode() {
        updateState { copy(isEditMode = false) }
    }

    fun setInputMode(type: PointType?) {
        updateState { copy(currentInputMode = type, inputReason = "") }
    }

    fun onReasonChanged(text: String) {
        updateState { copy(inputReason = text) }
    }

    fun grantPoint() {
        val state = uiState.value
        val type = state.currentInputMode ?: return
        val reason = state.inputReason.trim()
        if (reason.isBlank()) return

        viewModelScope.launch {
            resultResponse(
                response = grantChallengerPointUseCase(
                    challengerId,
                    ChallengerPointRequest(type, reason)
                ),
                successCallback = {
                    emitEvent(
                        AdminChallengerDetailEvent.ShowToast(
                            if (type == PointType.OUT) "상점이 부여되었습니다." else "벌점이 부여되었습니다.",
                            false
                        )
                    )
                    updateState { copy(currentInputMode = null, inputReason = "") }
                    fetchDetail()
                },
                errorCallback = { fail ->
                    emitEvent(AdminChallengerDetailEvent.ShowToast(fail.message, true))
                }
            )
        }
    }

    fun deletePoint(pointId: Long) {
        viewModelScope.launch {
            resultResponse(
                response = deleteChallengerPointUseCase(pointId),
                successCallback = {
                    emitEvent(AdminChallengerDetailEvent.ShowToast("기록이 삭제되었습니다.", false))
                    fetchDetail()
                },
                errorCallback = { fail ->
                    emitEvent(AdminChallengerDetailEvent.ShowToast(fail.message, true))
                }
            )
        }
    }
}