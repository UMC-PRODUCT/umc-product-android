package com.umc.presentation.study.admin.group.schedule.bottomsheet

import com.umc.component.theme.AppStrings
import android.content.Context
import android.location.Address
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
import java.util.Locale
import javax.inject.Inject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 스터디 일정 장소 선택 BottomSheet의 상태와 로직을 관리하는 ViewModel
 *
 * 주요 기능
 * - 장소 검색
 * - 최근 검색어 조회 및 저장
 * - 지도 좌표를 주소로 변환
 * - 검색 결과 장소 선택
 * - 지도 카메라 이동 이벤트 전달
 * - 최종 장소 선택 결과 전달
 */
@HiltViewModel
class GroupScheduleLocationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getRecentSearchPlaceUseCase:
    GetRecentSearchPlaceUseCase,
    private val updateRecentSearchPlaceUseCase:
    UpdateRecentSearchPlaceUseCase,
    private val getSearchLocationUseCase:
    GetSearchLocationUseCase,
) : BaseViewModel<
        GroupScheduleLocationState,
        GroupScheduleLocationEvent,
        >(
    GroupScheduleLocationState()
) {

    /**
     * 지도 좌표를 실제 주소로 변환하기 위한 Geocoder
     */
    private val geocoder =
        Geocoder(
            context,
            Locale.KOREAN
        )

    init {
        // 최근 장소 검색 기록 조회
        loadRecentPlaces()

        // BottomSheet 최초 지도 위치
        setInitialLocation(
            37.3943,
            126.6388
        )
    }

    /**
     * 지도 최초 위치를 설정합니다.
     *
     * 사용자가 지도를 이동하거나 검색하기 전까지
     * 임시 위치 정보를 표시합니다.
     */
    private fun setInitialLocation(
        lat: Double,
        lng: Double,
    ) {
        updateState {
            copy(
                selectedPlace = LocationItem(
                    title = AppStrings.STUDY_SCHEDULE_LOCATION_CURRENT_TITLE,
                    address =
                        AppStrings.STUDY_SCHEDULE_LOCATION_MOVE_MAP_GUIDE,
                    latitude = lat,
                    longitude = lng,
                )
            )
        }
    }

    /**
     * DataStore에 저장된 최근 장소 검색어를 조회합니다.
     */
    private fun loadRecentPlaces() {
        viewModelScope.launch {
            getRecentSearchPlaceUseCase()
                .collect { places ->
                    updateState {
                        copy(
                            recentSearchList = places.toImmutableList()
                        )
                    }
                }
        }
    }

    /**
     * 장소 검색창 입력값 변경
     */
    fun onQueryChanged(
        query: String,
    ) {
        updateState {
            copy(
                searchQuery = query
            )
        }
    }

    /**
     * 입력된 키워드를 이용하여 장소를 검색합니다.
     *
     * 검색 성공 시 결과 목록을 저장하고,
     * 검색어를 최근 검색 기록에 추가합니다.
     */
    fun searchLocation(
        query: String,
    ) {
        if (query.isBlank()) {
            return
        }

        updateState {
            copy(
                isSearching = true
            )
        }

        viewModelScope.launch {
            resultResponse(
                response =
                    getSearchLocationUseCase(query),

                successCallback = { locationList ->
                    updateState {
                        copy(
                            searchResultList =
                                locationList.toImmutableList(),
                            isSearching =
                                false,
                        )
                    }

                    if (locationList.isEmpty()) {
                        emitEvent(
                            GroupScheduleLocationEvent.ShowToast(
                                AppStrings.STUDY_SCHEDULE_LOCATION_SEARCH_EMPTY
                            )
                        )
                    } else {
                        saveRecentPlace(query)
                    }
                },

                errorCallback = {
                    updateState {
                        copy(
                            isSearching = false
                        )
                    }

                    emitEvent(
                        GroupScheduleLocationEvent.ShowToast(
                            AppStrings.STUDY_SCHEDULE_LOCATION_SEARCH_ERROR
                        )
                    )
                },
            )
        }
    }

    /**
     * 지도 중심 좌표를 실제 주소 정보로 변환합니다.
     *
     * Android 버전에 따라 Geocoder API 호출 방식이 달라
     * Android 13 이상과 이하를 분리하여 처리합니다.
     */
    fun updateLocationFromCoordinates(
        lat: Double,
        lng: Double,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (
                    Build.VERSION.SDK_INT >=
                    Build.VERSION_CODES.TIRAMISU
                ) {
                    geocoder.getFromLocation(
                        lat,
                        lng,
                        1,
                        object : Geocoder.GeocodeListener {
                            override fun onGeocode(
                                addresses: MutableList<Address>,
                            ) {
                                applyAddress(
                                    lat,
                                    lng,
                                    addresses.firstOrNull(),
                                )
                            }

                            /**
                             * 오프라인 / 백엔드 오류 시에도
                             * 이전 장소가 남지 않도록 폴백 장소로 갱신합니다.
                             */
                            override fun onError(
                                errorMessage: String?,
                            ) {
                                applyFallbackPlace(lat, lng)
                            }
                        },
                    )
                } else {
                    @Suppress("DEPRECATION")
                    val addresses =
                        geocoder.getFromLocation(
                            lat,
                            lng,
                            1,
                        )

                    applyAddress(
                        lat,
                        lng,
                        addresses?.firstOrNull(),
                    )
                }
            } catch (e: Exception) {
                /**
                 * 주소 변환에 실패해도
                 * 현재 지도 좌표 자체는 유지합니다.
                 */
                applyFallbackPlace(lat, lng)
            }
        }
    }

    /**
     * 변환된 주소를 선택 장소로 반영합니다.
     *
     * 주소가 없으면(빈 결과) 폴백 장소로 대체합니다.
     */
    private fun applyAddress(
        lat: Double,
        lng: Double,
        address: Address?,
    ) {
        if (address == null) {
            applyFallbackPlace(lat, lng)
            return
        }

        updateState {
            copy(
                selectedPlace =
                    LocationItem(
                        title =
                            address.featureName
                                ?: AppStrings.STUDY_SCHEDULE_LOCATION_UNSPECIFIED_TITLE,
                        address =
                            address.getAddressLine(0)
                                ?: "",
                        latitude = lat,
                        longitude = lng,
                    ),
                isPlaceSelected = true,
            )
        }
    }

    /**
     * 주소 변환에 실패했거나 결과가 없을 때
     * 좌표는 유지한 채 안내용 장소 정보로 갱신합니다.
     */
    private fun applyFallbackPlace(
        lat: Double,
        lng: Double,
    ) {
        updateState {
            copy(
                selectedPlace =
                    LocationItem(
                        title = AppStrings.STUDY_SCHEDULE_LOCATION_SELECTED_POINT_TITLE,
                        address =
                            AppStrings.STUDY_SCHEDULE_LOCATION_ADDRESS_UNAVAILABLE,
                        latitude = lat,
                        longitude = lng,
                    ),
                isPlaceSelected = true,
            )
        }
    }

    /**
     * 장소 검색 결과를 선택합니다.
     *
     * 선택한 장소를 현재 장소로 지정하고,
     * 해당 좌표로 지도 카메라를 이동하도록 Event를 전달합니다.
     */
    fun selectSearchResult(
        place: LocationItem,
    ) {
        updateState {
            copy(
                selectedPlace = place,
                isPlaceSelected = true,
                searchResultList = persistentListOf(),
                searchQuery = place.title,
            )
        }

        emitEvent(
            GroupScheduleLocationEvent.MoveCameraTo(
                place.latitude,
                place.longitude,
            )
        )
    }

    /**
     * 장소 검색어를 최근 검색 기록에 저장합니다.
     */
    private fun saveRecentPlace(
        place: String,
    ) {
        viewModelScope.launch {
            updateRecentSearchPlaceUseCase(
                place
            )
        }
    }

    /**
     * 현재 선택된 장소를 최종 확정합니다.
     */
    fun confirmSelection() {
        // 지도 이동이나 검색 없이는 기본 위치를 장소로 확정하지 않음
        if (!uiState.value.isPlaceSelected) {
            emitEvent(
                GroupScheduleLocationEvent.ShowToast(
                    AppStrings.STUDY_SCHEDULE_LOCATION_MOVE_MAP_GUIDE
                )
            )
            return
        }

        emitEvent(
            GroupScheduleLocationEvent.LocationConfirmed(
                uiState.value.selectedPlace
            )
        )
    }
}

