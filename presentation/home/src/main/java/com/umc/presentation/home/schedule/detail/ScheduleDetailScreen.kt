package com.umc.presentation.home.schedule.detail

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.umc.component.R
import com.umc.component.component.UBasicDialog
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.component.model.UBasicDialogModel
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.accent500
import com.umc.component.theme.neutral000
import com.umc.component.theme.neutral100
import com.umc.component.theme.neutral600
import com.umc.component.theme.neutral800
import com.umc.component.theme.primary100
import com.umc.component.theme.primary600
import com.umc.presentation.home.schedule.add.ScheduleAddEvent
import kotlinx.coroutines.flow.collectLatest
import java.net.URLEncoder


@Composable
fun ScheduleDetailRoute(
    scheduleId: Long, //상세 보기할 일정 ID
    plusDay: Int, //시작일로부터 몇일 +
    viewModel : ScheduleDetailViewModel = hiltViewModel()
){

    val uiState by viewModel.uiState.collectAsState()

    //뒤로 가기 디스패처
    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    //지도 가져오기 위한 context
    val context = LocalContext.current

    //다이얼로그 노출 여부 체크
    var showDeleteDialog by remember { mutableStateOf(false) }

    //초기 데이터 로드
    LaunchedEffect(scheduleId, plusDay) {
        viewModel.getScheduleDetail(scheduleId, plusDay)
    }

    LaunchedEffect(viewModel){
        viewModel.uiEvent.collectLatest { event ->
            when (event){
                is ScheduleDetailEvent.MoveBackPressedEvent -> onBackPressedDispatcher?.onBackPressed()

                //일정 수정
                is ScheduleDetailEvent.EditPlan -> {}

                is ScheduleDetailEvent.CheckDeletePlan -> { showDeleteDialog = true }

                else -> {}
            }
        }
    }

    ScheduleDetailScreen(
        uiState = uiState,
        onBackClick = { onBackPressedDispatcher?.onBackPressed() },
        onMenuClick = viewModel::toggleKebabMenu,
        onEditClick = viewModel::editPlan,
        onDeleteClick = viewModel::checkDeletePlan,
        onMapClick = { openNaverMap(context,
            uiState.place,
            uiState.latitude,
            uiState.longitude
        ) },
        onAttendanceClick = viewModel::onClickConfirmAttention
    )

    //UBsaicDialog 사용(경고 버전)
    if(showDeleteDialog){
        UBasicDialog(
            model = UBasicDialogModel.Warning(
                title = "해당 일정을 삭제하시겠습니까",
                content = "삭제된 일정은 복구할 수 없습니다.",
                positiveText = "삭제하기"
            ),
            onConfirm = {
                viewModel.deletePlan()
                showDeleteDialog = false
            },
            onDismiss = {
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
    onAttendanceClick: () -> Unit //출석 클릭
) {

    //케밥 메뉴 겹치기를 위해 BOX
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(neutral100())
            .padding(horizontal = 16.dp)
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

            //2. D-day 및 제목
            UButton(
                text = uiState.dDay,
                backgroundColor = primary100(),
                textColor = primary600(),
                textStyle = UmcTypographyTokens.FootnoteBold,
                onClick = {}
            )

            Spacer(modifier = Modifier
                .height(16.dp)
            )
            UText(text = uiState.title,
                style = UmcTypographyTokens.Title2Bold,
                color = neutral800()
            )

            Spacer(modifier = Modifier
                .height(8.dp)
            )

            UText(text = uiState.startDate,
                style = UmcTypographyTokens.Subheadline,
                color = neutral600()
            )

            Spacer(modifier = Modifier
                .height(24.dp)
            )

            //3. 일시 및 장소 영역
            ScheduleInfoCard(
                todayDate = uiState.todayDate,
                todayTime = uiState.todayTime,
                place = uiState.place,
                onMapClick = onMapClick
            )

            Spacer(modifier = Modifier
                .height(40.dp)
            )

            //4. 상세 안내 영역
            UText(text = "상세 안내",
                style = UmcTypographyTokens.Title3Bold,
                color = neutral800()
            )

            Spacer(modifier = Modifier
                .height(16.dp)
            )

            UText(
                text = uiState.detail,
                style = UmcTypographyTokens.Body,
                color = neutral600(),
                modifier = Modifier
                    .weight(1f)
            )

            //5. 하단 출석 버튼
            if(uiState.isToday){
                UButton(
                    text = "출석 체크하기",
                    backgroundColor = accent500(),
                    textColor = neutral000(),
                    textStyle = UmcTypographyTokens.HeadlineBold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .padding(bottom = 24.dp),
                    onClick = onAttendanceClick
                )
            }
            else {
                Spacer(modifier = Modifier
                    .height(32.dp)
                )
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
            .padding(vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        //이름과 뒤로가기
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(id = R.drawable.ic_back),
                contentDescription = null,
                modifier = Modifier
                    .clickable { onBackClick() },
                tint = neutral800()
            )
            Spacer(modifier = Modifier
                .width(16.dp)
            )
            UText(text = "일정 상세", style = UmcTypographyTokens.Title2Bold, color = neutral800())
        }
        //메뉴 버튼
        Icon(
            painter = painterResource(id = R.drawable.ic_menu_kebab),
            contentDescription = null,
            modifier = Modifier
                .clickable { onMenuClick() },
            tint = neutral800()
        )

    }
}




//지도를 열고 닫는 로직
private fun openNaverMap(
    context: android.content.Context,
    placeName: String, //장소 이름
    latitude: Double, //위도
    longitude: Double //경도
) {
    try {

        // 네이버 지도 앱에 접근을 시도해보기
        val encodedTitle = java.net.URLEncoder.encode(placeName, "UTF-8")
        val appUri = android.net.Uri.parse(
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
        Toast.makeText(context, "지도를 열 수 없습니다.", Toast.LENGTH_SHORT).show()
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