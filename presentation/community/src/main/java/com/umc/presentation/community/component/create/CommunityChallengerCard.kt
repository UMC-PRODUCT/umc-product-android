package com.umc.presentation.community.component.create

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey300
import com.umc.component.theme.grey400
import com.umc.component.theme.grey800
import com.umc.presentation.community.model.CommunityChallengerUiModel
import kotlinx.collections.immutable.ImmutableList
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.collections.immutable.persistentListOf

/**
 * 스레드 생성 화면에서 선택된 챌린저 정보를 표시하는 카드
 *
 * 선택된 인원에 따라 이름 또는 "OOO 외 N명" 형식으로 표시하며,
 * 클릭 시 챌린저 선택 BottomSheet를 엽니다.
 */
@Composable
fun CommunityChallengerCard(
    challengers: ImmutableList<CommunityChallengerUiModel>,
    maxCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    currentCount: Int = challengers.size,
) {
    val interactionSource = remember {
        MutableInteractionSource()
    }

    // 선택된 챌린저 수에 따라 카드에 표시할 문구 구성
    val challengerText = when {
        challengers.isEmpty() -> {
            "챌린저를 선택하세요"
        }

        challengers.size == 1 -> {
            challengers.first().name
        }

        else -> {
            "${challengers.first().name} 외 ${challengers.size - 1}명"
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        color = grey000(),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(
            width = 1.dp,
            color = grey300(),
        ),
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 14.dp,
                    end = 12.dp,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UText(
                text = challengerText,
                style = UmcTypographyTokens.Body,
                color = if (challengers.isEmpty()) {
                    grey400()
                } else {
                    grey800()
                },
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Icon(
                painter = painterResource(
                    id = R.drawable.ic_next,
                ),
                contentDescription = "챌린저 선택",
                tint = grey400(),
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

/**
 * 선택 인원별(미선택/1명/여러 명) 표시 문구 확인용 Preview
 */
@Preview(showBackground = true, widthDp = 360)
@Composable
private fun CommunityChallengerCardPreview() {
    val challengers = persistentListOf(
        CommunityChallengerUiModel(
            memberId = 1L,
            name = "김챌린",
            nickname = "챌린",
            school = "UMC대학교",
            generation = 9L,
            partLabel = "Android",
        ),
        CommunityChallengerUiModel(
            memberId = 2L,
            name = "이유엠",
            nickname = "유엠",
            school = "UMC대학교",
            generation = 9L,
            partLabel = "iOS",
        ),
        CommunityChallengerUiModel(
            memberId = 3L,
            name = "박씨",
            nickname = "",
            school = "UMC대학교",
            generation = 9L,
            partLabel = "Web",
        ),
    )

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CommunityChallengerCard(
            challengers = persistentListOf(),
            maxCount = 10,
            onClick = {},
        )

        CommunityChallengerCard(
            challengers = persistentListOf(challengers.first()),
            maxCount = 10,
            onClick = {},
        )

        CommunityChallengerCard(
            challengers = challengers,
            maxCount = 10,
            onClick = {},
        )
    }
}
