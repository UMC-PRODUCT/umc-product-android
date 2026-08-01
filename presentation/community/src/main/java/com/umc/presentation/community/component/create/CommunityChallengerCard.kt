package com.umc.presentation.community.component.create

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.umc.component.theme.grey400
import com.umc.component.theme.grey600
import com.umc.component.theme.grey950
import com.umc.component.theme.indigo500
import com.umc.presentation.community.model.CommunityChallengerUiModel

@Composable
fun CommunityChallengerCard(
    challengers: List<CommunityChallengerUiModel>,
    maxCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember {
        MutableInteractionSource()
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        color = grey000(),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(
            width = 1.dp,
            color = indigo500(),
        ),
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                UText(
                    text = "추가할 챌린저",
                    style = UmcTypographyTokens.HeadlineBold,
                    color = grey950(),
                )


                UText(
                    text = "최대 ${maxCount}명까지 추가할 수 있습니다",
                    style = UmcTypographyTokens.Footnote,
                    color = grey600(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(modifier = Modifier.size(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                UText(
                    text = "${challengers.size} / $maxCount",
                    style = UmcTypographyTokens.Footnote,
                    color = indigo500(),
                )

                Icon(
                    painter = painterResource(
                        id = R.drawable.ic_next,
                    ),
                    contentDescription = "챌린저 선택",
                    tint = grey400(),
                    modifier = Modifier.size( 32.dp),
                )
            }
        }
    }
}