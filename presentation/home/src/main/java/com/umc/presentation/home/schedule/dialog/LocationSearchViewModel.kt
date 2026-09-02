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

    // 위치 좌표 기반 주소명을 추출하기 위한 Geocoder 객체 (기획 변경으로 사용 X)
    private val geocoder = Geocoder(context, Locale.KOREAN)

    init{
        // 최근 장소 검색 기록 조회 실행
        loadRecentPlaces()

        // 기본 좌표 초기화 (현재 사용 X)
        setInitialLocation(37.3943, 126.6388)
    }


    /**
     * 화면 진입 시 초기 지도에 보여줄 대표 좌표 설정 및 안내 텍스트를 UI State에 설정하는 메서드
     *
     * @param lat 초기 위도
     * @param lng 초기 경도
     */
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

    /**
     * DataStore에 보관되어 있는 최근 장소 검색 기록 목록을 비동기 조회하는 메서드
     */
    private fun loadRecentPlaces() {
        viewModelScope.launch {
            getRecentSearchPlaceUseCase().collect { places ->
                updateState { copy(recentSearchList = places) }
            }
        }
    }

    /**
     * 장소 검색 입력창의 텍스트 변경 이벤트 처리 메서드
     *
     * @param query 사용자가 입력한 검색 쿼리 문자열
     */
    fun onQueryChanged(query: String) {
        updateState { copy(searchQuery = query) }
    }

    /**
     * 카카오 장소 검색 API를 호출하여 입력된 키워드 기반 위치 데이터를 검색하는 메서드
     *
     * @param query 장소 검색 키워드
     */
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

    /**
     * 지도의 위도/경도 좌표를 받아 Geocoder를 이용해 주소 및 지명 텍스트로 역지오코딩하는 메서드
     * [주의] 
     * 현재 사용 안해요
     * 
     * @param lat 선택 지점 위도
     * @param lng 선택 지점 경도
     */
    fun updateLocationFromCoordinates(lat: Double, lng: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Android 13(API 33) 이상 비동기 콜백 Geocoder 사용
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
                    // Android 12 이하 동기 Geocoder 사용
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

    /**
     * 장소 검색 결과 목록 중 특정 항목을 클릭했을 때 포커스를 이동시키고 UI 상태를 갱신하는 메서드
     * [주의]
     * 현재 사용 안해요
     *
     * @param place 선택한 장소 아이템
     */
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

    /**
     * 검색에 성공한 장소명을 DataStore 기반 최근 검색어 데이터로 기입하는 메서드
     *
     * @param place 저장할 장소 키워드
     */
    private fun saveRecentPlace(place: String) {
        viewModelScope.launch {
            updateRecentSearchPlaceUseCase(place)
        }
    }

    /**
     * 선택된 최종 장소 정보를 상위 스크린으로 넘겨주기 위한 확정 이벤트를 발행하는 메서드
     * [주의]
     * 현재 사용 안해요
     */
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