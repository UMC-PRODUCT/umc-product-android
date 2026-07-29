package com.umc.presentation.act.admin.attendance.fixlocation

import android.view.MotionEvent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.compose.CameraPositionState
import com.naver.maps.map.compose.CameraUpdateReason
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.LocationTrackingMode
import com.naver.maps.map.compose.MapProperties
import com.naver.maps.map.compose.MapUiSettings
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.rememberCameraPositionState
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.component.UTextField
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTheme
import com.umc.component.theme.UmcTypographyTokens.Body
import com.umc.component.theme.UmcTypographyTokens.HeadlineBold
import com.umc.component.theme.UmcTypographyTokens.Subheadline
import com.umc.component.theme.UmcTypographyTokens.Title3Bold
import com.umc.component.theme.grey000
import com.umc.component.theme.grey100
import com.umc.component.theme.grey400
import com.umc.component.theme.grey600
import com.umc.component.theme.grey800
import com.umc.component.theme.grey900
import com.umc.component.theme.indigo500
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun FixLocationRoute(
    scheduleId: Long = 0L,
    viewModel: FixLocationViewModel = hiltViewModel(),
    onUpdateSuccess: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(LatLng(37.3943, 126.6388), 16.0)
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is FixLocationEvent.MoveCameraTo -> {
                    cameraPositionState.animate(
                        CameraUpdate.scrollTo(LatLng(event.latitude, event.longitude))
                    )
                }
                is FixLocationEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                FixLocationEvent.UpdateSuccess -> onUpdateSuccess()
            }
        }
    }

    FixLocationScreen(
        uiState = uiState,
        cameraPositionState = cameraPositionState,
        onSearchKeywordChange = viewModel::onSearchKeywordChanged,
        onSearchClick = viewModel::searchLocation,
        onLocationClick = { location -> viewModel.selectLocation(location) },
        onMapCenterChanged = viewModel::updateLocationFromCoordinates,
        onConfirmClick = { viewModel.updateLocation(scheduleId) }
    )
}


@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun FixLocationScreen(
    uiState: FixLocationUiState = FixLocationUiState(),
    cameraPositionState: CameraPositionState = rememberCameraPositionState {
        position = CameraPosition(LatLng(37.3943, 126.6388), 16.0)
    },
    onSearchKeywordChange: (String) -> Unit = {},
    onSearchClick: () -> Unit = {},
    onLocationClick: (com.umc.domain.model.home.LocationItem) -> Unit = {},
    onMapCenterChanged: (Double, Double) -> Unit = { _, _ -> },
    onConfirmClick: () -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    val recentAddressTestData = listOf(
        "서울특별시 강남구 테헤란로 427",
        "서울특별시 송파구 올림픽로 300",
        "서울특별시 중구 세종대로 110",
        "경기도 성남시 분당구 판교역로 235",
        "인천광역시 연수구 센트럴로 123"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(700.dp)
            .imePadding()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(grey000())
            .padding(horizontal = 16.dp)
    ) {
        DragHeader()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            UText(
                text = AppStrings.ACT_LOCATION_TITLE,
                style = Title3Bold,
                color = grey800()
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(grey100()),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_gps),
                    modifier = Modifier.size(24.dp),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        UText(
            text = AppStrings.ACT_LOCATION_UPDATE,
            style = Subheadline,
            color = grey600(),
        )

        Spacer(Modifier.height(24.dp))

        FixLocationNaverMap(
            cameraPositionState = cameraPositionState,
            onMapCenterChanged = onMapCenterChanged
        )

        Spacer(Modifier.height(16.dp))

        uiState.selectedLocation?.let { location ->
            UText(
                text = location.address.ifBlank { location.title },
                style = Body,
                color = grey800()
            )

            Spacer(Modifier.height(12.dp))

            UButton(
                text = "이 위치로 변경",
                onClick = onConfirmClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                backgroundColor = indigo500(),
                textColor = grey000()
            )

            Spacer(Modifier.height(16.dp))
        }

        UTextField(
            value = uiState.searchKeyword,
            onValueChange = onSearchKeywordChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(52.dp),
            placeholder = AppStrings.ACT_LOCATION_SEARCH_PLACEHOLDER,
            placeholderColor = grey400(),
            textColor = grey800(),
            textStyle = Body,
            backgroundColor = grey100(),
            strokeColor = grey100(),
            focusStrokeColor = grey900(),
            prevIcon = painterResource(R.drawable.ic_search),
            prevIconTint = grey400(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    onSearchClick()
                    focusManager.clearFocus()
                }
            )
        )

        Spacer(Modifier.height(24.dp))

        UText(
            text = AppStrings.COMMUNITY_SEARCH_RECENT,
            style = HeadlineBold,
            color = grey800()
        )

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            if (uiState.searchResults.isNotEmpty()) {
                items(uiState.searchResults) { location ->
                    UText(
                        text = location.address.ifBlank { location.title },
                        style = Body,
                        color = grey600(),
                        modifier = Modifier.clickable {
                            onLocationClick(location)
                            focusManager.clearFocus()
                        }
                    )
                    Spacer(Modifier.height(16.dp))
                }
            } else {
                items(uiState.recentAddresses.ifEmpty { recentAddressTestData }) { address ->
                    UText(
                        text = address,
                        style = Body,
                        color = grey600(),
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }
        }

    }
}

@OptIn(ExperimentalNaverMapApi::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
private fun FixLocationNaverMap(
    cameraPositionState: CameraPositionState,
    onMapCenterChanged: (Double, Double) -> Unit,
) {
    val localView = LocalView.current

    LaunchedEffect(cameraPositionState.isMoving) {
        if (
            !cameraPositionState.isMoving &&
            cameraPositionState.cameraUpdateReason == CameraUpdateReason.GESTURE
        ) {
            val center = cameraPositionState.position.target
            onMapCenterChanged(center.latitude, center.longitude)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
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
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
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
            painter = painterResource(R.drawable.ic_location),
            contentDescription = "선택할 출석 위치",
            modifier = Modifier
                .size(36.dp)
                .align(Alignment.Center)
                .padding(bottom = 18.dp),
            tint = indigo500()
        )
    }
}

@Composable
private fun DragHeader(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(vertical = 16.dp)
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(32.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(grey600())
        )
    }
}

@Preview(showBackground = false)
@Composable
private fun FixLocationPreview() {
    UmcTheme(darkTheme = false) {
        FixLocationScreen()
    }
}
