package com.example.presentation.act.normal.challenger

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.umc.component.R
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
import com.umc.component.theme.accent100
import com.umc.component.theme.accent500
import com.umc.component.theme.danger100
import com.umc.component.theme.danger500
import com.umc.component.theme.neutral000
import com.umc.component.theme.neutral100
import com.umc.component.theme.neutral200
import com.umc.component.theme.neutral400
import com.umc.component.theme.neutral600
import com.umc.component.theme.neutral800
import com.umc.component.theme.neutral900
import com.umc.component.theme.primary100
import com.umc.component.theme.primary500
import com.umc.component.theme.success100
import com.umc.component.theme.success500


@Composable
fun NormalChallengerDetailRoute() {
    NormalChallengerDetailScreen()
}

@Composable
fun NormalChallengerDetailScreen(
) {
    var isEditMode by remember { mutableStateOf(false) }

    val ui = ChallengerDetailUi(
        nicknameWithName = "홍길동(닉네임)",
        generation = "기수",
        totalScore = 0,
        school = "중앙대학교",
        part = "Web",
        history = listOf(
            HistoryDetail(date = "2024.01.01", content = "스터디 미제출", score = -1),
            HistoryDetail(date = "2024.01.01", content = "베스트 워크북 수행", score = 1)
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(neutral100()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(56.dp))

            Header()
        }

        item {
            ProfileInfoSection(ui = ui)
        }

        item{
            ScoreButtons(
                onAddClick = { },
                onMinusClick = { },
                onOtherClick = { }
            )
        }

        item {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .background(neutral200())
            )
        }

        item {
            HistorySection(
                totalPlusCount = 1,
                totalMinusCount = 1,
                history = ui.history,
                isEditMode = isEditMode
            )
        }

        item{
            EditChip(onEditClick = { isEditMode = !isEditMode })
        }
    }
}

@Composable
fun ScoreButtons(
    onAddClick: () -> Unit,
    onMinusClick: () -> Unit,
    onOtherClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AddScore(onAddClick = onAddClick)
        MinusScore(onMinusClick = onMinusClick)
        OtherScore(onOtherClick = onOtherClick)
    }
}

@Composable
private fun Header() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(neutral100())
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clickable { },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back),
                contentDescription = null,
                tint = neutral800(),
                modifier = Modifier
                    .size(24.dp)
                    .padding(4.dp)
            )
        }

        Spacer(Modifier.width(10.dp))

        UText(
            text = AppStrings.CHALLENGER_MANAGE_PROFILE_TITLE,
            style = Title2Bold,
            color = neutral800()
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
                .background(neutral900()),
            contentAlignment = Alignment.Center
        ) {
            UText(
                text = "UMC",
                style = BodyBold,
                color = neutral000()
            )
        }

        Row(
            modifier = Modifier
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UText(
                text = ui.nicknameWithName,
                style = HeadlineBold,
                color = neutral800()
            )
            Spacer(modifier = Modifier.width(8.dp))
            UText(
                text = ui.generation,
                style = Footnote,
                color = neutral600()
            )
            Spacer(modifier = Modifier.width(16.dp))
            InfoChip(
                text = ui.school,
                background = accent100(),
                textColor = accent500()
            )
            Spacer(modifier = Modifier.width(8.dp))
            InfoChip(
                text = ui.part,
                background = primary100(),
                textColor = primary500()
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(neutral200())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            UText(
                text = "${ui.nicknameWithName.split("(").first()}님의 총점수는 ${ui.totalScore}점입니다.",
                style = Body,
                color = neutral800()
            )
        }
    }
}

@Composable
private fun InfoChip(
    text: String,
    background: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(background)
            .height(24.dp)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        UText(
            text = text,
            style = Caption1Bold,
            color = textColor
        )
    }
}

