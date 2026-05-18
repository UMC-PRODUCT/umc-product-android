package com.umc.presentation.home.schedule.dialog

import android.content.Context
import android.location.Geocoder
import android.os.Build
import android.util.Log
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
class LocationSearchViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getRecentSearchPlaceUseCase: GetRecentSearchPlaceUseCase,
    private val updateRecentSearchPlaceUseCase: UpdateRecentSearchPlaceUseCase,
    private val getSearchLocationUseCase: GetSearchLocationUseCase
)
    : BaseViewModel<LocationSearchUiState, LocationSearchEvent>(
        LocationSearchUiState()
    ){

    private val geocoder = Geocoder(context, Locale.KOREAN)

    init{
        loadRecentPlaces()
        setInitialLocation(37.3943, 126.6388)
    }


    //처음 위치를 초기화
    private fun setInitialLocation(lat: Double, lng: Double) {
        updateState {
            copy(
                selectedPlace = LocationItem(
                    title = "현재 설정된 위치",
                    address = "지도를 움직여 정확한 장소를 선택하세요.",
                    latitude = lat,
                    longitude = lng
                )
            )
        }
    }

    //최근 장소 검색 기록을 불러오기
    private fun loadRecentPlaces() {
        viewModelScope.launch {
            getRecentSearchPlaceUseCase().collect { places ->
                updateState { copy(recentSearchList = places) }
            }
        }
    }

    //검색 쿼리 변경 시
    fun onQueryChanged(query: String) {
        updateState { copy(searchQuery = query) }
    }

    //카카오 API 기반 텍스트 위치 검색
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
                        emitEvent(LocationSearchEvent.ShowToast("검색 결과가 없습니다."))
                    } else {
                        //최근 검색어 저장 트리거
                        saveRecentPlace(query)
                    }
                },
                errorCallback = {
                    updateState { copy(isSearching = false) }
                    emitEvent(LocationSearchEvent.ShowToast("장소 검색에 실패했습니다."))
                }
            )
        }
    }

    //지도를 스와이프해서 멈췄을 때 위도/경도로 이름과 상세주소 추출
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
                // 네트워크 에러 시 좌표 정보라도 유지하여 선택 가능하게 보정
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

    //검색 결과 선택 시 해당 위경도로 지도 포커싱 이동
    fun selectSearchResult(place: LocationItem) {
        updateState {
            copy(
                selectedPlace = place,
                searchResultList = emptyList(), // 리스트 닫기
                searchQuery = place.title
            )
        }
        emitEvent(LocationSearchEvent.MoveCameraTo(place.latitude, place.longitude))
    }

    //검색 결과를 앱 내부에 저장
    private fun saveRecentPlace(place: String) {
        viewModelScope.launch {
            updateRecentSearchPlaceUseCase(place)
        }
    }

    fun confirmSelection() {
        emitEvent(LocationSearchEvent.LocationConfirmed(uiState.value.selectedPlace))
    }


    }

data class LocationSearchUiState(
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val selectedPlace: LocationItem = LocationItem("", "", 0.0, 0.0),
    val recentSearchList: List<String> = emptyList(),
    val searchResultList: List<LocationItem> = emptyList()
) : UiState

sealed interface LocationSearchEvent : UiEvent {
    data class ShowToast(val message: String) : LocationSearchEvent
    data class MoveCameraTo(val lat: Double, val lng: Double) : LocationSearchEvent
    data class LocationConfirmed(val placeInfo: LocationItem) : LocationSearchEvent

}