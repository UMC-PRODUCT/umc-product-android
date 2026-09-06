package com.umc.presentation.act.admin.attendance.fixlocation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import androidx.lifecycle.viewModelScope
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.home.LocationItem
import com.umc.domain.usecase.appDataStore.recent.GetRecentSearchPlaceUseCase
import com.umc.domain.usecase.appDataStore.recent.UpdateRecentSearchPlaceUseCase
import com.umc.domain.usecase.kakao.GetSearchLocationUseCase
import com.umc.domain.usecase.schedule.GetScheduleDetailUseCase
import com.umc.domain.usecase.schedule.UpdateScheduleLocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class FixLocationViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getRecentSearchPlaceUseCase: GetRecentSearchPlaceUseCase,
    private val updateRecentSearchPlaceUseCase: UpdateRecentSearchPlaceUseCase,
    private val getSearchLocationUseCase: GetSearchLocationUseCase, //장소 검색
    private val getScheduleDetailUseCase: GetScheduleDetailUseCase,
    private val updateScheduleLocationUseCase: UpdateScheduleLocationUseCase, //세션 장소 수정
) : BaseViewModel<FixLocationUiState, FixLocationEvent>(
    FixLocationUiState()
) {
    private val geocoder = Geocoder(context, Locale.KOREAN)
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private var searchJob: Job? = null

    init {
        observeRecentPlaces()
    }

    private fun observeRecentPlaces() {
        viewModelScope.launch {
            getRecentSearchPlaceUseCase().collect { places ->
                updateState { copy(recentAddresses = places.toImmutableList()) }
            }
        }
    }

    fun loadScheduleLocation(scheduleId: Long) {
        if (scheduleId <= 0L) return

        updateState {
            copy(
                searchKeyword = "",
                searchResults = persistentListOf(),
                selectedLocation = null,
            )
        }

        viewModelScope.launch {
            startLoading()
            resultResponse(
                response = getScheduleDetailUseCase(scheduleId),
                successCallback = { schedule ->
                    val hasSavedLocation =
                        schedule.address.isNotBlank() &&
                            schedule.latitude != 0.0 &&
                            schedule.longitude != 0.0

                    if (hasSavedLocation) {
                        selectLocation(
                            location = LocationItem(
                                title = schedule.address,
                                address = schedule.address,
                                latitude = schedule.latitude,
                                longitude = schedule.longitude,
                            )
                        )
                    }
                },
                errorCallback = { failState ->
                    emitEvent(FixLocationEvent.ShowToast(failState.message))
                }
            )
        }
    }

    //장소 검색어 입력
    fun onSearchKeywordChanged(keyword: String) {
        updateState {
            copy(
                searchKeyword = keyword,
                searchResults = if (keyword.isBlank()) persistentListOf() else searchResults,
            )
        }

        if (keyword.isBlank()) {
            searchJob?.cancel()
            return
        }

        searchLocation(
            keyword = keyword.trim(),
            debounce = true,
            saveRecent = false,
        )
    }

    //검색어로 장소 검색
    fun searchLocation() {
        val keyword = uiState.value.searchKeyword.trim()
        if (keyword.isEmpty()) {
            updateState { copy(searchResults = persistentListOf()) }
            return
        }

        searchLocation(keyword = keyword, debounce = false, saveRecent = true)
    }

    private fun searchLocation(
        keyword: String,
        debounce: Boolean,
        saveRecent: Boolean,
    ) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (debounce) delay(300)
            startLoading()
            resultResponse(
                response = getSearchLocationUseCase(keyword),
                successCallback = { locations ->
                    updateState { copy(searchResults = locations.toImmutableList()) }
                    if (saveRecent && locations.isNotEmpty()) {
                        viewModelScope.launch {
                            updateRecentSearchPlaceUseCase(keyword)
                        }
                    }
                },
                errorCallback = { failState ->
                    emitEvent(FixLocationEvent.ShowToast(failState.message))
                }
            )
        }
    }

    fun searchRecentLocation(query: String) {
        updateState { copy(searchKeyword = query) }
        searchLocation()
    }

    //수정할 장소 선택
    fun selectLocation(location: LocationItem, moveCamera: Boolean = true) {
        updateState { copy(selectedLocation = location) }
        if (moveCamera) {
            emitEvent(FixLocationEvent.MoveCameraTo(location.latitude, location.longitude))
        }
    }

    fun moveToCurrentLocation() {
        val hasLocationPermission =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

        if (!hasLocationPermission) {
            emitEvent(FixLocationEvent.ShowToast("현재 위치를 확인하려면 위치 권한이 필요합니다."))
            return
        }

        startLoading()
        try {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                CancellationTokenSource().token
            ).addOnSuccessListener { location ->
                stopLoading()
                if (location == null) {
                    emitEvent(FixLocationEvent.ShowToast("현재 위치를 확인할 수 없습니다."))
                    return@addOnSuccessListener
                }

                emitEvent(
                    FixLocationEvent.MoveCameraTo(
                        latitude = location.latitude,
                        longitude = location.longitude,
                    )
                )
                updateLocationFromCoordinates(location.latitude, location.longitude)
            }.addOnFailureListener {
                stopLoading()
                emitEvent(FixLocationEvent.ShowToast("현재 위치를 확인하지 못했습니다."))
            }
        } catch (_: SecurityException) {
            stopLoading()
            emitEvent(FixLocationEvent.ShowToast("현재 위치를 확인하려면 위치 권한이 필요합니다."))
        }
    }

    fun updateLocationFromCoordinates(latitude: Double, longitude: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                        val address = addresses.firstOrNull() ?: return@getFromLocation
                        selectLocation(
                            location = LocationItem(
                                title = address.featureName ?: "선택한 위치",
                                address = address.getAddressLine(0).orEmpty(),
                                latitude = latitude,
                                longitude = longitude,
                            ),
                            moveCamera = false,
                        )
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val address = geocoder.getFromLocation(latitude, longitude, 1)
                        ?.firstOrNull()
                        ?: return@launch
                    selectLocation(
                        location = LocationItem(
                            title = address.featureName ?: "선택한 위치",
                            address = address.getAddressLine(0).orEmpty(),
                            latitude = latitude,
                            longitude = longitude,
                        ),
                        moveCamera = false,
                    )
                }
            } catch (_: Exception) {
                selectLocation(
                    location = LocationItem(
                        title = "선택한 위치",
                        address = "",
                        latitude = latitude,
                        longitude = longitude,
                    ),
                    moveCamera = false,
                )
            }
        }
    }

    //선택한 장소로 세션 위치 수정
    fun updateLocation(scheduleId: Long, selectedLocation: LocationItem? = null) {
        val location = selectedLocation ?: uiState.value.selectedLocation ?: return
        if (scheduleId <= 0L) return
        updateState { copy(selectedLocation = location) }

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
    val searchResults: ImmutableList<LocationItem> = persistentListOf(),
    //최근 주소 목록
    val recentAddresses: ImmutableList<String> = persistentListOf(),
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
