package com.umc.presentation.community.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey100
import com.umc.component.theme.grey200
import com.umc.component.theme.grey800
import com.umc.component.theme.grey950
import com.umc.component.theme.indigo500
import com.umc.presentation.community.model.CommunityCategory

@Composable
fun CommunityFilterMenu(
    expanded: Boolean,
    selectedCategory: CommunityCategory,
    onDismissRequest: () -> Unit,
    onCategorySelected: (CommunityCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!expanded) return

    val menuYOffset = with(LocalDensity.current) {
        44.dp.roundToPx()
    }

    Popup(
        alignment = Alignment.TopEnd,
        offset = IntOffset(
            x = 0,
            y = menuYOffset,
        ),
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(
            focusable = true,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        Column(
            modifier = modifier
                .width(151.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(12.dp),
                    clip = false,
                )
                .background(
                    color = grey000(),
                    shape = RoundedCornerShape(12.dp),
                )
                .border(
                    width = 1.dp,
                    color = grey200(),
                    shape = RoundedCornerShape(12.dp),
                )
                .padding(
                    horizontal = 4.dp,
                    vertical = 6.dp,
                ),
        ) {
            CommunityFilterMenuItem(
                text = CommunityCategory.ALL.label,
                isSelected = selectedCategory == CommunityCategory.ALL,
                onClick = {
                    onCategorySelected(CommunityCategory.ALL)
                    onDismissRequest()
                },
            )

            CommunityFilterMenuItem(
                text = CommunityCategory.UNREAD.label,
                isSelected = selectedCategory == CommunityCategory.UNREAD,
                onClick = {
                    onCategorySelected(CommunityCategory.UNREAD)
                    onDismissRequest()
                },
            )

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .height(1.dp)
                    .background(grey100()),
            )

            Spacer(modifier = Modifier.height(4.dp))

            CommunityFilterMenuItem(
                text = CommunityCategory.PART_NOTICE.label,
                isSelected = selectedCategory == CommunityCategory.PART_NOTICE,
                onClick = {
                    onCategorySelected(CommunityCategory.PART_NOTICE)
                    onDismissRequest()
                },
            )

            CommunityFilterMenuItem(
                text = CommunityCategory.STUDY.label,
                isSelected = selectedCategory == CommunityCategory.STUDY,
                onClick = {
                    onCategorySelected(CommunityCategory.STUDY)
                    onDismissRequest()
                },
            )

            CommunityFilterMenuItem(
                text = CommunityCategory.QUESTION.label,
                isSelected = selectedCategory == CommunityCategory.QUESTION,
                onClick = {
                    onCategorySelected(CommunityCategory.QUESTION)
                    onDismissRequest()
                },
            )

            CommunityFilterMenuItem(
                text = CommunityCategory.FREE.label,
                isSelected = selectedCategory == CommunityCategory.FREE,
                onClick = {
                    onCategorySelected(CommunityCategory.FREE)
                    onDismissRequest()
                },
            )
        }
    }
}

@Composable
private fun CommunityFilterMenuItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        UText(
            text = text,
            style = UmcTypographyTokens.Subheadline,
            color = grey950(),
        )

        if (isSelected) {
            Icon(
                painter = painterResource(
                    id = R.drawable.ic_indigo_check,
                ),
                contentDescription = "선택됨",
                tint = indigo500(),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .size(18.dp),
            )
        }
    }
}