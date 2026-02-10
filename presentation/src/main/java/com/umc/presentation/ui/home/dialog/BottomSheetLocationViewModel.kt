package com.umc.presentation.ui.home.dialog

import androidx.lifecycle.viewModelScope
import com.umc.domain.model.home.LocationItem
import com.umc.domain.usecase.appDataStore.recent.GetRecentSearchPlaceUseCase
import com.umc.domain.usecase.appDataStore.recent.UpdateRecentSearchPlaceUseCase
import com.umc.domain.usecase.kakao.GetSearchLocationUseCase
import com.umc.presentation.base.BaseViewModel
import com.umc.presentation.base.UiEvent
import com.umc.presentation.base.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BottomSheetLocationViewModel @Inject constructor(
    private val getRecentSearchPlaceUseCase: GetRecentSearchPlaceUseCase, //최근 장소 기록 불러오기;
    private val updateRecentSearchPlaceUseCase: UpdateRecentSearchPlaceUseCase, //최근 장소 기록 업데이트하기
    private val getSearchLocationUseCase: GetSearchLocationUseCase, //카카오 SDK로 장소 검색하기
) : BaseViewModel<BottomSheetLoactionUiState, BottomSheetLocationEvent>(
    BottomSheetLoactionUiState()
) {


    init {
        loadRecentPlaces()
    }

    //최근 장소 검색 기록을 불러오기
    private fun loadRecentPlaces() {
        viewModelScope.launch {
            getRecentSearchPlaceUseCase().collect { places ->
                updateState { copy(recentSearchList = places) }
            }
        }
    }

    //쿼리를 통해, 장소 검색을 수행
    fun searchLocation(query: String) {
        if (query.isBlank()) return // 빈 값은 검색하지 않음

        viewModelScope.launch {
            resultResponse(
                response = getSearchLocationUseCase(query),
                successCallback = { locationList ->
                    updateState {
                        copy(searchResultList = locationList)
                    }
                },
                errorCallback = { errorCode ->
                    /**검색 실패 로직**/
                }
            )
        }
    }

    //최근 장소를 저장
    fun saveRecentPlace(place: String) {
        viewModelScope.launch {
            updateRecentSearchPlaceUseCase(place)
        }
    }
}

data class BottomSheetLoactionUiState(
    //최근 장소 검색 기록 (DATASTORE)
    val recentSearchList: List<String> = emptyList(),

    //장소 검색 결과를 보여줄 곳
    val searchResultList: List<LocationItem> = emptyList()
) : UiState

sealed interface BottomSheetLocationEvent : UiEvent {

}