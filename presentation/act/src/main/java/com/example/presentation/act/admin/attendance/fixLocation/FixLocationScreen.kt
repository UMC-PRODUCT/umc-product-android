package com.example.presentation.act.admin.attendance.fixLocation

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
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.component.UTextField
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTheme
import com.umc.component.theme.UmcTypographyTokens.Body
import com.umc.component.theme.UmcTypographyTokens.HeadlineBold
import com.umc.component.theme.UmcTypographyTokens.Subheadline
import com.umc.component.theme.UmcTypographyTokens.Title3Bold
import com.umc.component.theme.neutral000
import com.umc.component.theme.neutral100
import com.umc.component.theme.neutral400
import com.umc.component.theme.neutral600
import com.umc.component.theme.neutral800

@Composable
fun FixLocationRoute(
    scheduleId: Long = 0L,
    viewModel: FixLocationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FixLocationScreen(
        uiState = uiState,
        onSearchKeywordChange = viewModel::onSearchKeywordChanged,
        onSearchClick = viewModel::searchLocation,
        onLocationClick = { location ->
            viewModel.selectLocation(location)
            viewModel.updateLocation(scheduleId)
        }
    )
}


@Composable
fun FixLocationScreen(
    uiState: FixLocationUiState = FixLocationUiState(),
    onSearchKeywordChange: (String) -> Unit = {},
    onSearchClick: () -> Unit = {},
    onLocationClick: (com.umc.domain.model.home.LocationItem) -> Unit = {},
) {
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
            .background(neutral000())
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
                color = neutral800()
            )
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(neutral100()),
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
            color = neutral600(),
        )

        Spacer(Modifier.height(24.dp))

        UTextField(
            value = uiState.searchKeyword,
            onValueChange = onSearchKeywordChange,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(52.dp),
            placeholder = AppStrings.ACT_LOCATION_SEARCH_PLACEHOLDER,
            placeholderColor = neutral400(),
            textColor = neutral800(),
            textStyle = Body,
            backgroundColor = neutral100(),
            strokeColor = neutral100(),
            focusStrokeColor = neutral100(),
            prevIcon = painterResource(R.drawable.ic_search),
            prevIconTint = neutral400()
        )

        Spacer(Modifier.height(8.dp))

        UText(
            text = "검색",
            style = Body,
            color = neutral800(),
            modifier = Modifier
                .align(Alignment.End)
                .clickable(onClick = onSearchClick)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        )

        Spacer(Modifier.height(40.dp))

        UText(
            text = AppStrings.COMMUNITY_SEARCH_RECENT,
            style = HeadlineBold,
            color = neutral800()
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
                        color = neutral600(),
                        modifier = Modifier.clickable { onLocationClick(location) }
                    )
                    Spacer(Modifier.height(16.dp))
                }
            } else {
                items(uiState.recentAddresses.ifEmpty { recentAddressTestData }) { address ->
                    UText(
                        text = address,
                        style = Body,
                        color = neutral600(),
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }
        }

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
                .background(neutral600())
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
