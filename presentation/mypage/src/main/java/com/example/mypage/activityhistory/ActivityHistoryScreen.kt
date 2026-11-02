package com.example.mypage.activityhistory

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.mypage.dialog.AddCodeDialog
import com.example.mypage.mypage.UMC_KAKAO_CHANNEL_ID
import com.example.mypage.mypage.openKakaoChannel
import com.umc.component.R
import com.umc.component.base.CollectUiEvents
import com.umc.component.component.UButton
import com.umc.component.component.UDialog
import com.umc.component.component.UText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.grey50
import com.umc.component.theme.UmcTheme
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.black
import com.umc.component.theme.grey200
import com.umc.component.theme.grey300
import com.umc.component.theme.grey400
import com.umc.component.theme.grey800
import com.umc.component.theme.grey500
import com.umc.component.theme.grey600
import com.umc.component.theme.grey950
import com.umc.component.theme.main500
import com.umc.component.theme.tagColors
import com.umc.component.theme.white
import com.umc.domain.model.enums.UserChallengerRole
import com.umc.domain.model.mypage.UserActiveItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/**
 * 마이페이지 > 나의 활동 > 활동 이력 화면
 *
 * 기수별 챌린저·운영진 이력을 보여준다. 하단 탭 바가 없는 2뎁스 화면이다.
 */
@Composable
fun ActivityHistoryRoute(
    viewModel: ActivityHistoryViewModel = hiltViewModel(),
    onNavigateToBack: () -> Unit, //뒤로가기
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    CollectUiEvents(viewModel.uiEvent) { event ->
        when (event) {
            is ActivityHistoryEvent.RecordAdded ->
                Toast.makeText(context, AppStrings.ACTIVITY_HISTORY_RECORD_ADDED, Toast.LENGTH_SHORT).show()
            is ActivityHistoryEvent.ShowToast ->
                Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
        }
    }

    ActivityHistoryScreen(
        uiState = uiState,
        onBackClick = onNavigateToBack,
        onRetryClick = viewModel::onClickRetry,
        onAddRecordClick = viewModel::onClickAddRecord,
    )

    //챌린저 기록 추가 - 6자리 코드 입력
    if (uiState.isAddCodeSheetOpen) {
        AddCodeDialog(
            code = uiState.code,
            onCodeChanged = viewModel::onCodeChanged,
            onConfirmClick = viewModel::addChallengerCode,
            isSubmitting = uiState.isAddingCode,
            onDismissRequest = viewModel::onDismissAddCode
        )
    }

    //이미 사용된 코드 안내 (문의하기 = UMC 카카오톡 채널)
    if (uiState.isUsedCodeDialogOpen) {
        UDialog(
            title = AppStrings.MYPAGE_ADDCODE_USED_TITLE,
            content = AppStrings.MYPAGE_ADDCODE_USED_CONTENT,
            isTwoButton = true,
            positiveText = AppStrings.MYPAGE_ADDCODE_INQUIRE,
            negativeText = AppStrings.CONFIRM,
            positiveTextColor = grey800(),
            onPositive = {
                viewModel.onDismissUsedCodeDialog()
                openKakaoChannel(context, UMC_KAKAO_CHANNEL_ID)
            },
            onDismissRequest = viewModel::onDismissUsedCodeDialog
        )
    }
}

@Composable
fun ActivityHistoryScreen(
    uiState: ActivityHistoryUiState,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onAddRecordClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(grey50())
    ) {
        ActivityHistoryTopBar(
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp, bottom = 64.dp)
        ) {
            if (uiState.isLoadFailed) {
                //조회 실패 - 빈 목록 대신 안내와 다시 시도 표시
                ActivityHistoryLoadError(
                    isLoading = uiState.isLoading,
                    onRetryClick = onRetryClick
                )
            } else {
                ActiveHistorySection(
                    histories = uiState.histories,
                    isLoading = uiState.isLoading
                )

                Spacer(modifier = Modifier.height(12.dp))

                AddRecordButton(onClick = onAddRecordClick)
            }
        }
    }
}

/**
 * 운영진에게 받은 6자리 코드로 챌린저 기록을 추가하는 행 (설정 > 내 활동에서 옮겨 왔다)
 */
@Composable
private fun AddRecordButton(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, grey200()),
        colors = CardDefaults.cardColors(containerColor = white())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_plus_circle),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = grey800()
            )
            UText(
                text = AppStrings.MYPAGE_ADDACTIVITY,
                style = UmcTypographyTokens.Body,
                color = grey800(),
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f)
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_next),
                contentDescription = null,
                tint = grey400()
            )
        }
    }
}

/**
 * 활동 이력 화면 상단 탑바 컴포저블
 */
@Composable
fun ActivityHistoryTopBar(
    onBackClick: () -> Unit, //뒤로 가기
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(grey50())
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        //뒤로 가기 버튼
        Icon(
            modifier = Modifier
                .padding(12.dp)
                .clickable { onBackClick() },
            painter = painterResource(id = R.drawable.ic_back),
            contentDescription = "뒤로가기",
            tint = Color.Unspecified,
        )

        UText(
            text = AppStrings.ACTIVITY_HISTORY_TITLE,
            style = UmcTypographyTokens.Title2Bold,
            modifier = Modifier
                .padding(horizontal = 6.dp)
        )
    }
}

/**
 * 활동 이력 제목과 기수별 이력 목록 카드 컴포저블
 */
