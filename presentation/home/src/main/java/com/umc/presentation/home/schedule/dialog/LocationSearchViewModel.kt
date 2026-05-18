package com.umc.presentation.home.schedule.dialog

import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.home.LocationItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class LocationSearchViewModel @Inject constructor()
    : BaseViewModel<LocationSearchUiState, LocationSearchEvent>(
        LocationSearchUiState()
    ){

    }

data class LocationSearchUiState(
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val selectedPlace: LocationItem = LocationItem("", "", 0.0, 0.0),
    val isMapMoving: Boolean = false,
    val searchResults: List<LocationItem> = emptyList()
) : UiState

sealed interface LocationSearchEvent : UiEvent {


}