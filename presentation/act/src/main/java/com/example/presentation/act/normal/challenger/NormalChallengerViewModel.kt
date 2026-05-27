package com.example.presentation.act.normal.challenger

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.act.challenger.ChallengerInfoDialogModel
import com.umc.domain.model.act.challenger.UserChallenger
import com.umc.domain.usecase.challenger.GetChallengerDetailUseCase
import com.umc.domain.usecase.challenger.GetChallengerListUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PAGE_SIZE = 30

@HiltViewModel
class NormalChallengerViewModel @Inject constructor(
    private val getNormalChallengerListUseCase: GetChallengerListUseCase, //일반 챌린저 목록 조회
    private val getNormalChallengerDetailUseCase: GetChallengerDetailUseCase, //일반 챌린저 상세 조회
) : BaseViewModel<NormalChallengerUiState, NormalChallengerEvent>(
    NormalChallengerUiState()
) {
    //초기 챌린저 목록 조회
    init {
        getChallengers()
    }

    //검색어 변경 시 목록 다시 조회
    fun onSearchKeywordChanged(keyword: String) {
        updateState { copy(searchKeyword = keyword) }
        getChallengers(keyword = keyword.trim().takeIf { it.isNotEmpty() })
    }

    //일반 챌린저 목록 조회
    fun getChallengers(
        cursor: Long? = null,
        keyword: String? = uiState.value.searchKeyword.trim().takeIf { it.isNotEmpty() },
    ) {
        viewModelScope.launch {
            startLoading()
            resultResponse(
                response = getNormalChallengerListUseCase(
                    cursor = cursor,
                    size = PAGE_SIZE,
                    schoolId = null,
                    gisuId = null,
                    keyword = keyword,
                    part = null
                ),
                successCallback = { challengerList ->
                    val challengers = if (cursor == null) {
                        challengerList.challengers
                    } else {
                        uiState.value.challengers + challengerList.challengers
                    }

                    updateState {
                        copy(
                            challengers = challengers,
                            nextCursor = challengerList.nextCursor,
                            hasNext = challengerList.hasNext
                        )
                    }
                },
                errorCallback = { failState ->
                    emitEvent(NormalChallengerEvent.ShowToast(failState.message))
                }
            )
        }
    }

    //다음 페이지 조회
    fun loadNextPage() {
        val state = uiState.value
        if (!state.hasNext) return
        getChallengers(cursor = state.nextCursor)
    }

    //선택한 챌린저 상세 정보 조회
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

    //챌린저 상세 다이얼로그 닫기
    fun dismissChallengerDetail() {
        updateState { copy(selectedChallenger = null) }
    }
}

data class NormalChallengerUiState(
    //검색어
    val searchKeyword: String = "",
    //일반 챌린저 목록
    val challengers: List<UserChallenger> = emptyList(),
    //선택한 챌린저 상세 정보
    val selectedChallenger: ChallengerInfoDialogModel? = null,
    //다음 페이지 커서
    val nextCursor: Long? = null,
    //다음 페이지 존재 여부
    val hasNext: Boolean = false,
) : UiState {
    //파트별로 그룹핑된 챌린저 목록
    val sections: List<NormalChallengerSectionUi>
        get() = challengers
            .groupBy { it.part.label }
            .map { (partName, members) ->
                NormalChallengerSectionUi(
                    partName = partName,
                    members = members.map { it.toMemberUi() }
                )
            }
}

data class NormalChallengerSectionUi(
    val partName: String,
    val members: List<NormalChallengerMemberUi>,
)

data class NormalChallengerMemberUi(
    val id: Long,
    val nicknameWithName: String,
    val generation: String,
    val roleBadge: String? = null,
)

sealed interface NormalChallengerEvent : UiEvent {
    //토스트 표시
    data class ShowToast(val message: String) : NormalChallengerEvent
}

//도메인 모델을 목록 UI 모델로 변환
private fun UserChallenger.toMemberUi(): NormalChallengerMemberUi {
    return NormalChallengerMemberUi(
        id = id,
        nicknameWithName = "$name($nickname)",
        generation = "${generation}기",
        roleBadge = role.displayName?.takeIf { role.isVisible }
    )
}
