package com.umc.presentation.home.schedule.dialog

import androidx.lifecycle.viewModelScope
import com.umc.component.base.BaseViewModel
import com.umc.component.base.UiEvent
import com.umc.component.base.UiState
import com.umc.domain.model.home.LocationItem
import com.umc.domain.usecase.appDataStore.recent.GetRecentSearchPlaceUseCase
import com.umc.domain.usecase.appDataStore.recent.UpdateRecentSearchPlaceUseCase
import com.umc.domain.usecase.kakao.GetSearchLocationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import javax.inject.Inject


/**
 * 일정 등록의 장소 검색 바텀시트 상태. 카카오 키워드 검색과 최근 검색어를 다룬다.
 *
 * 네이버 지도 이동·역지오코딩으로 고르던 방식은 기획 변경으로 철회되어 지웠다.
 * 지도 기반 선택이 다시 필요하면 study 모듈의 GroupScheduleLocationBottomSheet 구현을 참고한다.
 */
@HiltViewModel
class LocationSearchViewModel @Inject constructor(
    private val getRecentSearchPlaceUseCase: GetRecentSearchPlaceUseCase,
    private val updateRecentSearchPlaceUseCase: UpdateRecentSearchPlaceUseCase,
    private val getSearchLocationUseCase: GetSearchLocationUseCase
)
    : BaseViewModel<LocationSearchUiState, LocationSearchEvent>(
        LocationSearchUiState()
    ){

    init{
        // 최근 장소 검색 기록 조회 실행
        loadRecentPlaces()
    }

    /**
     * DataStore에 보관되어 있는 최근 장소 검색 기록 목록을 비동기 조회하는 메서드
     */
    private fun loadRecentPlaces() {
        viewModelScope.launch {
            getRecentSearchPlaceUseCase().collect { places ->
                updateState { copy(recentSearchList = places.toImmutableList()) }
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
                            searchResultList = locationList.toImmutableList(),
                            isSearching = false
                        )
                    }
                    if (locationList.isEmpty()) {
                        emitEvent(LocationSearchEvent.ShowSearchEmptyToast)
                    } else {
                        //최근 검색어 저장 트리거
                        saveRecentPlace(query)
                    }
                },
                errorCallback = {
                    updateState { copy(isSearching = false) }
                    emitEvent(LocationSearchEvent.ShowSearchFailedToast)
                }
            )
        }
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
}

data class LocationSearchUiState(
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val recentSearchList: ImmutableList<String> = persistentListOf(),
    val searchResultList: ImmutableList<LocationItem> = persistentListOf()
) : UiState

/** 토스트 문구와 모양(아이콘)은 화면이 정한다. ViewModel 은 무슨 일이 있었는지만 알린다. */
sealed interface LocationSearchEvent : UiEvent {
    /** 검색은 성공했지만 결과가 없을 때 */
    object ShowSearchEmptyToast : LocationSearchEvent
    /** 장소 검색 요청이 실패했을 때 */
    object ShowSearchFailedToast : LocationSearchEvent
}
