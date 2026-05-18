package com.umc.presentation.home.schedule.dialog

import android.view.MotionEvent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.domain.model.home.LocationItem
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalView
import com.umc.component.R
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.compose.CameraPositionState
import com.naver.maps.map.compose.CameraUpdateReason
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.component.UTextField
import com.umc.component.theme.*
import kotlinx.coroutines.flow.collectLatest
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.rememberCameraPositionState
import com.naver.maps.map.compose.MapProperties
import com.naver.maps.map.compose.MapUiSettings
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.LocationTrackingMode

@OptIn(ExperimentalMaterial3Api::class, ExperimentalNaverMapApi::class)
@Composable
fun LocationSearchBottomSheet(
    viewModel: LocationSearchViewModel = hiltViewModel(),
    onDismissRequest: () -> Unit,
    onLocationSelected: (LocationItem) -> Unit
){

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden },
    )

    // 바텀시트 전체가 유연하게 스크롤될 수 있도록 스크롤 상태 정의
    val mainScrollState = rememberScrollState()

    // 공식 SDK 가이드 기준 카메라 상태 초기화
    val cameraPositionState = rememberCameraPositionState()

    //위치 변경에 따른 지도의 핀포인트 위치 변경
    LaunchedEffect(uiState.selectedPlace.latitude, uiState.selectedPlace.longitude) {
        if (uiState.selectedPlace.latitude != 0.0 && !cameraPositionState.isMoving) {
            cameraPositionState.position = CameraPosition(
                LatLng(uiState.selectedPlace.latitude, uiState.selectedPlace.longitude),
                16.0
            )
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is LocationSearchEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }

                //위도 경도로 지도를 이동
                is LocationSearchEvent.MoveCameraTo -> {
                    cameraPositionState.animate(
                        CameraUpdate.scrollTo(LatLng(event.lat, event.lng))
                    )
                }

                //위치 결정 시
                is LocationSearchEvent.LocationConfirmed -> {
                    onLocationSelected(event.placeInfo)
                    onDismissRequest()
                }
            }
        }
    }

    //제스처 이동 완료 시 지점 좌표 기반 주소 데이터 파싱
    /**사용자가 지도를 움직일 떄만 title이 변경되도로**/
    LaunchedEffect(cameraPositionState.isMoving) {
        // 카메라가 멈추고, 움직인 원인이 사용자의 제스처(손가락 드래그)일 때만 역지오코딩을 수행
        if (!cameraPositionState.isMoving && cameraPositionState.cameraUpdateReason == CameraUpdateReason.GESTURE) {
            val center = cameraPositionState.position.target
            if (center.latitude != 0.0) {
                viewModel.updateLocationFromCoordinates(center.latitude, center.longitude)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = neutral000(),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = neutral600())
        },

        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = Modifier
            .fillMaxHeight(0.9f)
    ){
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)

        ) {

            //1. 타이틀 헤더 및 검색창
            LocationHeaderAndSearchBar(
                searchQuery = uiState.searchQuery,
                onQueryChanged = viewModel::onQueryChanged,
                onSearchClick = {
                    //검색 클릭 시 카카오 API를 통한 장소 검색
                    viewModel.searchLocation(uiState.searchQuery)
                    focusManager.clearFocus()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            //2. 네이버 지도 뷰
            LocationNaverMapContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                cameraPositionState = cameraPositionState
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            //3. 지도가 멈춘 부분 정보 + 선택 버튼
            SelectedLocationCard(
                selectedPlace = uiState.selectedPlace,
                onConfirmClick = viewModel::confirmSelection
            )


            Spacer(modifier = Modifier.height(16.dp))

            //4. 분기 영역 : 검색어 입력 유무에 따른 하단 리스트 스위칭
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (uiState.searchQuery.isEmpty()) {
                    //검색하지 않을 경우: 최근 검색 기록 리스트 노출
                    RecentSearchList(
                        recentSearchList = uiState.recentSearchList,
                        onItemClick = { recentText ->
                            //검색창에 띄우고 텍스트 기반 -> 카카오 API로 상세 정보 획득
                            viewModel.onQueryChanged(recentText)
                            viewModel.searchLocation(recentText)
                        }
                    )
                } else {
                    //검색할 겨우: 카카오 API 검색 결과 리스트 노출
                    SearchResultList(
                        searchResultList = uiState.searchResultList,
                        onItemClick = { clickedItem ->
                            //검색한 결과를 그대로 띄우기
                            viewModel.selectSearchResult(clickedItem)
                            focusManager.clearFocus()
                        }
                    )
                }
            }
        }

    }


}

