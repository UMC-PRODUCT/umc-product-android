package com.umc.presentation.home.schedule

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.component.UChip
import com.umc.component.component.UText
import com.umc.component.theme.*
import kotlinx.coroutines.flow.collectLatest
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner

@Composable
fun ScheduleAddRoute(
    viewModel: ScheduleAddViewModel = hiltViewModel(),
    onShowCategoryDialog: () -> Unit,
    onShowLocationDialog: () -> Unit,
    onShowParticipantDialog: () -> Unit,
    onShowDatePicker: (isStart: Boolean) -> Unit,
    onShowTimePicker: (isStart: Boolean) -> Unit,
    onShowAttendanceDialog: (onConfirm: () -> Unit, onReject: () -> Unit) -> Unit
){

    val uiState by viewModel.uiState.collectAsState()
    val onBackPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    LaunchedEffect(viewModel){
        viewModel.uiEvent.collectLatest { event ->
            when (event){
                is ScheduleAddEvent.MoveBackPressedEvent -> onBackPressedDispatcher?.onBackPressed()
                else -> {}
            }
        }
    }

    ScheduleAddScreen(
        uiState = uiState,
        onBackClick = { onBackPressedDispatcher?.onBackPressed() },
        onTitleChanged = viewModel::updatePlanTitle,
        onDetailChanged = viewModel::updatePlanDetail,
        onAlldayChanged = viewModel::setAllday,
        onCategoryClick = onShowCategoryDialog,
        onLocationClick = onShowLocationDialog,
        onParticipantClick = onShowParticipantDialog,
        onStartDateClick = { onShowDatePicker(true) },
        onStartTimeClick = { onShowTimePicker(true) },
        onEndDateClick = { onShowDatePicker(false) },
        onEndTimeClick = { onShowTimePicker(false) },
        onRegisterClick = {
            //운영진 여부 및 수정 모드에 따른 분기 로직
            if (uiState.isManager && !uiState.editMode) {
                onShowAttendanceDialog(
                    { viewModel.submitPlan(true) },
                    { viewModel.submitPlan(false) }
                )
            } else {
                viewModel.submitPlan(uiState.editMode)
            }
        }
    )


}

