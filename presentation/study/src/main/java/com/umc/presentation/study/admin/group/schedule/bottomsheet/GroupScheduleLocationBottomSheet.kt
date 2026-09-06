package com.umc.presentation.study.admin.group.schedule.bottomsheet

import android.view.MotionEvent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.compose.*
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.component.UTextField
import com.umc.component.theme.*
import com.umc.domain.model.home.LocationItem
import kotlinx.coroutines.flow.collectLatest
import com.umc.component.base.CollectUiEvents

/**
 * 스터디 일정 등록 시 장소를 선택하는 BottomSheet
 *
 * 주요 기능
 * - 장소명 또는 주소 검색
 * - 네이버 지도 표시
 * - 지도 이동을 통한 직접 위치 선택
 * - 검색 결과 선택 시 해당 위치로 지도 이동
 * - 최근 장소 검색어 표시
 * - 선택한 장소를 상위 화면에 전달
 */
@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalNaverMapApi::class
)
@Composable
fun GroupScheduleLocationBottomSheet(
    viewModel: GroupScheduleLocationViewModel = hiltViewModel(),
    onDismissRequest: () -> Unit,
    onLocationSelected: (LocationItem) -> Unit,
) {
    // ViewModel의 현재 장소 선택 상태 구독
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Toast 출력에 사용할 Context
    val context = LocalContext.current

    // 장소 검색 후 키보드 포커스 해제에 사용
    val focusManager = LocalFocusManager.current

    /**
     * BottomSheet 상태
     *
     * 장소 선택 화면에서는 지도를 직접 드래그할 수 있으므로
     * Sheet가 중간 단계로 접히지 않도록 설정합니다.
     */
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = {
            it != SheetValue.Hidden
        }
    )

    /**
     * 네이버 지도 카메라 위치 상태
     *
     * 현재 선택된 장소와 지도 화면의 중심 좌표를 연결합니다.
     */
    val cameraPositionState = rememberCameraPositionState()

    /**
     * 선택된 장소 좌표가 변경되면
     * 지도 카메라를 해당 위치로 이동합니다.
     */
    LaunchedEffect(
        state.selectedPlace.latitude,
        state.selectedPlace.longitude
    ) {
        if (
            state.selectedPlace.latitude != 0.0 &&
            !cameraPositionState.isMoving
        ) {
            cameraPositionState.position = CameraPosition(
                LatLng(
                    state.selectedPlace.latitude,
                    state.selectedPlace.longitude
                ),
                16.0
            )
        }
    }

    /**
     * ViewModel에서 발생하는 일회성 이벤트 처리
     *
     * - Toast 출력
     * - 지도 카메라 이동
     * - 장소 선택 완료
     */
    CollectUiEvents(viewModel.uiEvent) { event ->
        when (event) {
            /**
             * 검색 실패 등의 안내 메시지 표시
             */
            is GroupScheduleLocationEvent.ShowToast -> {
                Toast.makeText(
                    context,
                    event.message,
                    Toast.LENGTH_SHORT
                ).show()
            }

            /**
             * 장소 검색 결과를 선택하면
             * 해당 장소 좌표로 지도 카메라 이동
             */
            is GroupScheduleLocationEvent.MoveCameraTo -> {
                cameraPositionState.animate(
                    CameraUpdate.scrollTo(
                        LatLng(
                            event.lat,
                            event.lng
                        )
                    )
                )
            }

            /**
             * 최종 장소 선택 완료
             *
             * 선택된 장소 정보를 상위 화면에 전달하고
             * BottomSheet를 닫습니다.
             */
            is GroupScheduleLocationEvent.LocationConfirmed -> {
                onLocationSelected(
                    event.placeInfo
                )

                onDismissRequest()
            }
        }
    }

    /**
     * 사용자가 직접 지도를 움직인 후 멈춘 경우
     * 지도 중심 좌표를 ViewModel에 전달합니다.
     *
     * ViewModel에서는 해당 좌표를 Geocoder로 변환하여
     * 실제 주소 및 장소명을 가져옵니다.
     */
    LaunchedEffect(
        cameraPositionState.isMoving
    ) {
        if (
            !cameraPositionState.isMoving &&
            cameraPositionState.cameraUpdateReason ==
            CameraUpdateReason.GESTURE
        ) {
            val center =
                cameraPositionState.position.target

            if (center.latitude != 0.0) {
                viewModel.updateLocationFromCoordinates(
                    center.latitude,
                    center.longitude
                )
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = grey000(),
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = grey600()
            )
        },
        shape = RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 28.dp
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(760.dp)
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            /**
             * 장소 검색 영역
             *
             * 장소명 또는 주소를 입력하고
             * 검색 버튼 또는 키보드 Search 버튼으로 검색합니다.
             */
            GroupScheduleLocationHeaderAndSearchBar(
                searchQuery = state.searchQuery,
                onQueryChanged = viewModel::onQueryChanged,
                onSearchClick = {
                    viewModel.searchLocation(
                        state.searchQuery
                    )

                    focusManager.clearFocus()
                }
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            /**
             * 네이버 지도 영역
             *
             * 지도 중심에는 고정 마커를 표시하며
             * 지도를 움직여 직접 장소를 지정할 수 있습니다.
             */
            GroupScheduleLocationNaverMapContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                cameraPositionState =
                    cameraPositionState
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            /**
             * 현재 선택된 장소 정보
             *
             * 장소명과 주소를 확인하고
             * 선택 버튼을 눌러 최종 확정합니다.
             */
            GroupScheduleSelectedLocationCard(
                selectedPlace =
                    state.selectedPlace,
                onConfirmClick =
                    viewModel::confirmSelection
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            /**
             * 검색어 상태에 따른 하단 목록
             *
             * 검색어가 없을 때
             * -> 최근 검색어
             *
             * 검색어가 있을 때
             * -> 장소 검색 결과
             */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (state.searchQuery.isEmpty()) {
                    GroupScheduleRecentSearchList(
                        recentSearchList =
                            state.recentSearchList,

                        onItemClick = { recentText ->
                            // 최근 검색어를 검색창에 적용
                            viewModel.onQueryChanged(
                                recentText
                            )

                            // 해당 검색어로 장소 검색
                            viewModel.searchLocation(
                                recentText
                            )
                        }
                    )
                } else {
                    GroupScheduleSearchResultList(
                        searchResultList =
                            state.searchResultList,

                        onItemClick = { placeItem ->
                            // 검색 결과 장소 선택
                            viewModel.selectSearchResult(
                                placeItem
                            )

                            // 키보드 포커스 해제
                            focusManager.clearFocus()
                        }
                    )
                }
            }
        }
    }
}

