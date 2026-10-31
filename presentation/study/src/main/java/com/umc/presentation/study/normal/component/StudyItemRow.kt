package com.umc.presentation.study.normal.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.umc.component.component.UText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey50
import com.umc.component.theme.grey200
import com.umc.component.theme.grey300
import com.umc.component.theme.grey500
import com.umc.component.theme.grey800
import com.umc.presentation.study.normal.NormalStudyItemUiModel

/**
 * 주차별 스터디 커리큘럼 항목을 표시하는 컴포넌트
 *
 * 주요 기능
 * - 주차별 타임라인(주차 번호) 표시
 * - Week 태그와 커리큘럼 제목·설명 표시
 * - 미공개(잠긴) 주차는 비활성 상태로 흐리게 표시
 */
@Composable
fun StudyItemRow(
    item: NormalStudyItemUiModel,
    modifier: Modifier = Modifier,
) {
    /**
     * 잠긴 주차는 비활성 상태임을 시각적으로 표현하기 위해
     * 전체 콘텐츠의 투명도를 낮춥니다.
     */
    val alpha = if (item.isLocked) 0.35f else 1f

    Row(
        modifier = modifier
            .fillMaxWidth()

            /**
             * 왼쪽 타임라인 높이를
             * 오른쪽 카드 콘텐츠 높이에 맞추기 위해 사용합니다.
             */
            .height(IntrinsicSize.Min)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top,
    ) {

        /**
         * 왼쪽 주차별 타임라인 영역
         *
         * 주차 번호를 표시하고, 아래 주차와 연결되는 세로선을 함께 표시합니다.
         */
        // 좌우 여백은 화면(UserStudyScreen)의 16dp 하나로만 둔다
        Column(
            modifier = Modifier
                .width(32.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(
                modifier = Modifier.height(16.dp),
            )

            // 주차 번호
            StudyTimelineColumn(
                week = item.week,
                isLocked = item.isLocked,
                modifier = Modifier.alpha(alpha),
            )

            Spacer(
                modifier = Modifier.height(4.dp),
            )

            // 다음 주차와 이어지는 타임라인 세로선
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .weight(1f)
                    .background(grey300()),
            )
        }

        Spacer(
            modifier = Modifier.width(12.dp),
        )

        /**
         * 오른쪽 커리큘럼 상세 카드 영역
         */
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(8.dp),
            color = grey50(),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier
                    // 잠긴 항목의 카드 내용도 동일하게 흐리게 표시
                    .alpha(alpha)
                    .padding(16.dp),
            ) {
                // Week 태그
                StudyTagChip(
                    text = AppStrings.STUDY_WEEK_FORMAT.format(item.week),
                )

                Spacer(
                    modifier = Modifier.height(10.dp),
                )

                // 해당 주차 커리큘럼 제목
                UText(
                    text = item.title,
                    style = UmcTypographyTokens.HeadlineBold,
                    color = grey800(),
                    maxLines = 2,
                )

                // 커리큘럼 설명. 미공개(잠긴) 주차는 제목만 보여주고 설명은 숨긴다.
                if (!item.isLocked && item.description.isNotBlank()) {
                    Spacer(
                        modifier = Modifier.height(8.dp),
                    )

                    UText(
                        text = item.description,
                        style = UmcTypographyTokens.Footnote,
                        color = grey500(),
                    )
                }
            }
        }
    }
}