@Composable
fun ActiveHistorySection(
    histories: ImmutableList<UserActiveItem>,
    isLoading: Boolean = false,
) {
    Column {
        UText(
            text = AppStrings.ACTIVITY_HISTORY_TITLE,
            style = UmcTypographyTokens.HeadlineBold,
            color = grey950()
        )
        Card(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, grey200()),
            colors = CardDefaults.cardColors(containerColor = white())
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 13.dp)
            ) {
                when {
                    histories.isNotEmpty() -> histories.forEach { history ->
                        ActiveHistoryItem(history)
                    }

                    //이력을 만드는 중 - 빈 안내 대신 진행 표시
                    isLoading -> Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = main500(),
                            strokeWidth = 2.dp
                        )
                    }

                    else -> UText(
                        text = AppStrings.ACTIVITY_HISTORY_EMPTY,
                        style = UmcTypographyTokens.Subheadline,
                        color = grey500(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}

/**
 * 기수별 활동 내역 및 파트/직함 배지를 표시하는 레코드 행 컴포저블
 */
@Composable
fun ActiveHistoryItem(
    history: UserActiveItem
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        //1. 기수 정보 (ex. 10기)
        // 숫자마다 글자 폭이 달라 칩 크기가 들쭉날쭉하지 않도록 최소 너비를 맞춘다
        UButton(
            text = history.generation,
            onClick = {},
            modifier = Modifier
                .height(28.dp)
                .widthIn(min = 44.dp),
            backgroundColor = white(),
            borderColor = grey200(),
            borderWidth = 1.dp,
            textColor = grey600(),
            textStyle = UmcTypographyTokens.FootnoteBold,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 5.dp),
        )


        Spacer(
            modifier = Modifier.width(16.dp)
        )


        // 2. 활동 내용 (ex. Android Part 챌린저)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            UText(
                text = history.partName, // "Android Part 챌린저" 등 합성된 문자열
                style = UmcTypographyTokens.Subheadline,
                color = black(),
                modifier = Modifier
                    .weight(1f)
            )

            // 회장·챌린저처럼 두세 글자 직함은 같은 크기로 보이게 최소 너비를 맞추고, 더 긴 직함만 넓어진다
            // 색은 역할 태그 체계(100 배경 · 600 글자)를 따른다
            val roleColors = history.role.tagColors()
            UButton(
                text = history.position,
                onClick = {},
                modifier = Modifier
                    .height(28.dp)
                    .widthIn(min = 56.dp),
                backgroundColor = roleColors.base,
                borderWidth = 0.dp,
                textColor = roleColors.strong,
                textStyle = UmcTypographyTokens.FootnoteBold,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 5.dp),
            )
        }



    }
}

/**
 * 활동 이력 조회에 실패했을 때 빈 목록 대신 표시하는 실패 안내 + 다시 시도 컴포저블
 */
@Composable
private fun ActivityHistoryLoadError(isLoading: Boolean, onRetryClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        UText(
            text = AppStrings.ACTIVITY_HISTORY_LOAD_ERROR_TITLE,
            style = UmcTypographyTokens.HeadlineBold,
            color = grey600(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        UText(
            text = AppStrings.ACTIVITY_HISTORY_LOAD_ERROR_DESCRIPTION,
            style = UmcTypographyTokens.Subheadline,
            color = grey500(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        UButton(
            text = AppStrings.ACTIVITY_HISTORY_RETRY,
            onClick = onRetryClick,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            backgroundColor = main500(),
            textColor = grey50(),
            textStyle = UmcTypographyTokens.HeadlineBold,
            cornerRadius = 12.dp,
        )
    }
}

@PreviewScreenSizes
@Composable
private fun ActivityHistoryScreenPreview() {
    UmcTheme {
        ActivityHistoryScreen(
            uiState = ActivityHistoryUiState(
                // 글자 폭이 다른 기수·직함이 섞여도 칩 크기가 같은지 보이도록 섞어 둔다
                histories = persistentListOf(
                    UserActiveItem("12기", "Web Part", "교내 파트장", UserChallengerRole.SCHOOL_PART_LEADER),
                    UserActiveItem("11기", "PM Part", "챌린저"),
                    UserActiveItem("10기", "중앙대학교 회장", "교내 회장", UserChallengerRole.SCHOOL_PRESIDENT),
                    UserActiveItem("11기", "중앙대학교 교내 운영진", "교내 운영진", UserChallengerRole.SCHOOL_ETC_ADMIN),
                    UserActiveItem("9기", "수달지부 지부장", "지부장", UserChallengerRole.CHAPTER_PRESIDENT),
                    UserActiveItem("9기", "중앙운영사무국 총괄", "총괄", UserChallengerRole.CENTRAL_PRESIDENT),
                ),
            ),
            onBackClick = {},
            onRetryClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 400)
@Composable
private fun ActivityHistoryScreenEmptyPreview() {
    UmcTheme {
        ActivityHistoryScreen(
            uiState = ActivityHistoryUiState(),
            onBackClick = {},
            onRetryClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 400)
@Composable
private fun ActivityHistoryScreenLoadingPreview() {
    UmcTheme {
        ActivityHistoryScreen(
            uiState = ActivityHistoryUiState(isLoading = true),
            onBackClick = {},
            onRetryClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 600)
@Composable
private fun ActivityHistoryScreenLoadFailedPreview() {
    UmcTheme {
        ActivityHistoryScreen(
            uiState = ActivityHistoryUiState(isLoadFailed = true),
            onBackClick = {},
            onRetryClick = {},
        )
    }
}
