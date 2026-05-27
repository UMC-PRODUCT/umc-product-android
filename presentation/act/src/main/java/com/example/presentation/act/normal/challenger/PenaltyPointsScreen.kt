package com.example.presentation.act.normal.challenger

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.component.UTextField
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTheme
import com.umc.component.theme.UmcTypographyTokens.Body
import com.umc.component.theme.UmcTypographyTokens.Callout
import com.umc.component.theme.UmcTypographyTokens.Caption1Bold
import com.umc.component.theme.UmcTypographyTokens.HeadlineBold
import com.umc.component.theme.UmcTypographyTokens.Subheadline
import com.umc.component.theme.UmcTypographyTokens.SubheadlineBold
import com.umc.component.theme.UmcTypographyTokens.Title3Bold
import com.umc.component.theme.danger100
import com.umc.component.theme.danger500
import com.umc.component.theme.indigo500
import com.umc.component.theme.neutral000
import com.umc.component.theme.neutral100
import com.umc.component.theme.neutral200
import com.umc.component.theme.neutral300
import com.umc.component.theme.neutral400
import com.umc.component.theme.neutral500
import com.umc.component.theme.neutral600
import com.umc.component.theme.neutral800
import com.umc.component.theme.neutral900
import com.umc.component.theme.success100

@Composable
fun PenaltyPointsRoute() {
    PenaltyPointsScreen()
}

@Composable
fun PenaltyPointsScreen(
    modifier: Modifier = Modifier,
    penalties: List<PenaltyPointItem> = defaultPenaltyItems(),
    onSubmitClick: (PenaltyPointItem, String) -> Unit = { _, _ -> }
) {
    var selectedFilter by rememberSaveable { mutableStateOf(PenaltyFilter.ALL) }
    var selectedPenaltyId by rememberSaveable { mutableStateOf<Long?>(null) }
    var memo by rememberSaveable { mutableStateOf("") }

    val filteredPenalties = penalties.filter {
        selectedFilter == PenaltyFilter.ALL || it.filter == selectedFilter
    }
    val selectedPenalty = penalties.firstOrNull { it.id == selectedPenaltyId }
    val hasMemo = memo.isNotBlank()
    val isSubmitEnabled = selectedPenalty != null && hasMemo

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(848.dp)
            .imePadding()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(neutral000())
            .padding(horizontal = 16.dp)
    ) {
        DragHeader()

        Spacer(modifier = Modifier.height(16.dp))

        UText(
            text = AppStrings.PUNISH_TITLE,
            style = Title3Bold,
            color = neutral800()
        )

        Spacer(modifier = Modifier.height(8.dp))

        UText(
            text = AppStrings.PUNISH_CONTENT,
            style = Subheadline,
            color = neutral600()
        )

        Spacer(modifier = Modifier.height(16.dp))

        FilterTabs(
            selectedFilter = selectedFilter,
            onFilterSelected = {
                selectedFilter = it
                if (selectedPenaltyId != null && penalties.none { item ->
                        item.id == selectedPenaltyId && (it == PenaltyFilter.ALL || item.filter == it)
                    }) {
                    selectedPenaltyId = null
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        PenaltyList(
            penalties = filteredPenalties,
            selectedPenaltyId = selectedPenaltyId,
            onSelectPenalty = { selectedPenaltyId = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        UText(
            text = AppStrings.MEMO,
            style = SubheadlineBold,
            color = neutral800()
        )

        Spacer(modifier = Modifier.height(8.dp))

        MemoInput(
            value = memo,
            onValueChange = { memo = it }
        )

        Spacer(modifier = Modifier.weight(1f))

        UButton(
            modifier = Modifier.fillMaxWidth(),
            text = AppStrings.REWARD_SUBMIT,
            enabled = isSubmitEnabled,
            textStyle = HeadlineBold,
            textColor = if (isSubmitEnabled) neutral000() else neutral300(),
            backgroundColor = if (isSubmitEnabled) indigo500() else neutral100(),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 16.dp),
            cornerRadius = 8.dp,
            onClick = {
                selectedPenalty?.let { onSubmitClick(it, memo) }
            }
        )

        Spacer(modifier = Modifier.height(72.dp))
    }
}

@Composable
private fun DragHeader(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(neutral600())
        )
    }
}

@Composable
private fun FilterTabs(
    selectedFilter: PenaltyFilter,
    onFilterSelected: (PenaltyFilter) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PenaltyFilter.entries.forEach { filter ->
            val selected = selectedFilter == filter

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(if (selected) neutral900() else neutral100())
                    .clickable { onFilterSelected(filter) }
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                UText(
                    text = filter.label,
                    style = SubheadlineBold,
                    color = if (selected) neutral000() else neutral500()
                )
            }
        }
    }
}

@Composable
private fun PenaltyList(
    penalties: List<PenaltyPointItem>,
    selectedPenaltyId: Long?,
    onSelectPenalty: (Long) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        penalties.forEachIndexed { index, item ->
            PenaltyRow(
                item = item,
                selected = selectedPenaltyId == item.id,
                onClick = { onSelectPenalty(item.id) }
            )

            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                color = neutral200()
            )

        }
    }
}

@Composable
private fun PenaltyRow(
    item: PenaltyPointItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UText(
                text = item.title,
                style = Body,
                color = neutral800()
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .wrapContentWidth()
                    .height(24.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(danger100())
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                UText(
                    text = "-${item.score}",
                    style = Caption1Bold,
                    color = danger500()
                )
            }
        }

        Icon(
            painter = painterResource(
                if (selected) R.drawable.ic_radio_button_checked else R.drawable.ic_radio_button_unchecked
            ),
            contentDescription = null,
            tint = if (selected) indigo500() else neutral400()
        )
    }
}

@Composable
private fun MemoInput(
    value: String,
    onValueChange: (String) -> Unit
) {
    UTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = AppStrings.MEMO_PLACEHOLDER,
        placeholderColor = neutral400(),
        textColor = neutral800(),
        textStyle = Callout.copy(color = neutral800()),
        backgroundColor = neutral000(),
        strokeColor = neutral300(),
        focusStrokeColor = neutral300(),
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    )
}

enum class PenaltyFilter(val label: String) {
    ALL("전체"),
    STAFF("운영진"),
    CHAIR("회장단")
}

data class PenaltyPointItem(
    val id: Long,
    val title: String,
    val score: Int,
    val filter: PenaltyFilter
)

private fun defaultPenaltyItems(): List<PenaltyPointItem> = listOf(
    PenaltyPointItem(id = 1L, title = "과제 미수행", score = 4, filter = PenaltyFilter.CHAIR),
    PenaltyPointItem(id = 2L, title = "스터디 무단 지각", score = 2, filter = PenaltyFilter.CHAIR),
    PenaltyPointItem(id = 3L, title = "스터디 무단 불참", score = 4, filter = PenaltyFilter.CHAIR),
    PenaltyPointItem(id = 4L, title = "행사 무단 지각", score = 2, filter = PenaltyFilter.STAFF),
    PenaltyPointItem(id = 5L, title = "행사 중도 퇴실", score = 2, filter = PenaltyFilter.STAFF),
    PenaltyPointItem(id = 6L, title = "행사 기간 외 취소", score = 4, filter = PenaltyFilter.STAFF),
    PenaltyPointItem(id = 7L, title = "노쇼(무단 결석)", score = 10, filter = PenaltyFilter.CHAIR)
)

@Preview(showBackground = false)
@Composable
private fun PenaltyPointsScreenPreview() {
    UmcTheme(darkTheme = false) {
        PenaltyPointsScreen()
    }
}
