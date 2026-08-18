package com.umc.presentation.home.schedule.detail

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import android.content.Context
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.component.R
import com.umc.component.component.DialogType
import com.umc.component.component.UBasicDialog
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.yellow500
import com.umc.component.theme.grey000
import com.umc.component.theme.grey100
import com.umc.component.theme.grey600
import com.umc.component.theme.grey800
import com.umc.component.theme.grey950
import com.umc.component.theme.indigo100
import com.umc.component.theme.indigo500
import com.umc.component.theme.indigo600
import com.umc.presentation.home.schedule.add.ScheduleAddEvent
import kotlinx.coroutines.flow.collectLatest
import java.net.URLEncoder


@Composable
fun ScheduleDetailRoute(
    viewModel : ScheduleDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onNavigateToAttendSchedule: () -> Unit,
    onNavigateToEditSchedule: (Long) -> Unit,


){

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    //지도 가져오기 위한 context
    val context = LocalContext.current

    //다이얼로그 노출 여부 체크
    var showDeleteDialog by remember { mutableStateOf(false) }


    //화면이 resume에서 복귀할떄마다 재호출
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.getScheduleDetail()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    LaunchedEffect(viewModel){
        viewModel.uiEvent.collectLatest { event ->
            when (event){
                is ScheduleDetailEvent.MoveBackPressedEvent -> onBackClick()

                //일정 수정
                is ScheduleDetailEvent.EditPlan -> onNavigateToEditSchedule(uiState.content.scheduleId)

                is ScheduleDetailEvent.CheckDeletePlan -> { showDeleteDialog = true }


                else -> {}
            }
        }
    }

    ScheduleDetailScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onMenuClick = viewModel::toggleKebabMenu,
        onEditClick = viewModel::editPlan,
        onDeleteClick = viewModel::checkDeletePlan,
        onMapClick = { openNaverMap(context,
            uiState.place,
            uiState.latitude,
            uiState.longitude
        ) },
        onAttendanceClick = onNavigateToAttendSchedule,
    )

    //UBsaicDialog 사용(경고 버전)
    if(showDeleteDialog){
        UBasicDialog(
            title = AppStrings.HOME_PLAN_DETAIL_DELETE_DIALOG_TITLE,
            content = AppStrings.HOME_PLAN_DETAIL_DELETE_DIALOG_CONTENT,
            positiveText = AppStrings.HOME_PLAN_DETAIL_DELETE_DIALOG_CONFIRM,
            type = DialogType.WARNING,
            onPositive = {
                viewModel.deletePlan()
                showDeleteDialog = false
            },
            onNegative = {
                showDeleteDialog = false
            },
            onDismissRequest = {
                showDeleteDialog = false
            }
        )
    }

}

@Composable
fun ScheduleDetailScreen(
    uiState: ScheduleDetailUiState,
    onBackClick: () -> Unit, //뒤로 가기
    onMenuClick: () -> Unit, //케밥 메뉴 클릭
    onEditClick: () -> Unit, //수정하기 클릭 시(수정 이동)
    onDeleteClick: () -> Unit, //삭제 클릭
    onMapClick: () -> Unit, //장소 상세보기 클릭
    onAttendanceClick: () -> Unit, //출석 클릭
) {

    //케밥 메뉴 겹치기를 위해 BOX
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(grey000())

    ) {

        Column(modifier = Modifier
            .fillMaxSize()
        ) {
            //1. 상단 바 & 케밥 메뉴
            ScheduleDetailTopBar(
                onBackClick = onBackClick,
                onMenuClick = onMenuClick
            )

            Spacer(modifier = Modifier
                .height(36.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {


                //2. D-day 및 제목
                UButton(
                    text = uiState.dDay,
                    backgroundColor = indigo100(),
                    textColor = indigo500(),
                    textStyle = UmcTypographyTokens.FootnoteBold,
                    onClick = {},
                    modifier = Modifier
                        .height(24.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                )

                Spacer(
                    modifier = Modifier
                        .height(16.dp)
                )
                UText(
                    text = uiState.title,
                    style = UmcTypographyTokens.Title2Bold,
                    color = grey800()
                )

                Spacer(
                    modifier = Modifier
                        .height(8.dp)
                )

                UText(
                    text = uiState.startDate,
                    style = UmcTypographyTokens.Subheadline,
                    color = grey600()
                )

                Spacer(
                    modifier = Modifier
                        .height(24.dp)
                )

                //3. 일시 및 장소 영역
                ScheduleInfoCard(
                    todayDate = uiState.todayDate,
                    todayTime = uiState.todayTime,
                    place = uiState.place,
                    onMapClick = onMapClick,
                    isonline = uiState.isonline
                )

                Spacer(
                    modifier = Modifier
                        .height(40.dp)
                )

                //4. 상세 안내 영역
                UText(
                    text = AppStrings.HOME_PLAN_DETAIL_PLAN_NOTICE,
                    style = UmcTypographyTokens.Title3Bold,
                    color = grey800()
                )

                Spacer(
                    modifier = Modifier
                        .height(16.dp)
                )

                UText(
                    text = uiState.detail,
                    style = UmcTypographyTokens.Body,
                    color = grey600(),
                    modifier = Modifier
                        .weight(1f)
                )

                //5. 하단 출석 버튼
                if (uiState.isToday) {
                    UButton(
                        text = AppStrings.HOME_PLAN_DETAIL_CHECK_CONFIRM,
                        backgroundColor = grey950(), // accent 팔레트 제거로 yellow(구 warning)로 대체
                        textColor = grey000(),
                        textStyle = UmcTypographyTokens.HeadlineBold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        cornerRadius = 12.dp,
                        onClick = onAttendanceClick
                    )

                    Spacer(
                        modifier = Modifier
                            .height(32.dp)
                    )

                } else {
                    Spacer(
                        modifier = Modifier
                            .height(32.dp)
                    )
                }
            }

        }


        //6. 케밥 메뉴(버튼 누를 때 나오기)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 56.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            ScheduleKebabMenu(
                isVisible = uiState.isMenuVisible,
                isAuthor = uiState.isAuthor,
                onEditClick = onEditClick,
                onDeleteClick = onDeleteClick
            )
        }
    }

}

