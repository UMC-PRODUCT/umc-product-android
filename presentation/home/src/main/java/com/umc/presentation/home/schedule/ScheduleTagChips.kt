package com.umc.presentation.home.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.umc.component.component.UText
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey200
import com.umc.component.theme.grey700
import com.umc.domain.model.enums.CategoryType

/** 일정 태그(카테고리) 칩. 표시 전용이라 클릭을 받지 않는다(카드의 clickable 로 터치를 그대로 전달). */
@Composable
fun ScheduleTagChip(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = grey700(),
    textStyle: TextStyle = UmcTypographyTokens.Caption1Bold,
) {
    Box(
        modifier = modifier
            .heightIn(min = 24.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(grey000())
            .border(1.dp, grey200(), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        UText(text = text, style = textStyle, color = textColor)
    }
}

/**
 * FlowRow 안에서 태그 칩들을 이어 그린다. maxVisible 을 넘는 태그는 "+N" 칩 하나로 줄인다.
 * 다른 요소(예: 상세의 D-day 버튼)와 같은 줄에 칩을 놓을 때 쓴다.
 */
@Composable
fun FlowRowScope.ScheduleTagChipItems(
    tags: List<CategoryType>,
    maxVisible: Int = Int.MAX_VALUE,
    textColor: Color = grey700(),
    textStyle: TextStyle = UmcTypographyTokens.Caption1Bold,
) {
    tags.take(maxVisible).forEach {
        ScheduleTagChip(text = it.label, textColor = textColor, textStyle = textStyle)
    }
    val hiddenCount = tags.size - maxVisible
    if (hiddenCount > 0) {
        ScheduleTagChip(text = "+$hiddenCount", textColor = textColor, textStyle = textStyle)
    }
}

/** 태그가 없으면 아무것도 그리지 않는다. maxVisible 을 넘는 태그는 "+N" 칩 하나로 줄인다. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScheduleTagChips(
    tags: List<CategoryType>,
    modifier: Modifier = Modifier,
    maxVisible: Int = Int.MAX_VALUE,
    textColor: Color = grey700(),
) {
    if (tags.isEmpty()) return
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ScheduleTagChipItems(tags = tags, maxVisible = maxVisible, textColor = textColor)
    }
}
