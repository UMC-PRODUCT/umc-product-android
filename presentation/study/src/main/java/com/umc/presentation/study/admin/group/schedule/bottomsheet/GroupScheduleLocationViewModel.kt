package com.umc.presentation.study.admin.group.schedule.bottomsheet

import android.content.Context
import android.location.Geocoder
import android.os.Build
import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.home.LocationItem
import com.umc.domain.usecase.appDataStore.recent.GetRecentSearchPlaceUseCase
import com.umc.domain.usecase.appDataStore.recent.UpdateRecentSearchPlaceUseCase
import com.umc.domain.usecase.kakao.GetSearchLocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class GroupScheduleLocationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getRecentSearchPlaceUseCase: GetRecentSearchPlaceUseCase,
    private val updateRecentSearchPlaceUseCase: UpdateRecentSearchPlaceUseCase,
    private val getSearchLocationUseCase: GetSearchLocationUseCase
) : BaseViewModel<GroupScheduleLocationState, GroupScheduleLocationEvent>(
    GroupScheduleLocationState()
) {

    private val geocoder = Geocoder(context, Locale.KOREAN)

    init {
        loadRecentPlaces()
        setInitialLocation(37.3943, 126.6388)
    }

    private fun setInitialLocation(lat: Double, lng: Double) {
        updateState {
            copy(
                selectedPlace = LocationItem(
                    title = "현재 설정된 위치",
                    address = "지도를 움직이거나 검색하여 장소를 선택하세요.",
                    latitude = lat,
                    longitude = lng
                )
            )
        }
    }

    private fun loadRecentPlaces() {
        viewModelScope.launch {
            getRecentSearchPlaceUseCase().collect { places ->
                updateState { copy(recentSearchList = places) }
            }
        }
    }

    fun onQueryChanged(query: String) {
        updateState { copy(searchQuery = query) }
    }

    fun searchLocation(query: String) {
        if (query.isBlank()) return

        updateState { copy(isSearching = true) }

        viewModelScope.launch {
            resultResponse(
                response = getSearchLocationUseCase(query),
                successCallback = { locationList ->
                    updateState {
                        copy(
                            searchResultList = locationList,
                            isSearching = false
                        )
                    }

                    if (locationList.isEmpty()) {
                        emitEvent(GroupScheduleLocationEvent.ShowToast("검색 결과가 없습니다."))
                    } else {
                        saveRecentPlace(query)
                    }
                },
                errorCallback = {
                    updateState { copy(isSearching = false) }
                    emitEvent(GroupScheduleLocationEvent.ShowToast("장소 검색에 실패했습니다."))
                }
            )
        }
    }

    fun updateLocationFromCoordinates(lat: Double, lng: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(lat, lng, 1) { addresses ->
                        if (addresses.isNotEmpty()) {
                            val addr = addresses[0]
                            updateState {
                                copy(
                                    selectedPlace = LocationItem(
                                        title = addr.featureName ?: "지정된 위치",
                                        address = addr.getAddressLine(0) ?: "",
                                        latitude = lat,
                                        longitude = lng
                                    )
                                )
                            }
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(lat, lng, 1)

                    if (!addresses.isNullOrEmpty()) {
                        val addr = addresses[0]
                        updateState {
                            copy(
                                selectedPlace = LocationItem(
                                    title = addr.featureName ?: "지정된 위치",
                                    address = addr.getAddressLine(0) ?: "",
                                    latitude = lat,
                                    longitude = lng
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                updateState {
                    copy(
                        selectedPlace = LocationItem(
                            title = "선택한 지점",
                            address = "상세 주소를 불러올 수 없습니다.",
                            latitude = lat,
                            longitude = lng
                        )
                    )
                }
            }
        }
    }

    fun selectSearchResult(place: LocationItem) {
        updateState {
            copy(
                selectedPlace = place,
                searchResultList = emptyList(),
                searchQuery = place.title
            )
        }

        emitEvent(GroupScheduleLocationEvent.MoveCameraTo(place.latitude, place.longitude))
    }

    private fun saveRecentPlace(place: String) {
        viewModelScope.launch {
            updateRecentSearchPlaceUseCase(place)
        }
    }

    fun confirmSelection() {
        emitEvent(GroupScheduleLocationEvent.LocationConfirmed(uiState.value.selectedPlace))
    }
}

data class GroupScheduleLocationState(
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val selectedPlace: LocationItem = LocationItem("", "", 0.0, 0.0),
    val recentSearchList: List<String> = emptyList(),
    val searchResultList: List<LocationItem> = emptyList()
) : UiState

sealed interface GroupScheduleLocationEvent : UiEvent {
    data class ShowToast(val message: String) : GroupScheduleLocationEvent
    data class MoveCameraTo(val lat: Double, val lng: Double) : GroupScheduleLocationEvent
    data class LocationConfirmed(val placeInfo: LocationItem) : GroupScheduleLocationEvent
}