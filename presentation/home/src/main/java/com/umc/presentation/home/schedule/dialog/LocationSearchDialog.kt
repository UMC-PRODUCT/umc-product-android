package com.umc.presentation.home.schedule.dialog

import com.umc.component.theme.AppStrings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.component.UTextField
import com.umc.component.component.UToastData
import com.umc.component.component.UToastHost
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey50
import com.umc.component.theme.grey100
import com.umc.component.theme.grey200
import com.umc.component.theme.grey400
import com.umc.component.theme.grey500
import com.umc.component.theme.grey600
import com.umc.component.theme.grey700
import com.umc.component.theme.grey800
import com.umc.component.theme.grey900
import com.umc.component.theme.grey950
import com.umc.domain.model.home.LocationItem
import kotlinx.collections.immutable.ImmutableList
import kotlin.math.roundToInt

/**
 * 일정 생성 및 수정 과정에서 장소(위치) 검색 및 선택을 담당하는 바텀시트 다이얼로그 컴포저블
 *
 * 카카오 장소 검색 API 기반으로 키워드에 따른 장소 목록을 조회하고,
 * 선택된 장소(LocationItem: 장소명, 주소, 위경도) 정보를 상위 스케줄 작성 스크린으로 전달합니다.
 * 상태와 ViewModel([LocationSearchViewModel]) 연결은 호출하는 화면(ScheduleAddRoute)이 맡는다.
 *
 * 주요 동작 흐름:
 * 1. 입력 키워드가 비어있는 초기 상태에서는 최근 검색어 목록(RecentSearchList)을 표시합니다.
 * 2. 검색창에 장소명을 입력하고 검색 실행 시 카카오 장소 검색 API 결과를 SearchResultList로 분기하여 노출합니다.
 * 3. 최근 검색어 또는 검색 결과 리스트 항목 클릭 시 onLocationSelected 콜백을 발생시켜 장소 정보를 넘겨주고 바텀시트를 닫습니다.
 *
 * @param toastData 시트 안에 띄울 토스트. 시트는 별도 창이라 화면의 토스트가 시트 아래에 가려진다
 * @param onSearch 검색 실행 콜백. 검색어를 넘긴다
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSearchBottomSheet(
    searchQuery: String,
    recentSearchList: ImmutableList<String>,
    searchResultList: ImmutableList<LocationItem>,
    toastData: UToastData?,
    onToastDismiss: () -> Unit,
    onQueryChanged: (String) -> Unit,
    onSearch: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onLocationSelected: (LocationItem) -> Unit
){

    val focusManager = LocalFocusManager.current
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false,
    )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = grey50(),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = grey600())
        },

        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),

    ){
        // 내용이 시트 높이를 꽉 채워서 시트는 절반만 펼쳐진 채로 열린다. 그때는 내용 아래쪽이 화면 밖에 있으므로
        // 화면 밖으로 나간 만큼 토스트를 올려, 시스템 바·키보드 바로 위(보이는 아래 끝)에 띄운다.
        // 인셋은 시트 창 기준이어야 하므로 시트 내용 안에서 읽는다
        val density = LocalDensity.current
        val safeDrawingInsets = WindowInsets.safeDrawing
        var hiddenBottomPx by remember { mutableIntStateOf(0) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    val visibleBottom = coordinates.findRootCoordinates().size.height -
                        safeDrawingInsets.getBottom(density)
                    val contentBottom = coordinates.positionInRoot().y + coordinates.size.height
                    hiddenBottomPx = (contentBottom - visibleBottom).roundToInt().coerceAtLeast(0)
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp)

            ) {

                //1. 타이틀 헤더 및 검색창
                LocationHeaderAndSearchBar(
                    searchQuery = searchQuery,
                    onQueryChanged = onQueryChanged,
                    onSearchClick = {
                        //검색 클릭 시 카카오 API를 통한 장소 검색
                        onSearch(searchQuery)
                        focusManager.clearFocus()
                    },
                    onClearSearch = { onQueryChanged("") }
                )

                Spacer(modifier = Modifier.height(40.dp))

                //2. 분기 영역 : 검색어 입력 유무에 따른 하단 리스트 스위칭
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (searchQuery.isEmpty()) {
                        //검색하지 않을 경우: 최근 검색 기록 리스트 노출
                        RecentSearchList(
                            recentSearchList = recentSearchList,
                            onItemClick = { recentText ->
                                //검색창에 띄우고 텍스트 기반 -> 카카오 API로 상세 정보 획득
                                onQueryChanged(recentText)
                                onSearch(recentText)
                            }
                        )
                    } else {
                        //검색할 겨우: 카카오 API 검색 결과 리스트 노출
                        SearchResultList(
                            searchResultList = searchResultList,
                            onItemClick = { clickedItem ->
                                onLocationSelected(clickedItem)
                                onDismissRequest()
                            }
                        )
                    }
                }
            }

            // 시트가 키보드·내비게이션 바 인셋을 이미 내용 여백으로 쓰므로 여기서는 화면 밖으로 나간 만큼만 올린다
            UToastHost(
                data = toastData,
                onDismiss = onToastDismiss,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset { IntOffset(0, -hiddenBottomPx) }
                    .padding(horizontal = 16.dp, vertical = 24.dp),
            )
        }

    }


}

/**
 * 장소 검색 바텀시트의 타이틀 및 키워드 입력 텍스트 필드를 구성하는 컴포저블
 */
