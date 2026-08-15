package com.umc.presentation.act.admin.challenger

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.component.R
import com.umc.component.component.UDialog
import com.umc.component.component.UInfoChip
import com.umc.component.component.UInfoChipType
import com.umc.component.component.UText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTheme
import com.umc.component.theme.UmcTypographyTokens.Body
import com.umc.component.theme.UmcTypographyTokens.BodyBold
import com.umc.component.theme.UmcTypographyTokens.Caption1Bold
import com.umc.component.theme.UmcTypographyTokens.Footnote
import com.umc.component.theme.UmcTypographyTokens.HeadlineBold
import com.umc.component.theme.UmcTypographyTokens.Subheadline
import com.umc.component.theme.UmcTypographyTokens.SubheadlineBold
import com.umc.component.theme.UmcTypographyTokens.Title2Bold
import com.umc.component.theme.red100
import com.umc.component.theme.red500
import com.umc.component.theme.grey000
import com.umc.component.theme.grey100
import com.umc.component.theme.grey200
import com.umc.component.theme.grey400
import com.umc.component.theme.grey600
import com.umc.component.theme.grey800
import com.umc.component.theme.grey900
import com.umc.component.theme.green100
import com.umc.component.theme.green500
import com.umc.component.theme.grey50
import com.umc.component.theme.white
import com.umc.component.theme.yellow100
import com.umc.component.theme.yellow400
import com.umc.domain.model.act.challenger.ChallengerManageDialogModel
import com.umc.domain.model.enums.UserPart
import com.umc.presentation.act.admin.challenger.bottomsheet.OtherPointsScreen
import com.umc.presentation.act.admin.challenger.bottomsheet.PenaltyPointsScreen
import com.umc.presentation.act.admin.challenger.bottomsheet.RewardPointsScreen
import kotlinx.coroutines.flow.collectLatest

private enum class PointGrantSheet {
    REWARD,
    PENALTY,
    OTHER,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminChallengerDetailRoute(
    challengerId: Long = 0L,
    onNavigateToBack: () -> Unit = {},
    viewModel: AdminChallengerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var pointGrantSheet by remember { mutableStateOf<PointGrantSheet?>(null) }

    LaunchedEffect(challengerId) {
        viewModel.getChallengerDetail(challengerId)
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                AdminChallengerEvent.PointGranted -> pointGrantSheet = null
                is AdminChallengerEvent.ShowToast ->
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    AdminChallengerDetailScreen(
        uiState = uiState,
        onBackClick = onNavigateToBack,
        onAddScoreClick = {
            viewModel.resetPointGrantForm()
            pointGrantSheet = PointGrantSheet.REWARD
        },
        onMinusScoreClick = {
            viewModel.resetPointGrantForm()
            pointGrantSheet = PointGrantSheet.PENALTY
        },
        onOtherScoreClick = {
            viewModel.resetPointGrantForm()
            pointGrantSheet = PointGrantSheet.OTHER
        },
        onEditClick = viewModel::toggleDetailEditMode,
        onDeleteClick = viewModel::selectDeleteTarget,
        onDismissDelete = { viewModel.selectDeleteTarget(null) },
        onConfirmDelete = viewModel::deleteSelectedPoint
    )

    pointGrantSheet?.let { sheet ->
        ModalBottomSheet(
            onDismissRequest = {
                pointGrantSheet = null
                viewModel.resetPointGrantForm()
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.Transparent,
            dragHandle = null,
        ) {
            when (sheet) {
                PointGrantSheet.PENALTY -> PenaltyPointsScreen(
                    uiState = uiState,
                    onFilterSelected = viewModel::selectPenaltyFilter,
                    onSelectPenalty = viewModel::selectPenalty,
                    onMemoChange = viewModel::onMemoChanged,
                    onSubmitClick = { viewModel.grantPenalty(challengerId) },
                )

                PointGrantSheet.REWARD -> RewardPointsScreen(
                    uiState = uiState,
                    onSelectReward = viewModel::selectReward,
                    onMemoChange = viewModel::onMemoChanged,
                    onSubmitClick = { viewModel.grantReward(challengerId) },
                )

                PointGrantSheet.OTHER -> OtherPointsScreen(
                    uiState = uiState,
                    onRewardMinusClick = viewModel::decreaseRewardScore,
                    onRewardPlusClick = viewModel::increaseRewardScore,
                    onPunishMinusClick = viewModel::decreasePunishScore,
                    onPunishPlusClick = viewModel::increasePunishScore,
                    onReasonChange = viewModel::onCustomReasonChanged,
                    onSubmitClick = { viewModel.grantCustomPoint(challengerId) },
                )
            }
        }
    }
}

@Composable
fun AdminChallengerDetailScreen(
    uiState: AdminChallengerUiState = AdminChallengerUiState(),
    onBackClick: () -> Unit = {},
    onAddScoreClick: () -> Unit = {},
    onMinusScoreClick: () -> Unit = {},
    onOtherScoreClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteClick: (Long) -> Unit = {},
    onDismissDelete: () -> Unit = {},
    onConfirmDelete: () -> Unit = {}
) {
    val ui = uiState.detail.toDetailUi()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(white())
    ) {
        Header(onBackClick = onBackClick)

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                ProfileInfoSection(ui = ui)
            }

            item {
                ScoreButtons(
                    onAddClick = onAddScoreClick,
                    onMinusClick = onMinusScoreClick,
                    onOtherClick = onOtherScoreClick,
                )
            }

            item {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .background(grey100())
                )
            }

            item {
                HistorySection(
                    totalRewardScore = ui.totalRewardScore,
                    totalPenaltyScore = ui.totalPenaltyScore,
                    history = ui.history,
                    isEditMode = uiState.isDetailEditMode,
                    onDeleteClick = { item -> onDeleteClick(item.id) },
                )
            }

            if (ui.history.isNotEmpty()) {
                item {
                    EditChip(onEditClick = onEditClick)
                }
            }
        }
    }

    if (uiState.deleteTarget != null) {
        UDialog(
            isAccept = false,
            title = AppStrings.CHALLENGER_MANAGE_DELETE_TITLE,
            subtitle = AppStrings.CHALLENGER_MANAGE_DELETE_SUBTITLE,
            onDismissRequest = onDismissDelete,
            isTwoButton = true,
            negativeText = AppStrings.COMMON_CANCEL,
            positiveText = AppStrings.DELETE,
            onNegative = onDismissDelete,
            onPositive = onConfirmDelete
        )
    }
}

