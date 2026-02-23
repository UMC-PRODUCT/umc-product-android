package com.umc.presentation.ui.community.top

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.umc.domain.model.community.TrophyBody
import com.umc.domain.usecase.community.GetCommunityTrophyUseCase

import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TopPostViewModel @Inject constructor(
    private val getTrophyUseCase: GetCommunityTrophyUseCase
) :
BaseViewModel<TopPostFragmentUiState, TopPostFragmentEvent>(
    TopPostFragmentUiState()
) {


    //명예의전당게시글 가져오기
    fun fetchTrophies(week: Int?, school: String?, part: String?){
        viewModelScope.launch {
            resultResponse(
                response = getTrophyUseCase(week, school, part),
                successCallback = { trophies ->
                    updateState { copy(trophyList = trophies)}
                    Log.d("log_community", trophies.toString())
                },
                errorCallback = {
                    updateState { copy(trophyList = emptyList()) }
                }
            )

        }
    }
}



data class TopPostFragmentUiState(

    //서버에서 받아온 리스트
    val trophyList : List<TrophyBody> = listOf(),

    ) : UiState

sealed interface TopPostFragmentEvent : UiEvent {


}