@Composable
private fun AddScore(
    onAddClick : () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onAddClick)
            .background(neutral000())
            .border(1.dp, neutral200(), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(success100()),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add_filled),
                contentDescription = null,
                tint = success500(),
                modifier = Modifier
                    .size(24.dp)
                    .padding(2.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        UText(
            text = AppStrings.REWARD_TITLE,
            style = HeadlineBold,
            color = neutral800(),
        )
        Spacer(modifier = Modifier.weight(1f))

        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_next),
                contentDescription = null,
                tint = neutral400(),
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
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onMinusClick)
            .background(neutral000())
            .border(1.dp, neutral200(), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(danger100()),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_minus_fill),
                contentDescription = null,
                tint = danger500(),
                modifier = Modifier
                    .size(24.dp)
                    .padding(2.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        UText(
            text = AppStrings.PUNISH_TITLE,
            style = HeadlineBold,
            color = neutral800(),
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_next),
                contentDescription = null,
                tint = neutral400(),
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
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onOtherClick)
            .background(neutral000())
            .border(1.dp, neutral200(), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(neutral100()),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_error),
                contentDescription = null,
                tint = neutral400(),
                modifier = Modifier
                    .size(24.dp)
                    .padding(2.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        UText(
            text = AppStrings.REWARD_ETC_TITLE,
            style = HeadlineBold,
            color = neutral800(),
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_next),
                contentDescription = null,
                tint = neutral400(),
                modifier = Modifier.size(7.dp, 12.dp)
            )
        }
    }
}

@Composable
private fun HistorySection(
    totalPlusCount: Int,
    totalMinusCount: Int,
    history: List<HistoryDetail>,
    isEditMode: Boolean
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
                tint = neutral600(),
                modifier = Modifier.size(21.dp)
            )
            UText(
                text = AppStrings.CHALLENGER_MANAGE_HISTORY_TITLE,
                style = HeadlineBold,
                color = neutral800()
            )

            ScoreCountChip(
                text = "${AppStrings.REWARD} $totalPlusCount",
                bgColor = success100(),
                textColor = success500()
            )
            ScoreCountChip(
                text = "${AppStrings.PUNISH} $totalMinusCount",
                bgColor = danger100(),
                textColor = danger500()
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(neutral000())
                .border(1.dp, neutral200(), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            history.forEach { item ->
                HistoryRow(
                    item = item,
                    isEditMode = isEditMode
                )
            }
        }
    }
}

@Composable
private fun EditChip(
    onEditClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onEditClick)
                .background(neutral200())
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(18.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_slash),
                    contentDescription = null,
                    tint = neutral600(),
                    modifier = Modifier
                        .padding(3.dp)
                        .size(10.dp)
                )
            }

            Spacer(Modifier.width(6.dp))
            UText(
                text = AppStrings.CHALLENGER_MANAGE_ACTION_EDIT_HISTORY,
                style = SubheadlineBold,
                color = neutral600()
            )
        }
    }
}

@Composable
private fun ScoreCountChip(
    text: String,
    bgColor: Color,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .height(24.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        UText(
            text = text,
            style = Caption1Bold,
            color = textColor
        )
    }
}

@Composable
private fun HistoryRow(
    item: HistoryDetail,
    isEditMode: Boolean
) {
    val scoreText = if (item.score > 0) "+${item.score}" else item.score.toString()
    val scoreBgColor = if (item.score > 0) success100() else danger100()
    val scoreTextColor = if (item.score > 0) success500() else danger500()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UText(
                text = item.date,
                style = Subheadline,
                color = neutral600()
            )
            Spacer(Modifier.width(10.dp))
            UText(
                text = item.content,
                style = Subheadline,
                color = neutral800()
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
                text = scoreText,
                style = Caption1Bold,
                color = scoreTextColor
            )
        }

        if(isEditMode) {
            Spacer(modifier = Modifier.width(16.dp))

            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_check_failed),
                    contentDescription = null,
                    tint = neutral400(),
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
    val school : String,
    val part : String,
    val history : List<HistoryDetail>
)

private data class HistoryDetail(
    val date : String,
    val content : String,
    val score : Int
)

@Preview(showBackground = true, name = "Unfocused State")
@Composable
private fun PreviewScreen() {
    UmcTheme(darkTheme = false) {
        NormalChallengerDetailScreen()
    }
}
