package com.umc.presentation.community.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.umc.component.theme.grey100
import com.umc.component.theme.grey200

/**
 * 커뮤니티 스레드 목록 로딩 중 표시하는 Skeleton Item
 *
 * 실제 스레드 카드와 비슷한 형태를 만들고
 * alpha 값을 반복적으로 변경하여 로딩 애니메이션을 표현합니다.
 */
@Composable
fun CommunitySkeletonItem(
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "")

    // Skeleton 전체의 투명도를 반복적으로 변경
    val alpha = transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = grey200(),
                shape = RoundedCornerShape(10.dp),
            )
            .padding(16.dp)
            .alpha(alpha.value),
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {

            // 왼쪽 원형 썸네일
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = grey100(),
                        shape = CircleShape,
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                // 제목 영역
                Box(
                    modifier = Modifier
                        .width(150.dp)
                        .height(13.dp)
                        .background(
                            grey100(),
                            RoundedCornerShape(999.dp),
                        )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 내용 미리보기 영역
                Box(
                    modifier = Modifier
                        .width(220.dp)
                        .height(11.dp)
                        .background(
                            grey100(),
                            RoundedCornerShape(999.dp),
                        )
                )
            }
        }
    }
}