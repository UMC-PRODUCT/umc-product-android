package com.umc.presentation.act.admin.attendance.fixlocation

import android.content.Context
import android.location.Geocoder
import android.os.Build
import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.home.LocationItem
import com.umc.domain.usecase.kakao.GetSearchLocationUseCase
import com.umc.domain.usecase.schedule.UpdateScheduleLocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class FixLocationViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val getSearchLocationUseCase: GetSearchLocationUseCase, //장소 검색
    private val updateScheduleLocationUseCase: UpdateScheduleLocationUseCase, //세션 장소 수정
) : BaseViewModel<FixLocationUiState, FixLocationEvent>(
    FixLocationUiState()
) {
    private val geocoder = Geocoder(context, Locale.KOREAN)

    init {
        selectLocation(
            LocationItem(
                title = "현재 설정된 위치",
                address = "지도를 움직이거나 검색하여 장소를 선택하세요.",
                latitude = DEFAULT_LATITUDE,
                longitude = DEFAULT_LONGITUDE
            ),
            moveCamera = false
        )
    }

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
    fun selectLocation(location: LocationItem, moveCamera: Boolean = true) {
        updateState { copy(selectedLocation = location) }
        if (moveCamera) {
            emitEvent(FixLocationEvent.MoveCameraTo(location.latitude, location.longitude))
        }
    }

    // 지도 이동이 끝난 중심 좌표를 주소로 변환
    fun updateLocationFromCoordinates(latitude: Double, longitude: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                        val address = addresses.firstOrNull() ?: return@getFromLocation
                        selectLocation(
                            LocationItem(
                                title = address.featureName ?: "지정된 위치",
                                address = address.getAddressLine(0).orEmpty(),
                                latitude = latitude,
                                longitude = longitude
                            ),
                            moveCamera = false
                        )
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val address = geocoder.getFromLocation(latitude, longitude, 1)
                        ?.firstOrNull()
                        ?: return@launch

                    selectLocation(
                        LocationItem(
                            title = address.featureName ?: "지정된 위치",
                            address = address.getAddressLine(0).orEmpty(),
                            latitude = latitude,
                            longitude = longitude
                        ),
                        moveCamera = false
                    )
                }
            } catch (_: Exception) {
                selectLocation(
                    LocationItem(
                        title = "선택한 지점",
                        address = "상세 주소를 불러올 수 없습니다.",
                        latitude = latitude,
                        longitude = longitude
                    ),
                    moveCamera = false
                )
            }
        }
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
    //검색 결과 좌표로 지도 이동
    data class MoveCameraTo(val latitude: Double, val longitude: Double) : FixLocationEvent
}

private const val DEFAULT_LATITUDE = 37.3943
private const val DEFAULT_LONGITUDE = 126.6388