/**
 * 장소 선택 BottomSheet 상단의
 * 제목 및 장소 검색 영역
 */
@Composable
fun GroupScheduleLocationHeaderAndSearchBar(
    searchQuery: String,
    onQueryChanged: (String) -> Unit,
    onSearchClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // BottomSheet 제목
        UText(
            text = "장소를 선택하세요",
            style = UmcTypographyTokens.Title3Bold,
            color = grey800(),
            modifier = Modifier.padding(
                top = 8.dp
            )
        )

        // 장소 검색창 및 검색 버튼
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UTextField(
                value = searchQuery,
                onValueChange = onQueryChanged,
                placeholder = "장소 또는 주소를 입력하세요",
                modifier = Modifier.weight(1f),

                // 키보드의 검색 버튼 사용
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Search
                ),

                keyboardActions = KeyboardActions(
                    onSearch = {
                        onSearchClick()
                    }
                )
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            // 장소 검색 실행
            UButton(
                text = "검색",
                onClick = onSearchClick,
                modifier = Modifier
                    .width(64.dp)
                    .height(40.dp),
                backgroundColor = indigo500(),
                textColor = grey000(),
                textStyle =
                    UmcTypographyTokens.Caption1Bold,
                cornerRadius = 8.dp
            )
        }
    }
}

/**
 * 장소 선택에 사용하는 네이버 지도
 *
 * 사용자는 지도를 직접 움직여 위치를 선택할 수 있으며,
 * 지도 중심에는 고정된 위치 마커를 표시합니다.
 *
 * BottomSheet 내부의 지도 드래그가
 * BottomSheet 드래그 동작과 충돌하지 않도록
 * 터치 이벤트를 제어합니다.
 */
