package com.umc.presentation.act.admin.attendance.fixlocation

import android.Manifest
import android.content.pm.PackageManager
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
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
import com.naver.maps.map.compose.rememberFusedLocationSource
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.component.UTextField
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTheme
import com.umc.component.theme.UmcTypographyTokens.Body
import com.umc.component.theme.UmcTypographyTokens.CalloutBold
import com.umc.component.theme.UmcTypographyTokens.Footnote
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
import com.umc.component.base.CollectUiEvents

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun FixLocationRoute(
    scheduleId: Long = 0L,
    viewModel: FixLocationViewModel = hiltViewModel(),
    onUpdateSuccess: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            viewModel.moveToCurrentLocation()
        } else {
            Toast.makeText(
                context,
                "현재 위치를 확인하려면 위치 권한이 필요합니다.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition(LatLng(37.3943, 126.6388), 16.0)
    }

    CollectUiEvents(viewModel.uiEvent) { event ->
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

    LaunchedEffect(scheduleId) {
        viewModel.loadScheduleLocation(scheduleId)
    }

    FixLocationScreen(
        uiState = uiState,
        cameraPositionState = cameraPositionState,
        onSearchKeywordChange = viewModel::onSearchKeywordChanged,
        onSearchClick = viewModel::searchLocation,
        onRecentAddressClick = viewModel::searchRecentLocation,
        onLocationClick = { location ->
            viewModel.updateLocation(scheduleId, location)
        },
        onCurrentLocationClick = {
            val hasLocationPermission =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

            if (hasLocationPermission) {
                viewModel.moveToCurrentLocation()
            } else {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    )
                )
            }
        },
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
    onRecentAddressClick: (String) -> Unit = {},
    onLocationClick: (com.umc.domain.model.home.LocationItem) -> Unit = {},
    onCurrentLocationClick: () -> Unit = {},
    onConfirmClick: () -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    var isSearchFocused by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(550.dp)
            .imePadding()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(grey000())
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        DragHeader()

        if (isSearchFocused) {
            UText(
                text = AppStrings.ACT_LOCATION_SELECT_TITLE,
                style = Title3Bold,
                color = grey800()
            )
            Spacer(Modifier.height(20.dp))
        } else {
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
                        .background(grey100())
                        .clickable(onClick = onCurrentLocationClick),
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
        }

        UTextField(
            value = uiState.searchKeyword,
            onValueChange = onSearchKeywordChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(52.dp)
                .onFocusChanged { focusState ->
                    isSearchFocused = focusState.isFocused
                },
            placeholder = AppStrings.ACT_LOCATION_SEARCH_PLACEHOLDER,
            placeholderColor = grey400(),
            textColor = grey800(),
            textStyle = Body,
            backgroundColor = grey100(),
            focusBackgroundColor = grey000(),
            strokeColor = grey100(),
            focusStrokeColor = grey900(),
            prevIcon = if (isSearchFocused) null else painterResource(R.drawable.ic_search),
            prevIconTint = grey400(),
            nextIcon = if (isSearchFocused && uiState.searchKeyword.isNotBlank()) {
                painterResource(R.drawable.ic_clear_circle)
            } else {
                null
            },
            nextIconSize = 16.dp,
            onClickNextIcon = {
                onSearchKeywordChange("")
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    onSearchClick()
                }
            )
        )

        Spacer(Modifier.height(40.dp))

        if (!isSearchFocused) {
            UText(
                text = AppStrings.COMMUNITY_SEARCH_RECENT,
                style = HeadlineBold,
                color = grey800()
            )

            Spacer(Modifier.height(16.dp))
        }

        if (uiState.searchResults.isNotEmpty()) {
            uiState.searchResults.forEach { location ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        UText(
                            text = location.title,
                            style = CalloutBold,
                            color = grey800(),
                        )
                        Spacer(Modifier.height(2.dp))
                        UText(
                            text = location.address,
                            style = Footnote,
                            color = grey400(),
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    UButton(
                        text = AppStrings.ACT_LOCATION_SELECT_BUTTON,
                        onClick = {
                            onLocationClick(location)
                            focusManager.clearFocus()
                        },
                        backgroundColor = grey100(),
                        textColor = grey600(),
                        textStyle = Footnote,
                        cornerRadius = 8.dp,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 7.dp),
                    )
                }
            }
        } else if (!isSearchFocused) {
            uiState.recentAddresses.forEach { address ->
                UText(
                    text = address,
                    style = Body,
                    color = grey600(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onRecentAddressClick(address)
                            focusManager.clearFocus()
                        }
                )
                Spacer(Modifier.height(16.dp))
            }
        }

        Spacer(Modifier.height(16.dp))
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
