package com.example.presentation.act.admin.attendance.fixLocation

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.home.LocationItem
import com.umc.domain.usecase.kakao.GetSearchLocationUseCase
import com.umc.domain.usecase.schedule.UpdateScheduleLocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FixLocationViewModel @Inject constructor(
    private val getSearchLocationUseCase: GetSearchLocationUseCase, //장소 검색
    private val updateScheduleLocationUseCase: UpdateScheduleLocationUseCase, //세션 장소 수정
) : BaseViewModel<FixLocationUiState, FixLocationEvent>(
    FixLocationUiState()
) {
    //장소 검색어 입력
    fun onSearchKeywordChanged(keyword: String) {
        updateState { copy(searchKeyword = keyword) }
    }

    //검색어로 장소 검색
    fun searchLocation() {
        val keyword = uiState.value.searchKeyword.trim()
        if (keyword.isEmpty()) {
            updateState { copy(searchResults = emptyList()) }
            return
        }

        viewModelScope.launch {
            startLoading()
            resultResponse(
                response = getSearchLocationUseCase(keyword),
                successCallback = { locations ->
                    updateState { copy(searchResults = locations) }
                },
                errorCallback = { failState ->
                    emitEvent(FixLocationEvent.ShowToast(failState.message))
                }
            )
        }
    }

    //수정할 장소 선택
    fun selectLocation(location: LocationItem) {
        updateState { copy(selectedLocation = location) }
    }

    //선택한 장소로 세션 위치 수정
    fun updateLocation(scheduleId: Long) {
        val location = uiState.value.selectedLocation ?: return
        if (scheduleId <= 0L) return

        viewModelScope.launch {
            startLoading()
            resultResponse(
                response = updateScheduleLocationUseCase(
                    scheduleId = scheduleId,
                    locationName = location.address.ifBlank { location.title },
                    latitude = location.latitude,
                    longitude = location.longitude
                ),
                successCallback = {
                    emitEvent(FixLocationEvent.UpdateSuccess)
                },
                errorCallback = { failState ->
                    emitEvent(FixLocationEvent.ShowToast(failState.message))
                }
            )
        }
    }
}

data class FixLocationUiState(
    //장소 검색어
    val searchKeyword: String = "",
    //장소 검색 결과
    val searchResults: List<LocationItem> = emptyList(),
    //최근 주소 목록
    val recentAddresses: List<String> = emptyList(),
    //선택한 장소
    val selectedLocation: LocationItem? = null,
) : UiState

sealed interface FixLocationEvent : UiEvent {
    //위치 수정 성공
    data object UpdateSuccess : FixLocationEvent
    //토스트 표시
    data class ShowToast(val message: String) : FixLocationEvent
}