@Composable
fun ScoreButtons(
    onAddClick: () -> Unit, onMinusClick: () -> Unit, onOtherClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AddScore(onAddClick = onAddClick)
        HorizontalDivider(modifier = Modifier.fillMaxWidth().width(1.dp))
        MinusScore(onMinusClick = onMinusClick)
        HorizontalDivider(modifier = Modifier.fillMaxWidth().width(1.dp))
        OtherScore(onOtherClick = onOtherClick)
    }
}

@Composable
private fun Header(
    onBackClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clickable(onClick = onBackClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back),
                contentDescription = null,
                tint = grey800(),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(Modifier.width(10.dp))

        UText(
            text = AppStrings.CHALLENGER_MANAGE_PROFILE_TITLE, style = Title2Bold, color = grey800()
        )
    }
}

@Composable
private fun ProfileInfoSection(ui: ChallengerDetailUi) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(CircleShape)
                .background(grey900()),
            contentAlignment = Alignment.Center
        ) {
            UText(
                text = "UMC", style = BodyBold, color = grey000()
            )
        }

        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UText(
                text = ui.nicknameWithName,
                modifier = Modifier.weight(1f, fill = false),
                style = HeadlineBold,
                color = grey800(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.width(8.dp))
            UText(
                text = ui.generation, style = Footnote, color = grey600()
            )
            Spacer(modifier = Modifier.width(16.dp))
            UInfoChip(
                text = ui.school, type = UInfoChipType.SCHOOL
            )
            Spacer(modifier = Modifier.width(8.dp))
            UInfoChip(part = ui.part)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(grey50())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            UText(
                text = "${ui.nicknameWithName.split("(").first()}님의 총점수는 ${ui.totalScore}점입니다.",
                style = Body,
                color = grey800()
            )
        }
    }
}

@Composable
private fun AddScore(
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onAddClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(green100()),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add_filled),
                contentDescription = null,
                tint = green500(),
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        UText(
            text = AppStrings.REWARD_TITLE,
            style = HeadlineBold,
            color = grey800(),
        )
        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_next),
                contentDescription = null,
                tint = grey400(),
                modifier = Modifier.size(7.dp, 12.dp)
            )

        }
    }
}

@Composable
private fun MinusScore(
    onMinusClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onMinusClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(red100()),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_minus_fill),
                contentDescription = null,
                tint = red500(),
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        UText(
            text = AppStrings.PUNISH_TITLE,
            style = HeadlineBold,
            color = grey800(),
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_next),
                contentDescription = null,
                tint = grey400(),
                modifier = Modifier.size(7.dp, 12.dp)
            )
        }
    }
}

@Composable
private fun OtherScore(
    onOtherClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOtherClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(yellow100()),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_error),
                contentDescription = null,
                tint = yellow400(),
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        UText(
            text = AppStrings.REWARD_ETC_TITLE,
            style = HeadlineBold,
            color = grey800(),
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_next),
                contentDescription = null,
                tint = grey400(),
                modifier = Modifier.size(7.dp, 12.dp)
            )
        }
    }
}