@OptIn(
    ExperimentalNaverMapApi::class,
    androidx.compose.ui.ExperimentalComposeUiApi::class
)
@Composable
fun GroupScheduleLocationNaverMapContent(
    modifier: Modifier = Modifier,
    cameraPositionState: CameraPositionState,
) {
    val localView = LocalView.current

    Box(
        modifier = modifier
            .clip(
                RoundedCornerShape(12.dp)
            )
            .pointerInteropFilter { motionEvent ->
                /**
                 * 지도 터치 중에는 부모인 BottomSheet가
                 * 드래그 이벤트를 가로채지 못하도록 처리합니다.
                 */
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN,
                    MotionEvent.ACTION_MOVE -> {
                        localView.parent
                            ?.requestDisallowInterceptTouchEvent(
                                true
                            )

                        false
                    }

                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_CANCEL -> {
                        localView.parent
                            ?.requestDisallowInterceptTouchEvent(
                                false
                            )

                        false
                    }

                    else -> false
                }
            }
    ) {
        /**
         * 네이버 지도
         */
        NaverMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState =
                cameraPositionState,
            properties = MapProperties(
                locationTrackingMode =
                    LocationTrackingMode.NoFollow
            ),
            uiSettings = MapUiSettings(
                // 현재 위치 버튼 표시
                isLocationButtonEnabled = true,

                // 확대/축소 기본 버튼 숨김
                isZoomControlEnabled = false
            )
        )

        /**
         * 지도 중심 고정 마커
         *
         * 실제 지도 Marker가 아니라 화면 중앙에
         * 고정된 Icon을 표시하는 방식입니다.
         */
        Icon(
            painter = painterResource(
                id = R.drawable.ic_location
            ),
            contentDescription =
                "Center Fixed Marker",
            modifier = Modifier
                .size(36.dp)
                .align(Alignment.Center)
                .padding(bottom = 18.dp),
            tint = indigo500()
        )
    }
}

/**
 * 현재 선택된 장소 정보를 보여주는 카드
 *
 * 장소명과 주소를 표시하고
 * 선택 버튼을 눌러 해당 장소를 최종 확정합니다.
 */
@Composable
fun GroupScheduleSelectedLocationCard(
    selectedPlace: LocationItem,
    onConfirmClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = grey100()
        )
    ) {
        Row(
            modifier = Modifier.padding(
                14.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement =
                Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // 선택된 장소명
                UText(
                    text = selectedPlace.title.ifBlank {
                        "지정된 장소"
                    },
                    style =
                        UmcTypographyTokens.BodyBold,
                    color = grey800()
                )

                Spacer(
                    modifier = Modifier.height(2.dp)
                )

                // 선택된 장소 주소
                UText(
                    text = selectedPlace.address.ifBlank {
                        "지도를 움직여 장소를 지정해 주세요."
                    },
                    style =
                        UmcTypographyTokens.Subheadline,
                    color = grey600()
                )
            }

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            // 현재 장소 최종 선택
            UButton(
                text = "선택",
                onClick = onConfirmClick,
                modifier = Modifier
                    .width(64.dp)
                    .height(36.dp),
                backgroundColor = indigo500(),
                textColor = grey000(),
                textStyle =
                    UmcTypographyTokens.Caption1Bold,
                cornerRadius = 8.dp
            )
        }
    }
}

/**
 * 최근 장소 검색어 목록
 *
 * 저장된 최근 검색어를 표시하며,
 * 항목을 선택하면 해당 검색어로 다시 장소를 검색합니다.
 */
@Composable
fun GroupScheduleRecentSearchList(
    recentSearchList: ImmutableList<String>,
    onItemClick: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        UText(
            text = "최근 검색어",
            style = UmcTypographyTokens.Title3Bold,
            color = grey800(),
            modifier = Modifier.padding(
                bottom = 12.dp
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(
                recentSearchList
            ) { recentText ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onItemClick(recentText)
                        }
                        .padding(vertical = 14.dp),
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {
                    // 최근 검색 기록 아이콘
                    Icon(
                        painter = painterResource(
                            id = R.drawable.ic_history
                        ),
                        contentDescription = null,
                        tint = grey400(),
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(
                        modifier = Modifier.width(12.dp)
                    )

                    UText(
                        text = recentText,
                        style =
                            UmcTypographyTokens.Body,
                        color = grey800()
                    )
                }

                // 검색어 항목 구분선
                HorizontalDivider(
                    color = grey200(),
                    thickness = 0.5.dp
                )
            }
        }
    }
}

/**
 * 장소 검색 API 결과 목록
 *
 * 각 장소의 이름과 주소를 표시하며,
 * 항목을 선택하면 해당 장소가 현재 선택 위치로 설정됩니다.
 */
@Composable
fun GroupScheduleSearchResultList(
    searchResultList: ImmutableList<LocationItem>,
    onItemClick: (LocationItem) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            searchResultList
        ) { placeItem ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onItemClick(placeItem)
                    }
                    .padding(
                        vertical = 12.dp,
                        horizontal = 4.dp
                    )
            ) {
                // 장소명
                UText(
                    text = placeItem.title,
                    style =
                        UmcTypographyTokens.BodyBold,
                    color = grey800()
                )

                Spacer(
                    modifier = Modifier.height(2.dp)
                )

                // 장소 주소
                UText(
                    text = placeItem.address,
                    style =
                        UmcTypographyTokens.Footnote,
                    color = grey600()
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                // 장소 검색 결과 구분선
                HorizontalDivider(
                    color = grey200(),
                    thickness = 0.5.dp
                )
            }
        }
    }
}