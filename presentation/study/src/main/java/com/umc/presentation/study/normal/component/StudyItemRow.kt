package com.umc.presentation.study.normal.component

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey200
import com.umc.component.theme.grey500
import com.umc.component.theme.grey800
import com.umc.presentation.study.normal.NormalStudyItemUiModel

/**
 * 주차별 스터디 커리큘럼 항목을 표시하는 컴포넌트
 *
 * 주요 기능
 * - 주차별 타임라인 표시
 * - Week / 플랫폼 태그 표시
 * - 스터디 제목 및 제출 상태 표시
 * - BEST 여부 표시
 * - 잠긴 주차 비활성화 처리
 * - 항목 클릭 시 상세 내용 펼치기/접기
 * - PASS / FAIL 상태의 피드백 결과 표시
 *
 * 잠긴 항목은 투명도를 낮춰 비활성 상태로 표시하며,
 * 클릭 및 상세 내용 펼치기가 제한됩니다.
 */
@Composable
fun StudyItemRow(
    item: NormalStudyItemUiModel,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    /**
     * 잠긴 주차는 비활성 상태임을 시각적으로 표현하기 위해
     * 전체 콘텐츠의 투명도를 낮춥니다.
     */
    val alpha = if (item.isLocked) 0.35f else 1f

    /**
     * 클릭 시 기본 Ripple 효과를 제거하고
     * interaction 상태만 관리하기 위해 사용합니다.
     */
    val interactionSource = remember {
        MutableInteractionSource()
    }

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
         * 주차 번호와 현재 상태를 표시하고,
         * 아래 주차와 연결되는 세로선을 함께 표시합니다.
         */
        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .width(32.dp)
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(
                modifier = Modifier.height(16.dp),
            )

            // 주차 번호 및 PASS / FAIL / 진행 중 상태 표시
            StudyTimelineColumn(
                week = item.week,
                status = item.status,
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
                    .background(grey200()),
            )
        }

        Spacer(
            modifier = Modifier.width(12.dp),
        )

        /**
         * 오른쪽 스터디 상세 카드 영역
         *
         * 잠긴 항목은 클릭할 수 없으며,
         * 활성화된 항목을 클릭하면 펼침 상태를 변경합니다.
         */
        Surface(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
                .clickable(
                    enabled = !item.isLocked,
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onToggle,
                )

                /**
                 * 상세 내용이 펼쳐지거나 접힐 때
                 * 카드 높이 변화가 자연스럽게 보이도록 애니메이션 적용
                 */
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    )
                ),
            shape = RoundedCornerShape(8.dp),
            color = grey000(),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier
                    // 잠긴 항목의 카드 내용도 동일하게 흐리게 표시
                    .alpha(alpha)
                    .padding(16.dp),
            ) {

                /**
                 * 카드 상단 영역
                 *
                 * 왼쪽:
                 * - Week 태그
                 * - 플랫폼 태그
                 * - 스터디 제목
                 *
                 * 오른쪽:
                 * - BEST 배지
                 * - 제출 상태 배지
                 * - 펼치기/접기 아이콘
                 */
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                    ) {

                        // Week 및 플랫폼 태그 영역
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            StudyTagChip(
                                text = AppStrings.STUDY_WEEK_FORMAT.format(
                                    item.week
                                ),
                            )

                            Spacer(
                                modifier = Modifier.width(6.dp),
                            )

                            StudyTagChip(
                                text = item.platform,
                            )
                        }

                        Spacer(
                            modifier = Modifier.height(10.dp),
                        )

                        // 해당 주차 스터디 제목
                        UText(
                            text = item.title,
                            style = UmcTypographyTokens.HeadlineBold,
                            color = grey800(),
                            maxLines = 2,
                        )
                    }

                    Spacer(
                        modifier = Modifier.width(8.dp),
                    )

                    /**
                     * 상태 및 배지 영역
                     */
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // BEST 워크북인 경우 BEST 배지 표시
                        if (item.isBest) {
                            StudyBestBadge()

                            Spacer(
                                modifier = Modifier.width(6.dp),
                            )
                        }

                        /**
                         * 잠긴 항목은 상태 배지와
                         * 펼치기/접기 아이콘을 표시하지 않습니다.
                         */
                        if (!item.isLocked) {
                            // PASS / FAIL / 진행 중 상태 표시
                            StudyStatusBadge(
                                item = item,
                            )

                            Spacer(
                                modifier = Modifier.width(8.dp),
                            )

                            // 현재 펼침 상태에 따라 화살표 방향 변경
                            Icon(
                                painter = painterResource(
                                    if (item.isExpanded) {
                                        R.drawable.ic_dropdown_up
                                    } else {
                                        R.drawable.ic_dropdown_down
                                    }
                                ),
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = grey500(),
                            )
                        }
                    }
                }

                /**
                 * 펼쳐진 상세 영역
                 *
                 * 잠기지 않은 항목이 펼쳐진 경우에만 표시합니다.
                 */
                if (
                    item.isExpanded &&
                    !item.isLocked
                ) {
                    Spacer(
                        modifier = Modifier.height(12.dp),
                    )

                    // 스터디 설명이 존재할 때만 상세 설명 표시
                    if (item.description.isNotBlank()) {
                        UText(
                            text = item.description,
                            style = UmcTypographyTokens.Footnote,
                            color = grey500(),
                        )
                    }

                    /**
                     * 제출 결과가 PASS 또는 FAIL인 경우
                     * 서버에서 받은 피드백 또는 기본 상태 문구를 표시합니다.
                     *
                     * IN_PROGRESS 상태에서는 결과 배너를 표시하지 않습니다.
                     */
                    if (
                        item.status ==
                        com.umc.domain.model.enums.StudyStatus.PASS ||
                        item.status ==
                        com.umc.domain.model.enums.StudyStatus.FAIL
                    ) {
                        Spacer(
                            modifier = Modifier.height(12.dp),
                        )

                        StudyExpandedContent(
                            item = item,
                        )
                    }
                }
            }
        }
    }
}