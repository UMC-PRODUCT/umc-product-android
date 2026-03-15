package com.umc.presentation.ui.act.challenge

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.act.challenger.AdminChallenger
import com.umc.domain.model.act.challenger.ChallengerManageDialogModel
import com.umc.domain.model.act.challenger.UserPartCount
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    private var searchJob: Job? = null
    private val partOrder = UserPart.entries.filter { it != UserPart.UNKNOWN }

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
                    updateState {
                        copy(
                            schoolId = userInfo.schoolId,
                            gisuId = userInfo.roles.firstOrNull()?.gisuId,
                            allChallengers = emptyList(),
                            currentPartIndex = 0,
                            nextCursor = null,
                            hasNext = true
                        )
                    }
                    fetchNextPage(isFirstPage = true)
                }
        }
    }

    fun fetchNextPage(isFirstPage: Boolean = false) {
        val currentState = uiState.value
        // 검색 중 첫 페이지 진입 조건 추가
        val isSearchFirstPage = currentState.searchQuery.isNotBlank() && currentState.allChallengers.isEmpty()

        if (!isFirstPage && !isSearchFirstPage && (!currentState.hasNext || currentState.isPageLoading)) return

        viewModelScope.launch {
            updateState { copy(isPageLoading = true) }

            val isSearching = currentState.searchQuery.isNotBlank()
            val requestPart = if (isSearching) null else partOrder.getOrNull(currentState.currentPartIndex)

            resultResponse(
                response = getAdminChallengerListUseCase(
                    cursor = if (isFirstPage || isSearchFirstPage) null else currentState.nextCursor,
                    size = 50,
                    schoolId = currentState.schoolId,
                    gisuId = null, //currentState.gisuId
                    keyword = if (isSearching) currentState.searchQuery else null, // keyword 사용
                    part = requestPart?.name
                ),
                successCallback = { data ->
                    val currentPartFinished = !data.hasNext && !isSearching
                    val hasMoreParts = currentState.currentPartIndex < partOrder.size - 1

                    updateState {
                        val mergedList = if (isFirstPage || isSearchFirstPage) data.challengers
                        else (allChallengers + data.challengers).distinctBy { it.id }

                        copy(
                            allChallengers = mergedList,
                            partCounts = data.partCounts,
                            filteredGroups = makeChallengerGroups(mergedList, data.partCounts),
                            nextCursor = if (currentPartFinished && hasMoreParts) null else data.nextCursor,
                            hasNext = if (currentPartFinished) hasMoreParts else data.hasNext,
                            currentPartIndex = if (currentPartFinished && hasMoreParts) currentPartIndex + 1 else currentPartIndex,
                            isPageLoading = false
                        )
                    }

                    // 빈 파트 자동 스킵 로직
                    if (currentPartFinished && hasMoreParts && data.challengers.isEmpty()) {
                        fetchNextPage(isFirstPage = false)
                    }
                },
                errorCallback = { fail ->
                    updateState { copy(isPageLoading = false) }
                    emitEvent(AdminChallengerEvent.ShowToast(fail.message, isError = true))
                }
            )
        }
    }

    fun filterList(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500L)
            updateState {
                copy(
                    searchQuery = query,
                    allChallengers = emptyList(),
                    isPageLoading = false, // 로딩 상태 초기화
                    currentPartIndex = 0,
                    nextCursor = null,
                    hasNext = true
                )
            }
            fetchNextPage(isFirstPage = true)
        }
    }

    private fun makeChallengerGroups(list: List<AdminChallenger>, partCounts: List<UserPartCount>): List<AdminChallengerGroupUIModel> {
        // User 버전과 동일하게 entries 기반으로 매핑
        return UserPart.entries.filter { it != UserPart.UNKNOWN }.mapNotNull { part ->
            val members = list.filter { it.part == part }
            val currentPartCount = partCounts.find { it.part == part } ?: UserPartCount(part, 0)
            if (members.isNotEmpty()) {
                AdminChallengerGroupUIModel(part, members, currentPartCount)
            } else null
        }
    }

    /**
     * 상벌점 기록 부여
     */
    fun grantPoint(challengerId: Int, type: PointType, description: String) {
        viewModelScope.launch {
            val request = ChallengerPointRequest(type, description)
            resultResponse(
                response = grantChallengerPointUseCase(challengerId.toLong(), request),
                successCallback = { data ->
                    emitEvent(AdminChallengerEvent.ShowManageDialog(data))
                },
                errorCallback = { failState ->
                    emitEvent(AdminChallengerEvent.ShowToast(failState.message, isError = true))
                }
            )
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
                    emitEvent(AdminChallengerEvent.ShowToast("기록이 삭제되었습니다.", isError = false)) // 성공
                    onChallengerClicked(challengerId)
                },
                errorCallback = { failState ->
                    emitEvent(AdminChallengerEvent.ShowToast(failState.message, isError = true)) // 에러
                }
            )
        }
    }

    fun onChallengerClicked(id: Int) {
        viewModelScope.launch {
            resultResponse(
                response = getAdminChallengerDetailUseCase(id.toLong()),
                successCallback = { data ->
                    emitEvent(AdminChallengerEvent.ShowManageDialog(data))
                },
                errorCallback = { failState ->
                    emitEvent(AdminChallengerEvent.ShowToast(failState.message, isError = true))
                }
            )
        }
    }
}

data class AdminChallengerUiState(
    val schoolId: Long = 0,
    val gisuId: Long? = null,
    val currentPartIndex: Int = 0,
    val allChallengers: List<AdminChallenger> = emptyList(),
    val partCounts: List<UserPartCount> = emptyList(),
    val filteredGroups: List<AdminChallengerGroupUIModel> = emptyList(),
    val searchQuery: String = "",
    val isPageLoading: Boolean = false,
    val nextCursor: Long? = null,
    val hasNext: Boolean = true
) : UiState

sealed interface AdminChallengerEvent : UiEvent {
    data class ShowManageDialog(val model: ChallengerManageDialogModel) : AdminChallengerEvent
    data class ShowToast(val message: String, val isError: Boolean = false) : AdminChallengerEvent
}