/**
 * 일정 장소 선택 BottomSheet의 UI 상태
 */
data class GroupScheduleLocationState(

    /** 장소 검색어 */
    val searchQuery: String = "",

    /** 장소 검색 API 요청 여부 */
    val isSearching: Boolean = false,

    /** 현재 지도에서 선택된 장소 */
    val selectedPlace: LocationItem =
        LocationItem(
            "",
            "",
            0.0,
            0.0,
        ),

    /** 사용자가 지도 이동 또는 검색으로 장소를 직접 선택했는지 여부 */
    val isPlaceSelected: Boolean = false,

    /** 최근 장소 검색어 목록 */
    val recentSearchList: ImmutableList<String> =
        persistentListOf(),

    /** 장소 검색 결과 */
    val searchResultList: ImmutableList<LocationItem> =
        persistentListOf(),
) : UiState

/**
 * 일정 장소 선택 화면의 일회성 UI 이벤트
 */
sealed interface GroupScheduleLocationEvent : UiEvent {

    /** Toast 메시지 표시 */
    data class ShowToast(
        val message: String,
    ) : GroupScheduleLocationEvent

    /** 지도 카메라를 지정된 좌표로 이동 */
    data class MoveCameraTo(
        val lat: Double,
        val lng: Double,
    ) : GroupScheduleLocationEvent

    /** 최종 장소 선택 완료 */
    data class LocationConfirmed(
        val placeInfo: LocationItem,
    ) : GroupScheduleLocationEvent
}