@Composable
fun LocationHeaderAndSearchBar(
    searchQuery: String,
    onQueryChanged: (String) -> Unit,
    onClearSearch: () -> Unit,
    onSearchClick: () -> Unit
) {


    Column(modifier = Modifier.fillMaxWidth()) {
        UText(
            text = AppStrings.HOME_LOCATION_SELECT_TITLE,
            style = UmcTypographyTokens.Title3Bold,
            color = grey800(),
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
                placeholder = AppStrings.HOME_PLAN_ADD_PLAN_LOCATION_PLACEHOLDER,
                modifier = Modifier
                    .weight(1f),
                backgroundColor = grey100(),
                strokeColor = Color.Transparent,
                focusStrokeColor = grey900(),
                prevIcon = painterResource(id = R.drawable.ic_search),
                prevIconTint = grey500(),
                prevIconSize = 24.dp,
                nextIcon = if (searchQuery.isNotEmpty()) painterResource(id = R.drawable.ic_delete) else null,
                nextIconTint = grey500(),
                nextIconSize = 24.dp,
                onClickNextIcon = onClearSearch,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearchClick() })
            )


        }
    }
}

/**
 * 사용자의 최근 장소 검색어 목록을 노출하는 컴포저블
 */
@Composable
fun RecentSearchList(
    recentSearchList: ImmutableList<String>,
    onItemClick: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        UText(
            text = AppStrings.HOME_LOCATION_RECENT_SEARCH,
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
                    UText(text = recentText, style = UmcTypographyTokens.Body, color = grey800())
                }
                HorizontalDivider(color = grey200(), thickness = 0.5.dp)
            }
        }
    }
}

/**
 * 카카오 장소 검색 API를 통해 수신된 장소 결과 목록을 노출하는 컴포저블
 */
@Composable
fun SearchResultList(
    searchResultList: ImmutableList<LocationItem>,
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ){

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        UText(text = placeItem.title, style = UmcTypographyTokens.BodyBold, color = grey950())
                        Spacer(modifier = Modifier.height(2.dp))
                        UText(text = placeItem.address, style = UmcTypographyTokens.Footnote, color = grey600())
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    UButton(
                        text = AppStrings.COMMON_SELECT,
                        onClick = { onItemClick(placeItem) },
                        backgroundColor = grey100(),
                        textColor = grey700(),
                        textStyle = UmcTypographyTokens.SubheadlineBold,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    )

                }


            }
        }
    }
}
