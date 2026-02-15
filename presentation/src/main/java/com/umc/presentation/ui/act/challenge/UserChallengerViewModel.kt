package com.umc.presentation.ui.act.challenge

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.act.challenger.ChallengerInfoDialogModel
import com.umc.domain.model.act.challenger.UserChallenger
import com.umc.domain.repository.AppDataStoreRepository
import com.umc.domain.usecase.challenger.GetChallengerDetailUseCase
import com.umc.domain.usecase.challenger.GetChallengerListUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserChallengerViewModel @Inject constructor(
    private val appDataStoreRepository: AppDataStoreRepository,
    private val getChallengerListUseCase: GetChallengerListUseCase,
    private val getChallengerDetailUseCase: GetChallengerDetailUseCase
) : BaseViewModel<UserChallengerUiState, UserChallengerEvent>(UserChallengerUiState()) {

    init {
        observeUserInfo()
    }

    /**
     * DataStore의 유저 정보를 관찰하여 학교/기수 ID가 있을 때 리스트를 가져옵니다.
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

    private fun startFetchingAll(schoolId: Long, gisuId: Long?) {
        // 기존 리스트를 비우고 새로 시작합니다.
        updateState { copy(allChallengers = emptyList(), filteredChallengers = emptyList()) }
        val accumulator = mutableListOf<UserChallenger>()
        fetchAllPages(schoolId, gisuId, null, accumulator)
    }

    /**
     * hasNext가 false일 때까지 모든 페이지를 순차적으로 요청합니다.
     */
    private fun fetchAllPages(
        schoolId: Long,
        gisuId: Long?,
        cursor: Long?,
        accumulator: MutableList<UserChallenger>
    ) {
        viewModelScope.launch {
            resultResponse(
                response = getChallengerListUseCase(cursor, 50, schoolId, gisuId),
                successCallback = { data ->
                    accumulator.addAll(data.challengers)

                    if (data.hasNext) {
                        // 다음 페이지가 있으면 다시 호출합니다.
                        fetchAllPages(schoolId, gisuId, data.nextCursor, accumulator)
                    } else {
                        // 모든 데이터 확보 후 UI를 한 번에 갱신합니다.
                        updateState {
                            copy(
                                allChallengers = accumulator,
                                filteredChallengers = accumulator
                            )
                        }
                    }
                },
                errorCallback = { failState ->
                    emitEvent(UserChallengerEvent.ShowErrorToast(failState.message))
                }
            )
        }
    }

    /**
     * 클라이언트 측 로컬 필터링 수행
     */
    fun filterList(query: String) {
        val allChallengers = uiState.value.allChallengers

        if (query.isBlank()) {
            // 검색어가 비어있으면 전체 리스트를 보여줌
            updateState { copy(filteredChallengers = allChallengers) }
        } else {
            val filtered = allChallengers.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.nickname.contains(query, ignoreCase = true)
            }
            updateState { copy(filteredChallengers = filtered) }
        }
    }

    fun navigateToDetail(id: Long) {
        viewModelScope.launch {
            resultResponse(
                response = getChallengerDetailUseCase(id),
                successCallback = { detail ->
                    emitEvent(UserChallengerEvent.NavigateToDetail(detail))
                },
                errorCallback = { failState ->
                    emitEvent(UserChallengerEvent.ShowErrorToast(failState.message))
                }
            )
        }
    }
}

data class UserChallengerUiState(
    val allChallengers: List<UserChallenger> = emptyList(),
    val filteredChallengers: List<UserChallenger> = emptyList()
) : UiState

sealed interface UserChallengerEvent : UiEvent {
    data class NavigateToDetail(val model: ChallengerInfoDialogModel) : UserChallengerEvent
    data class ShowErrorToast(val message: String) : UserChallengerEvent
}