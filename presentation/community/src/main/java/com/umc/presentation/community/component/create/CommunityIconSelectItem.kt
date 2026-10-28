package com.umc.presentation.community.component.create

import androidx.annotation.DrawableRes
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.umc.component.component.UText
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey200
import com.umc.component.theme.grey400
import com.umc.component.theme.indigo500
import com.umc.presentation.community.R
import com.umc.presentation.community.component.CommunityCategoryIcon

/**
 * 스레드 아이콘(카테고리) 선택 항목
 *
 * Figma `스레드/아이콘 선택`을 구현합니다.
 * 선택 시 테두리와 라벨이 indigo500, 미선택 시 테두리 grey200·라벨 grey400으로 표시합니다.
 * 아이콘 영역은 [CommunityCategoryIcon]으로 표시합니다.
 * 여러 개를 Row에 균등 배치할 때는 Row에 `height(IntrinsicSize.Min)`, 각 항목에
 * `weight(1f).fillMaxHeight()`를 주면 라벨이 줄바꿈되어도 높이가 맞춰집니다.
 *
 * @param label 아이콘 아래에 표시할 라벨
 * @param iconRes 표시할 24dp 아이콘 리소스
 * @param selected 선택 여부
 * @param onClick 항목 클릭 콜백
 */
@Composable
fun CommunityIconSelectItem(
    label: String,
    @DrawableRes iconRes: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember {
        MutableInteractionSource()
    }
    val shape = RoundedCornerShape(8.dp)

    // 선택 여부에 따라 테두리와 라벨 색상 결정
    val contentColor = if (selected) indigo500() else grey400()
    val borderColor = if (selected) indigo500() else grey200()

    Column(
        modifier = modifier
            .widthIn(min = 82.dp)
            .clip(shape)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = shape,
            )
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = null,
                role = Role.RadioButton,
                onClick = onClick,
            )
            // 라벨이 항목 폭을 최대한 쓰도록 좌우 여백은 최소로 둔다 (아이콘은 가운데 정렬)
            .padding(horizontal = 4.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        CommunityCategoryIcon(
            iconRes = iconRes,
        )

        // 폭이 좁으면 잘리지 않고 가운데 정렬로 줄바꿈
        UText(
            text = label,
            style = UmcTypographyTokens.Body,
            color = contentColor,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * 스레드 아이콘 선택 항목 Preview
 *
 * 412dp(Figma 기준)와 좁은 320dp 폭에서 균등 배치 시 라벨 줄바꿈과 높이 정렬을 확인합니다.
 */
@Preview(showBackground = true, widthDp = 412)
@Preview(showBackground = true, widthDp = 320)
@Composable
private fun CommunityIconSelectItemPreview() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        listOf(
            Triple("스터디", R.drawable.ic_category_study, true),
            Triple("자유", R.drawable.ic_category_free, false),
            Triple("프로젝트", R.drawable.ic_category_part_notice, false),
            Triple("질문", R.drawable.ic_category_question, false),
        ).forEach { (label, iconRes, selected) ->
            CommunityIconSelectItem(
                label = label,
                iconRes = iconRes,
                selected = selected,
                onClick = {},
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            )
        }
    }
}