//1. 상단 제목 부분 및 검색 파트
@Composable
fun LocationHeaderAndSearchBar(
    searchQuery: String,
    onQueryChanged: (String) -> Unit,
    onSearchClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        UText(
            text = "장소를 선택하세요",
            style = UmcTypographyTokens.Title3Bold,
            color = neutral800(),
            modifier = Modifier
                .padding(top = 8.dp)
        )

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
                modifier = Modifier
                    .weight(1f),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearchClick() })
            )

            Spacer(modifier = Modifier.width(8.dp))

            UButton(
                text = "검색",
                onClick = onSearchClick,
                backgroundColor = primary500(),
                textColor = neutral000(),
                textStyle = UmcTypographyTokens.Caption1Bold,
                cornerRadius = 8.dp
            )
            
        }
    }
}

//지도 컴포저블
@OptIn(ExperimentalNaverMapApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun LocationNaverMapContent(
    modifier: Modifier = Modifier,
    cameraPositionState: CameraPositionState
) {

    //현재 컴포지블이 그려지는 있는 안드로이드 뷰
    val localView = LocalView.current

    /**TODO: ModalDialog랑 지도 상의 터치(스와이프) 이벤트 중복 어떻게 해결할까....**/
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .pointerInteropFilter { motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                        // 1. 부모(바텀시트)가 가로채는 것은 네이티브 레벨에서 락
                        localView.parent?.requestDisallowInterceptTouchEvent(true)
                        // 2. 기존 true에서 false로 변경!
                        // 이 이벤트를 소비하지 않고 아래에 있는 NaverMap 컴포저블로 고스란히 흘려보냅니다.
                        false
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        localView.parent?.requestDisallowInterceptTouchEvent(false)
                        false // 마찬가지로 떼는 이벤트도 지도에 넘겨줍니다.
                    }
                    else -> false
                }
            }
    ) {
        NaverMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(locationTrackingMode = LocationTrackingMode.NoFollow),
            uiSettings = MapUiSettings(isLocationButtonEnabled = true, isZoomControlEnabled = false)
        )

        // 가로축/세로축 정확한 정중앙 고정 핀 에셋
        Icon(
            painter = painterResource(id = R.drawable.ic_location),
            contentDescription = "Center Fixed Marker",
            modifier = Modifier
                .size(36.dp)
                .align(Alignment.Center)
                .padding(bottom = 18.dp), //핀 뾰족한 끝단 조준 보정 패딩
            tint = primary500()
        )
    }
}

//지도에 나오는 타겟 장소 정보창 및 확인 버튼
@Composable
fun SelectedLocationCard(
    selectedPlace: LocationItem,
    onConfirmClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = neutral100())
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                UText(
                    text = selectedPlace.title.ifBlank { "지정된 장소" },
                    style = UmcTypographyTokens.BodyBold,
                    color = neutral800()
                )
                Spacer(modifier = Modifier.height(2.dp))
                UText(
                    text = selectedPlace.address.ifBlank { "지도를 움직여 장소를 지정해 주세요." },
                    style = UmcTypographyTokens.Subheadline,
                    color = neutral600()
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            UButton(
                text = "선택",
                onClick = onConfirmClick,
                backgroundColor = primary500(),
                textColor = neutral000(),
                textStyle = UmcTypographyTokens.Caption1Bold,
                cornerRadius = 8.dp
            )
        }
    }
}

//최근 검색 결과 lazyColumn
@Composable
fun RecentSearchList(
    recentSearchList: List<String>,
    onItemClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        UText(
            text = "최근 검색어",
            style = UmcTypographyTokens.Title3Bold,
            color = neutral800(),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(recentSearchList) { recentText ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onItemClick(recentText) }
                        .padding(vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_history),
                        contentDescription = null,
                        tint = neutral400(),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    UText(text = recentText, style = UmcTypographyTokens.Body, color = neutral800())
                }
                HorizontalDivider(color = neutral200(), thickness = 0.5.dp)
            }
        }
    }
}

//카카오 API로 검색한 결과 lazycolumn
@Composable
fun SearchResultList(
    searchResultList: List<LocationItem>,
    onItemClick: (LocationItem) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(searchResultList) { placeItem ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(placeItem) }
                    .padding(vertical = 12.dp, horizontal = 4.dp)
            ) {
                UText(text = placeItem.title, style = UmcTypographyTokens.BodyBold, color = neutral800())
                Spacer(modifier = Modifier.height(2.dp))
                UText(text = placeItem.address, style = UmcTypographyTokens.Footnote, color = neutral600())
                Spacer(modifier = Modifier.padding(top = 6.dp))
                HorizontalDivider(color = neutral200(), thickness = 0.5.dp)
            }
        }
    }
}