@Composable
fun ScheduleAddScreen(
    uiState: ScheduleAddUiState,
    onBackClick: () -> Unit, //뒤로 가기를 누를 때
    onTitleChanged: (String) -> Unit, //일정 제목이 바뀔 때 (ViewModel)
    onDetailChanged: (String) -> Unit, //일정 상세 내용이 바뀔 때
    onAlldayChanged: (Boolean) -> Unit, //하루 종일 여부가 바뀔 때
    onCategoryClick: () -> Unit, //일정 태그(카테고리)를 누를 때
    onLocationClick: () -> Unit, //일정 장소를 누를 때
    onParticipantClick: () -> Unit, //일정 참여자를 누를 때
    onStartDateClick: () -> Unit, //일정 시작 날짜를 수정 시
    onStartTimeClick: () -> Unit, //일정 시작 시각 수정 시
    onEndDateClick: () -> Unit, //일정 종료 날짜 수정 시
    onEndTimeClick: () -> Unit, //일정 종료 시각 수정 시
    onRegisterClick: () -> Unit //일정 등록 시
){

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(neutral000())

    ) {
        //1. 상단 바
        ScheduleAddTopBar(onBackClick = onBackClick)

        //2. 일정 입력 영역(스크롤)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            //3. 일정 제목
            ScheduleInputSection(title = "일정 제목", required = true) {
                OutlinedTextField(
                    value = uiState.planTitle,
                    onValueChange = onTitleChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("일정 제목을 입력해 주세요", color = neutral400()) },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = neutral300(),
                        focusedBorderColor = primary500()
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            //4. 태그
            ScheduleInputSection(title = "태그", required = true) {
                SelectableField(
                    text = if (!uiState.isSelectedCategory) "태그를 선택해 주세요" else uiState.selectedCategoriesString,
                    isPlaceholder = !uiState.isSelectedCategory,
                    onClick = onCategoryClick
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            //5. 장소
            ScheduleInputSection(title = "장소", required = true) {
                SelectableField(
                    text = if (uiState.planLocation.isEmpty()) "위치를 입력해 주세요" else uiState.planLocation,
                    isPlaceholder = uiState.planLocation.isEmpty(),
                    onClick = onLocationClick
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            //6. 일시
            ScheduleInputSection(title = "일시", required = false) {
                ScheduleDateCard(
                    uiState = uiState,
                    onAlldayChanged = onAlldayChanged,
                    onStartDateClick = onStartDateClick,
                    onStartTimeClick = onStartTimeClick,
                    onEndDateClick = onEndDateClick,
                    onEndTimeClick = onEndTimeClick
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            //7. 상세 안내
            ScheduleInputSection(title = "상세 안내", required = false) {
                OutlinedTextField(
                    value = uiState.planDetail,
                    onValueChange = onDetailChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(144.dp),
                    placeholder = { Text("상세 내용을 입력해 주세요", color = neutral400()) },
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = neutral300(),
                        focusedBorderColor = primary500()
                    )
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            //8. 챌린저 명단
            ScheduleInputSection(title = "챌린저 명단", required = false) {
                SelectableField(
                    text = if (!uiState.isSelectedParticipant) "챌린저를 선택해 주세요" else uiState.selectedParticipantsString,
                    isPlaceholder = !uiState.isSelectedParticipant,
                    onClick = onParticipantClick
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
            
            //9. 하단 버튼들
            ScheduleAddActionButtons(
                registerOk = uiState.isRegisterOk,
                editMode = uiState.editMode,
                onCancelClick = onBackClick,
                onRegisterClick = onRegisterClick
            )

            Spacer(modifier = Modifier.height(64.dp))

        }


    }

}

/**상단 top bar**/
@Composable
fun ScheduleAddTopBar(onBackClick: () -> Unit){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        IconButton(onClick = onBackClick) {
          Icon(
              painter = painterResource(id=R.drawable.ic_back),
              contentDescription = null,
              tint = neutral800()
          )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "일정 추가",
            style = UmcTypographyTokens.Title2Bold,
            color = neutral800()
        )

    }
}

/**스케쥴에서 필요한 제목을 작성하는 섹션**/
@Composable
fun ScheduleInputSection(
    title: String,
    required: Boolean,
    content: @Composable () -> Unit //하위 컴포지블 작성
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = title, style = UmcTypographyTokens.HeadlineBold, color = neutral800())
            if (required) {
                Text(text = "*", style = UmcTypographyTokens.HeadlineBold, color = danger500(), modifier = Modifier.padding(start = 4.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        content()
    }
}

/**터치하여 선택하는 textField**/
@Composable
fun SelectableField(text: String, isPlaceholder: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, neutral300()),
        color = neutral000()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = text,
                modifier = Modifier.weight(1f),
                style = UmcTypographyTokens.Body,
                color = if (isPlaceholder) neutral400() else neutral800(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(painter = painterResource(id = R.drawable.ic_arrow_next), contentDescription = null, tint = neutral400())
        }
    }
}

/**일정 등록&취소 버튼**/
@Composable
fun ScheduleAddActionButtons(
    registerOk: Boolean,
    editMode: Boolean,
    onCancelClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        UButton(
            text = "취소",
            modifier = Modifier.weight(1f).height(52.dp),
            cardBackgroundColor = neutral000(),
            borderColor = neutral300(),
            borderWidth = 1.dp,
            textColor = neutral800(),
            onClick = onCancelClick
        )
        Spacer(modifier = Modifier.width(16.dp))
        UButton(
            text = if (editMode) "수정" else "등록",
            modifier = Modifier.weight(1f).height(52.dp),
            cardBackgroundColor = if (registerOk) primary500() else neutral300(),
            textColor = neutral000(),
            onClick = onRegisterClick
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ScheduleAddScreenPreview() {
    //ScheduleAddScreen()
}