@Composable
private fun HistorySection(
    totalRewardScore: Int,
    totalPenaltyScore: Int,
    history: List<HistoryDetail>,
    isEditMode: Boolean,
    onDeleteClick: (HistoryDetail) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_history),
                contentDescription = null,
                tint = grey600(),
                modifier = Modifier.size(21.dp)
            )
            UText(
                text = AppStrings.CHALLENGER_MANAGE_HISTORY_TITLE,
                style = HeadlineBold,
                color = grey800()
            )

            ScoreCountChip(
                text = "${AppStrings.REWARD} $totalRewardScore",
                bgColor = green100(),
                textColor = green500()
            )
            ScoreCountChip(
                text = "${AppStrings.PUNISH} $totalPenaltyScore",
                bgColor = red100(),
                textColor = red500()
            )
        }

        if (history.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(grey000())
                    .border(1.dp, grey200(), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                history.forEach { item ->
                    HistoryRow(
                        item = item,
                        isEditMode = isEditMode,
                        onDeleteClick = { onDeleteClick(item) },
                    )
                }
            }
        }
    }
}

@Composable
private fun EditChip(
    onEditClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onEditClick)
                .background(grey100())
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(18.dp), contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_slash),
                    contentDescription = null,
                    tint = grey600(),
                    modifier = Modifier
                        .padding(3.dp)
                        .size(10.dp)
                )
            }

            Spacer(Modifier.width(6.dp))
            UText(
                text = AppStrings.CHALLENGER_MANAGE_ACTION_EDIT_HISTORY,
                style = SubheadlineBold,
                color = grey600()
            )
        }
    }
}

@Composable
private fun ScoreCountChip(
    text: String, bgColor: Color, textColor: Color
) {
    Box(
        modifier = Modifier
            .wrapContentWidth()
            .height(24.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp), contentAlignment = Alignment.Center
    ) {
        UText(
            text = text, style = Caption1Bold, color = textColor
        )
    }
}

@Composable
private fun HistoryRow(
    item: HistoryDetail, isEditMode: Boolean, onDeleteClick: () -> Unit
) {
    val scoreText = if (item.score > 0) "+${item.score}" else item.score.toString()
    val scoreBgColor = if (item.score > 0) green100() else red100()
    val scoreTextColor = if (item.score > 0) green500() else red500()

    Row(
        modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically
        ) {
            UText(
                text = item.date, style = Subheadline, color = grey600()
            )
            Spacer(Modifier.width(10.dp))
            UText(
                text = item.content, style = Subheadline, color = grey800()
            )
        }

        Box(
            modifier = Modifier
                .height(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(scoreBgColor)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            UText(
                text = scoreText, style = Caption1Bold, color = scoreTextColor
            )
        }

        if (isEditMode) {
            Spacer(modifier = Modifier.width(16.dp))

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clickable(
                        onClick = onDeleteClick
                    ), contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_check_failed),
                    contentDescription = null,
                    tint = grey400(),
                    modifier = Modifier
                        .padding(2.dp)
                        .size(20.dp)
                )
            }
        }
    }
}


private data class ChallengerDetailUi(
    val nicknameWithName: String,
    val generation: String,
    val totalScore: Int,
    val school: String,
    val part: UserPart,
    val totalRewardScore: Int,
    val totalPenaltyScore: Int,
    val history: List<HistoryDetail>
)

private data class HistoryDetail(
    val id: Long, val date: String, val content: String, val score: Int
)

private fun ChallengerManageDialogModel?.toDetailUi(): ChallengerDetailUi {
    if (this == null) {
        return ChallengerDetailUi(
            nicknameWithName = "홍길동(닉네임)",
            generation = "기수",
            totalScore = 0,
            school = "중앙대학교",
            part = UserPart.WEB,
            totalRewardScore = 1,
            totalPenaltyScore = 1,
            history = listOf(
                HistoryDetail(id = 1L, date = "2024.01.01", content = "스터디 미제출", score = -1),
                HistoryDetail(id = 2L, date = "2024.01.01", content = "베스트 워크북 수행", score = 1)
            )
        )
    }

    return ChallengerDetailUi(
        nicknameWithName = "$name($nickname)",
        generation = "${gisu}기",
        totalScore = totalScore.toInt(),
        school = university,
        part = part,
        totalRewardScore = rewardScore,
        totalPenaltyScore = penaltyScore,
        history = history.map { point ->
            HistoryDetail(
                id = point.id, date = point.date, content = point.title, score = point.value.toInt()
            )
        })
}

@Preview(showBackground = true, name = "Unfocused State")
@Composable
private fun PreviewScreen() {
    UmcTheme(darkTheme = false) {
        AdminChallengerDetailScreen()
    }
}

@Preview(showBackground = false)
@Composable
private fun previewDeleteDialog() {
    UmcTheme(darkTheme = false) {
        UDialog(
            isAccept = false,
            title = AppStrings.CHALLENGER_MANAGE_DELETE_TITLE,
            subtitle = AppStrings.CHALLENGER_MANAGE_DELETE_SUBTITLE,
            onDismissRequest = { },
            isTwoButton = true,
            negativeText = AppStrings.COMMON_CANCEL,
            positiveText = AppStrings.DELETE,
            onNegative = { },
            onPositive = { })
    }
}
