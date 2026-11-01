package com.umc.presentation.act.normal.challenger

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.enums.UserChallengerRole
import com.umc.domain.model.act.challenger.ChallengerInfoDialogModel
import com.umc.domain.model.act.challenger.UserChallenger
import com.umc.domain.model.base.ApiState
import com.umc.domain.model.enums.UserPart
import com.umc.domain.usecase.challenger.GetChallengerDetailUseCase
import com.umc.domain.usecase.challenger.GetChallengerListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PAGE_SIZE = 100
private const val MAX_PAGES = 20

@HiltViewModel
class NormalChallengerViewModel @Inject constructor(
    private val getNormalChallengerListUseCase: GetChallengerListUseCase,
    private val getNormalChallengerDetailUseCase: GetChallengerDetailUseCase,
) : BaseViewModel<NormalChallengerUiState, NormalChallengerEvent>(
    NormalChallengerUiState()
) {
    private var challengerListJob: Job? = null

    fun onSearchKeywordChanged(keyword: String) {
        updateState { copy(searchKeyword = keyword) }
        getChallengers(
            keyword = keyword.trim().takeIf { it.isNotEmpty() },
            debounce = true
        )
    }

    fun refresh() {
        // 이미 목록이 있으면 탭 재진입 때 깜빡임 없이 조용히 갱신하고, 비었거나 오류였으면 로딩을 보인다.
        getChallengers(showLoading = uiState.value.sections.isEmpty())
    }

    fun openPartFilter() {
        updateState { copy(isPartFilterVisible = true) }
    }

    fun dismissPartFilter() {
        updateState { copy(isPartFilterVisible = false) }
    }

    fun selectPartFilter(part: UserPart) {
        val nextPart = part.takeUnless { uiState.value.selectedPart == it }
        updateState { copy(selectedPart = nextPart, isPartFilterVisible = false) }
        getChallengers(selectedPart = nextPart)
    }

    private fun getChallengers(
        keyword: String? = uiState.value.searchKeyword.trim().takeIf { it.isNotEmpty() },
        debounce: Boolean = false,
        selectedPart: UserPart? = uiState.value.selectedPart,
        showLoading: Boolean = true,
    ) {
        challengerListJob?.cancel()
        challengerListJob = viewModelScope.launch {
            if (debounce) delay(300)
            // 이전 검색 결과가 로딩·오류 화면 뒤에 남지 않도록 비운다. 조용한 갱신(탭 재진입)은 목록을 그대로 둔다.
            if (showLoading) {
                updateState { copy(loadState = NormalChallengerLoadState.LOADING, sections = persistentListOf()) }
            }
            startLoading()

            val response = fetchChallengers(keyword, selectedPart)
            // apiCall 이 CancellationException 을 Fail 로 삼키므로, 취소된 조회가 상태를 덮어쓰지 않게 한다.
            currentCoroutineContext().ensureActive()

            resultResponse(
                response = response,
                successCallback = { challengers ->
                    updateState {
                        copy(
                            sections = challengers.toSections(),
                            loadState = NormalChallengerLoadState.SUCCESS,
                            loadErrorMessage = null,
                        )
                    }
                },
                errorCallback = { failState ->
                    // 조용한 갱신이 실패하면 보이던 목록은 유지하고 토스트로 알린다.
                    // 보여줄 목록이 없을 때만 실패 원인(네트워크/서버)을 담아 오류 화면을 띄운다.
                    if (uiState.value.sections.isEmpty()) {
                        updateState {
                            copy(
                                loadState = NormalChallengerLoadState.ERROR,
                                loadErrorMessage = failState.message.takeIf { it.isNotBlank() },
                            )
                        }
                    } else {
                        emitEvent(NormalChallengerEvent.ShowToast(failState.message))
                    }
                }
            )
        }
    }

    /**
     * 파트를 고르지 않으면 part 를 보내지 않는다. 서버가 part 를 주면 그 파트 행만 돌려주므로
     * 파트가 없는 챌린저(회장·부회장 등)는 part 없이 조회해야 목록에 나온다.
     */
    private suspend fun fetchChallengers(
        keyword: String?,
        selectedPart: UserPart?,
    ): ApiState<List<UserChallenger>> {
        val challengers = mutableListOf<UserChallenger>()
        var cursor: Long? = null
        repeat(MAX_PAGES) {
            val response = getNormalChallengerListUseCase(
                cursor = cursor,
                size = PAGE_SIZE,
                schoolId = null,
                gisuId = null,
                keyword = keyword,
                part = selectedPart?.name
            )
            when (response) {
                is ApiState.Fail -> return response
                is ApiState.Success -> {
                    challengers += response.data.challengers
                    cursor = response.data.nextCursor
                    if (!response.data.hasNext || cursor == null) return ApiState.Success(challengers.distinctBy { it.id })
                }
            }
        }
        return ApiState.Success(challengers.distinctBy { it.id })
    }

    fun getChallengerDetail(challengerId: Long) {
        viewModelScope.launch {
            startLoading()

            resultResponse(
                response = getNormalChallengerDetailUseCase(challengerId),
                successCallback = { detail ->
                    updateState { copy(selectedChallenger = detail) }
                },
                errorCallback = { failState ->
                    emitEvent(NormalChallengerEvent.ShowToast(failState.message))
                }
            )
        }
    }

    fun dismissChallengerDetail() {
        updateState { copy(selectedChallenger = null) }
    }
}

data class NormalChallengerUiState(
    val searchKeyword: String = "",
    val selectedPart: UserPart? = null,
    val isPartFilterVisible: Boolean = false,
    val sections: ImmutableList<NormalChallengerSectionUi> = persistentListOf(),
    val selectedChallenger: ChallengerInfoDialogModel? = null,
    val loadState: NormalChallengerLoadState = NormalChallengerLoadState.LOADING,
    //목록 조회 실패 원인 문구(없으면 기본 안내)
    val loadErrorMessage: String? = null,
) : UiState

enum class NormalChallengerLoadState { LOADING, SUCCESS, ERROR }

data class NormalChallengerSectionUi(
    val partName: String,
    val members: ImmutableList<NormalChallengerMemberUi>,
)

data class NormalChallengerMemberUi(
    val id: Long,
    val nicknameWithName: String,
    val generation: String,
    val roleBadge: UserChallengerRole? = null, //역할 배지로 보여줄 역할 (교내 회장 · 부회장 · 파트장)
)

sealed interface NormalChallengerEvent : UiEvent {
    data class ShowToast(val message: String) : NormalChallengerEvent
}

private fun UserChallenger.toMemberUi(): NormalChallengerMemberUi {
    return NormalChallengerMemberUi(
        id = id,
        nicknameWithName = "$name($nickname)",
        generation = "${generation}기",
        roleBadge = role.takeIf { it.isVisible }
    )
}

private fun List<UserChallenger>.toSections(): ImmutableList<NormalChallengerSectionUi> {
    return groupBy { it.part }
        .toSortedMap(compareBy<UserPart> { UserPart.entries.indexOf(it) })
        .map { (part, members) ->
            NormalChallengerSectionUi(
                partName = part.label,
                members = members.map { it.toMemberUi() }.toImmutableList()
            )
        }
        .toImmutableList()
}
