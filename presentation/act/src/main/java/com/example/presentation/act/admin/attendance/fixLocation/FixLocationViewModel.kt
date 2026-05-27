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
    private val getSearchLocationUseCase: GetSearchLocationUseCase,
    private val updateScheduleLocationUseCase: UpdateScheduleLocationUseCase,
) : BaseViewModel<FixLocationUiState, FixLocationEvent>(
    FixLocationUiState()
) {
    fun onSearchKeywordChanged(keyword: String) {
        updateState { copy(searchKeyword = keyword) }
    }

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

    fun selectLocation(location: LocationItem) {
        updateState { copy(selectedLocation = location) }
    }

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
    val searchKeyword: String = "",
    val searchResults: List<LocationItem> = emptyList(),
    val recentAddresses: List<String> = emptyList(),
    val selectedLocation: LocationItem? = null,
) : UiState

sealed interface FixLocationEvent : UiEvent {
    data object UpdateSuccess : FixLocationEvent
    data class ShowToast(val message: String) : FixLocationEvent
}
