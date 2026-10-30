package com.umc.presentation.community.component.create

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.umc.component.component.UText
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey200
import com.umc.component.theme.grey400
import com.umc.component.theme.grey700
import com.umc.component.theme.grey900
import com.umc.component.theme.white
import com.umc.presentation.community.R
import com.umc.presentation.community.component.CommunityCategoryChip
import com.umc.presentation.community.component.CommunityCategoryIcon
import com.umc.presentation.community.model.CommunityCategory

/**
 * 스레드 생성 화면의 미리보기 섹션
 *
 * Figma `스레드/미리보기`를 구현합니다.
 * 제목이나 특징이 비어 있으면 grey400 플레이스홀더("스레드 제목", "특징")를 표시하고,
 * 입력되면 제목은 grey900, 특징은 grey700으로 한 줄까지 표시합니다.
 *
 * @param title 입력된 스레드 제목
 * @param description 입력된 스레드 특징
 * @param category 표시할 카테고리. null이면 카테고리 칩을 숨깁니다.
 * @param iconRes 카테고리 아이콘 리소스
 */
@Composable
fun CommunityThreadPreview(
    title: String,
    description: String,
    category: CommunityCategory?,
    @DrawableRes iconRes: Int,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(8.dp)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(white())
            .border(
                width = 1.dp,
                color = grey200(),
                shape = shape,
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CommunityCategoryIcon(
            iconRes = iconRes,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // 제목이 길어도 카테고리 칩이 밀려나지 않도록 남은 폭까지만 차지
                UText(
                    text = title.ifBlank { "스레드 제목" },
                    style = UmcTypographyTokens.HeadlineBold,
                    color = if (title.isBlank()) grey400() else grey900(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )

                if (category != null) {
                    CommunityCategoryChip(
                        category = category,
                    )
                }
            }

            UText(
                text = description.ifBlank { "특징" },
                style = UmcTypographyTokens.Subheadline,
                color = if (description.isBlank()) grey400() else grey700(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * 스레드 미리보기 입력 전 상태 Preview
 */
@Preview(showBackground = true, widthDp = 412)
@Composable
private fun CommunityThreadPreviewEmptyPreview() {
    CommunityThreadPreview(
        title = "",
        description = "",
        category = CommunityCategory.STUDY,
        iconRes = R.drawable.ic_category_study,
        modifier = Modifier.padding(24.dp),
    )
}

/**
 * 스레드 미리보기 입력 후 상태 Preview
 */
@Preview(showBackground = true, widthDp = 412)
@Composable
private fun CommunityThreadPreviewFilledPreview() {
    CommunityThreadPreview(
        title = "iOS 3주차 과제 인증방",
        description = "iOS 스터디원들끼리 3주차 과제를 인증하고 서로 코드 피드백을 주고받는 방이에요.",
        category = CommunityCategory.STUDY,
        iconRes = R.drawable.ic_category_study,
        modifier = Modifier.padding(24.dp),
    )
}