/**상단 top bar**/
@Composable
fun ScheduleDetailTopBar(
    onBackClick: () -> Unit,
    onMenuClick: () -> Unit
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 4.dp,
                top = 8.dp,
                end = 24.dp,
                bottom = 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        //이름과 뒤로가기
        Row(verticalAlignment = Alignment.CenterVertically) {

            Icon(
                modifier = Modifier
                    .padding(12.dp)
                    .clickable { onBackClick() },
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = null,
                tint = Color.Unspecified,
            )

            UText(text = AppStrings.HOME_PLAN_DETAIL_TITLE,
                style = UmcTypographyTokens.Title2Bold,
                color = grey800()
            )
        }
        //메뉴 버튼
        Icon(
            painter = painterResource(id = R.drawable.ic_menu_kebab),
            contentDescription = null,
            modifier = Modifier
                .size(24.dp)
                .clickable { onMenuClick() },

            tint = grey800()
        )

    }
}




//지도를 열고 닫는 로직
private fun openNaverMap(
    context: Context,
    placeName: String, //장소 이름
    latitude: Double, //위도
    longitude: Double //경도
) {
    try {

        // 네이버 지도 앱에 접근을 시도해보기
        val encodedTitle = URLEncoder.encode(placeName, "UTF-8")
        val appUri = Uri.parse(
            "nmap://map?lat=$latitude&lng=$longitude&zoom=15&title=$encodedTitle&appname=${context.packageName}"
        )
        val appIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, appUri)

        // 앱이 설치되어 있는지 확인 (Manifest의 <queries> 설정)
        // 앱이 있으면 intent로 보내주기
        if (appIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(appIntent)
        } else {
            // 앱이 없으면 웹 브라우저용 URL 실행
            val webUrl = "https://m.map.naver.com/map.naver?pinId=&pinType=site&lat=$latitude&lng=$longitude&dlevel=11&enc=utf8"
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
            context.startActivity(webIntent)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        //UToast.createToast(requireContext(), "지도를 열 수 없습니다.", )
        Toast.makeText(context, AppStrings.HOME_PLAN_DETAIL_MAP_ERROR, Toast.LENGTH_SHORT).show()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewScheduleDetailMenuOpen() {
    val mockUiState = ScheduleDetailUiState(
        title = "삭제/수정 테스트 중",
        dDay = "D-14",
        startDate = "2026.05.10",
        todayDate = "2026.05.10",
        todayTime = "10:00-12:00",
        place = "온라인(Zoom)",
        detail = "이 화면은 케밥 메뉴가 열렸을 때의 레이아웃을 확인하기 위한 프리뷰입니다.",
        isAuthor = true,
        isMenuVisible = true // 메뉴 열림 상태
    )

    ScheduleDetailScreen(
        uiState = mockUiState,
        onBackClick = {},
        onMenuClick = {},
        onEditClick = {},
        onDeleteClick = {},
        onMapClick = {},
        onAttendanceClick = {}
    )
}