package com.umc.presentation.ui.act.challenge

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.act.challenger.AdminChallenger
import com.umc.domain.model.act.challenger.ChallengerManageDialogModel
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.enums.PointType
import com.umc.domain.model.enums.UserPart
import com.umc.domain.model.request.challenger.ChallengerPointRequest
import com.umc.domain.repository.AppDataStoreRepository
import com.umc.domain.usecase.challenger.DeleteChallengerPointUseCase
import com.umc.domain.usecase.challenger.GetAdminChallengerDetailUseCase
import com.umc.domain.usecase.challenger.GetAdminChallengerListUseCase
import com.umc.domain.usecase.challenger.GrantChallengerPointUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminChallengerViewModel @Inject constructor(
    private val appDataStoreRepository: AppDataStoreRepository,
    private val getAdminChallengerListUseCase: GetAdminChallengerListUseCase,
    private val getAdminChallengerDetailUseCase: GetAdminChallengerDetailUseCase,
    private val grantChallengerPointUseCase: GrantChallengerPointUseCase,
    private val deleteChallengerPointUseCase: DeleteChallengerPointUseCase
) : BaseViewModel<AdminChallengerUiState, AdminChallengerEvent>(AdminChallengerUiState()) {

    init {
        observeUserInfo()
    }

    /**
     * DataStore의 유저 정보를 관찰
     */
    private fun observeUserInfo() {
        viewModelScope.launch {
            appDataStoreRepository.getUserInfo()
                .distinctUntilChanged { old, new ->
                    old.schoolId == new.schoolId &&
                            old.roles.firstOrNull()?.gisuId == new.roles.firstOrNull()?.gisuId
                }
                .collect { userInfo ->
                    val currentGisuId = userInfo.roles.firstOrNull()?.gisuId
                    startFetchingAll(userInfo.schoolId, currentGisuId)
                }
        }
    }

    /**
     * 리스트 초기화 후 첫 페이지부터 불러오기
     */
    private fun startFetchingAll(schoolId: Long, gisuId: Long?) {
        updateState { copy(allChallengers = emptyList(), filteredChallengers = emptyList()) }
        val accumulator = mutableListOf<AdminChallenger>()
        fetchAllPages(schoolId, gisuId, null, accumulator)
    }

    /**
     * 모든 페이지를 순차적으로 요청
     */
    private fun fetchAllPages(
        schoolId: Long,
        gisuId: Long?,
        cursor: Long?,
        accumulator: MutableList<AdminChallenger>
    ) {
        viewModelScope.launch {
            resultResponse(
                response = getAdminChallengerListUseCase(cursor, 50, schoolId, gisuId),
                successCallback = { data ->
                    accumulator.addAll(data.challengers)

                    if (data.hasNext) {
                        fetchAllPages(schoolId, gisuId, data.nextCursor, accumulator)
                    } else {
                        updateState {
                            copy(
                                allChallengers = accumulator,
                                filteredChallengers = accumulator
                            )
                        }
                    }
                },
                errorCallback = { failState ->
                    emitEvent(AdminChallengerEvent.ShowErrorToast(failState.message))
                }
            )
        }
    }

    /**
     * 상벌점 기록 부여
     */
    fun grantPoint(challengerId: Int, type: PointType, description: String) {
        viewModelScope.launch {
            val request = ChallengerPointRequest(type, description)
            when (val result = grantChallengerPointUseCase(challengerId.toLong(), request)) {
                is ApiState.Success -> {
                    emitEvent(AdminChallengerEvent.ShowManageDialog(result.data))
                }
                is ApiState.Fail -> {
                    emitEvent(AdminChallengerEvent.ShowErrorToast(result.failState.message))
                }
            }
        }
    }

    /**
     * 상벌점 기록 삭제
     */
    fun deletePoint(challengerId: Int, challengerPointId: Long) {
        viewModelScope.launch {
            resultResponse(
                response = deleteChallengerPointUseCase(challengerPointId),
                successCallback = {
                    // 삭제 성공 시, 해당 챌린저의 상세 정보를 다시 불러와 다이얼로그를 갱신
                    onChallengerClicked(challengerId)
                },
                errorCallback = { failState ->
                    emitEvent(AdminChallengerEvent.ShowErrorToast(failState.message))
                }
            )
        }
    }

    fun filterList(query: String) {
        val filtered = uiState.value.allChallengers.filter {
            it.name.contains(query) || it.nickname.contains(query)
        }
        updateState { copy(filteredChallengers = filtered) }
    }

    // 상세 정보 API 호출 함수
    fun onChallengerClicked(id: Int) {
        viewModelScope.launch {
            when (val result = getAdminChallengerDetailUseCase(id.toLong())) {
                is ApiState.Success -> {
                    emitEvent(AdminChallengerEvent.ShowManageDialog(result.data))
                }
                is ApiState.Fail -> {
                    emitEvent(AdminChallengerEvent.ShowErrorToast(result.failState.message))
                }
            }
        }
    }
}

data class AdminChallengerUiState(
    val allChallengers: List<AdminChallenger> = emptyList(),
    val filteredChallengers: List<AdminChallenger> = emptyList()
) : UiState

sealed interface AdminChallengerEvent : UiEvent {
    data class ShowManageDialog(val model: ChallengerManageDialogModel) : AdminChallengerEvent
    data class ShowErrorToast(val message: String) : AdminChallengerEvent
}