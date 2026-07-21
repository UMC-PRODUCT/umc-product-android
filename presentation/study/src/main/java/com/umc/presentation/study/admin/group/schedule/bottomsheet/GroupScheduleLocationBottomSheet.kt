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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalNaverMapApi::class)
@Composable
fun GroupScheduleLocationBottomSheet(
    viewModel: GroupScheduleLocationViewModel = hiltViewModel(),
    onDismissRequest: () -> Unit,
    onLocationSelected: (LocationItem) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden }
    )

    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(state.selectedPlace.latitude, state.selectedPlace.longitude) {
        if (state.selectedPlace.latitude != 0.0 && !cameraPositionState.isMoving) {
            cameraPositionState.position = CameraPosition(
                LatLng(state.selectedPlace.latitude, state.selectedPlace.longitude),
                16.0
            )
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is GroupScheduleLocationEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }

                is GroupScheduleLocationEvent.MoveCameraTo -> {
                    cameraPositionState.animate(
                        CameraUpdate.scrollTo(LatLng(event.lat, event.lng))
                    )
                }

                is GroupScheduleLocationEvent.LocationConfirmed -> {
                    onLocationSelected(event.placeInfo)
                    onDismissRequest()
                }
            }
        }
    }

    LaunchedEffect(cameraPositionState.isMoving) {
        if (
            !cameraPositionState.isMoving &&
            cameraPositionState.cameraUpdateReason == CameraUpdateReason.GESTURE
        ) {
            val center = cameraPositionState.position.target
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
            BottomSheetDefaults.DragHandle(color = grey600())
        },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(760.dp)
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            GroupScheduleLocationHeaderAndSearchBar(
                searchQuery = state.searchQuery,
                onQueryChanged = viewModel::onQueryChanged,
                onSearchClick = {
                    viewModel.searchLocation(state.searchQuery)
                    focusManager.clearFocus()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            GroupScheduleLocationNaverMapContent(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                cameraPositionState = cameraPositionState
            )

            Spacer(modifier = Modifier.height(16.dp))

            GroupScheduleSelectedLocationCard(
                selectedPlace = state.selectedPlace,
                onConfirmClick = viewModel::confirmSelection
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (state.searchQuery.isEmpty()) {
                    GroupScheduleRecentSearchList(
                        recentSearchList = state.recentSearchList,
                        onItemClick = { recentText ->
                            viewModel.onQueryChanged(recentText)
                            viewModel.searchLocation(recentText)
                        }
                    )
                } else {
                    GroupScheduleSearchResultList(
                        searchResultList = state.searchResultList,
                        onItemClick = { placeItem ->
                            viewModel.selectSearchResult(placeItem)
                            focusManager.clearFocus()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GroupScheduleLocationHeaderAndSearchBar(
    searchQuery: String,
    onQueryChanged: (String) -> Unit,
    onSearchClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        UText(
            text = "장소를 선택하세요",
            style = UmcTypographyTokens.Title3Bold,
            color = grey800(),
            modifier = Modifier.padding(top = 8.dp)
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
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { onSearchClick() }
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            UButton(
                text = "검색",
                onClick = onSearchClick,
                modifier = Modifier
                    .width(64.dp)
                    .height(40.dp),
                backgroundColor = indigo500(),
                textColor = grey000(),
                textStyle = UmcTypographyTokens.Caption1Bold,
                cornerRadius = 8.dp
            )
        }
    }
}

@OptIn(ExperimentalNaverMapApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun GroupScheduleLocationNaverMapContent(
    modifier: Modifier = Modifier,
    cameraPositionState: CameraPositionState,
) {
    val localView = LocalView.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .pointerInteropFilter { motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN,
                    MotionEvent.ACTION_MOVE -> {
                        localView.parent?.requestDisallowInterceptTouchEvent(true)
                        false
                    }

                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_CANCEL -> {
                        localView.parent?.requestDisallowInterceptTouchEvent(false)
                        false
                    }

                    else -> false
                }
            }
    ) {
        NaverMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                locationTrackingMode = LocationTrackingMode.NoFollow
            ),
            uiSettings = MapUiSettings(
                isLocationButtonEnabled = true,
                isZoomControlEnabled = false
            )
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_location),
            contentDescription = "Center Fixed Marker",
            modifier = Modifier
                .size(36.dp)
                .align(Alignment.Center)
                .padding(bottom = 18.dp),
            tint = indigo500()
        )
    }
}

@Composable
fun GroupScheduleSelectedLocationCard(
    selectedPlace: LocationItem,
    onConfirmClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = grey100())
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
                    color = grey800()
                )

                Spacer(modifier = Modifier.height(2.dp))

                UText(
                    text = selectedPlace.address.ifBlank { "지도를 움직여 장소를 지정해 주세요." },
                    style = UmcTypographyTokens.Subheadline,
                    color = grey600()
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            UButton(
                text = "선택",
                onClick = onConfirmClick,
                modifier = Modifier
                    .width(64.dp)
                    .height(36.dp),
                backgroundColor = indigo500(),
                textColor = grey000(),
                textStyle = UmcTypographyTokens.Caption1Bold,
                cornerRadius = 8.dp
            )
        }
    }
}

@Composable
fun GroupScheduleRecentSearchList(
    recentSearchList: List<String>,
    onItemClick: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        UText(
            text = "최근 검색어",
            style = UmcTypographyTokens.Title3Bold,
            color = grey800(),
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
                        tint = grey400(),
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    UText(
                        text = recentText,
                        style = UmcTypographyTokens.Body,
                        color = grey800()
                    )
                }

                HorizontalDivider(color = grey200(), thickness = 0.5.dp)
            }
        }
    }
}

@Composable
fun GroupScheduleSearchResultList(
    searchResultList: List<LocationItem>,
    onItemClick: (LocationItem) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(searchResultList) { placeItem ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(placeItem) }
                    .padding(vertical = 12.dp, horizontal = 4.dp)
            ) {
                UText(
                    text = placeItem.title,
                    style = UmcTypographyTokens.BodyBold,
                    color = grey800()
                )

                Spacer(modifier = Modifier.height(2.dp))

                UText(
                    text = placeItem.address,
                    style = UmcTypographyTokens.Footnote,
                    color = grey600()
                )

                Spacer(modifier = Modifier.height(6.dp))

                HorizontalDivider(color = grey200(), thickness = 0.5.dp)
            }
        }
    }
}