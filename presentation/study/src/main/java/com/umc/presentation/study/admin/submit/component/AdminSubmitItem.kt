package com.umc.presentation.study.admin.submit.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.umc.component.R
import com.umc.component.component.UText
import com.umc.component.theme.*
import com.umc.component.theme.UmcTypographyTokens.BodyBold
import com.umc.component.theme.UmcTypographyTokens.Caption1
import com.umc.component.theme.UmcTypographyTokens.Caption1Bold
import com.umc.presentation.study.admin.submit.AdminSubmitItemUiModel

/**
 * 관리자 제출 현황 목록의 개별 챌린저 항목
 *
 * 사용자 정보와 스터디 제출 상태를 표시하며,
 * 클릭 시 해당 챌린저의 제출 상세 화면으로 이동합니다.
 *
 * 주요 표시 정보
 * - 프로필 이미지
 * - 이름 / 닉네임
 * - 파트
 * - 학교 / 스터디 제목
 * - Pass / Fail / Best 상태
 */
@Composable
fun AdminSubmitItem(
    item: AdminSubmitItemUiModel,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(74.dp)
            .background(grey000(), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 사용자 프로필 이미지
        AdminSubmitProfileImage(
            profileImageUrl = item.profileImageUrl,
        )

        Spacer(modifier = Modifier.width(10.dp))

        // 이름, 파트, 학교, 스터디 제목
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                UText(
                    text = "${item.name}(${item.nickname})",
                    style = BodyBold,
                    color = grey800()
                )

                Spacer(modifier = Modifier.width(8.dp))

                // 사용자 파트
                Box(
                    modifier = Modifier
                        .border(
                            1.dp,
                            grey200(),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(
                            horizontal = 8.dp,
                            vertical = 2.dp
                        )
                ) {
                    UText(
                        text = item.partLabel,
                        style = Caption1Bold,
                        color = grey700()
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 학교 및 제출한 스터디 제목
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                UText(
                    text = item.schoolName,
                    style = Caption1,
                    color = grey500()
                )

                UText(
                    text = " | ",
                    style = Caption1,
                    color = grey500()
                )

                UText(
                    text = item.studyTitle,
                    style = Caption1,
                    color = grey500(),
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 제출 결과 상태 뱃지
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // PASS 또는 BEST 상태
            if (
                item.markStatus == "PASS" ||
                item.isBest
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            green100(),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(
                            horizontal = 8.dp,
                            vertical = 4.dp
                        )
                ) {
                    UText(
                        text = "Pass",
                        style = Caption1Bold,
                        color = green500()
                    )
                }

                Spacer(modifier = Modifier.width(7.dp))
            }

            // FAIL 상태
            if (item.markStatus == "FAIL") {
                Box(
                    modifier = Modifier
                        .background(
                            red100(),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(
                            horizontal = 8.dp,
                            vertical = 4.dp
                        )
                ) {
                    UText(
                        text = "Fail",
                        style = Caption1Bold,
                        color = red700()
                    )
                }

                Spacer(modifier = Modifier.width(7.dp))
            }

            // BEST 워크북
            if (item.isBest) {
                Box(
                    modifier = Modifier
                        .background(
                            yellow100(),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(
                            horizontal = 8.dp,
                            vertical = 4.dp
                        )
                ) {
                    UText(
                        text = "Best",
                        style = Caption1Bold,
                        color = yellow500()
                    )
                }

                Spacer(modifier = Modifier.width(7.dp))
            }

            Spacer(modifier = Modifier.width(10.dp))

            // 제출 상세 화면 이동 아이콘
            Icon(
                painter = painterResource(
                    R.drawable.ic_arrow_next
                ),
                contentDescription = null,
                tint = grey500(),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

/**
 * 제출 현황에 표시할 사용자 프로필 이미지
 *
 * 프로필 이미지 URL이 존재하면 실제 이미지를 표시하고,
 * URL이 없으면 기본 프로필 아이콘을 표시합니다.
 */
@Composable
private fun AdminSubmitProfileImage(
    profileImageUrl: String?,
) {
    if (!profileImageUrl.isNullOrBlank()) {
        AsyncImage(
            model = profileImageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape),
        )
    } else {
        Icon(
            painter = painterResource(
                R.drawable.ic_profile_default
            ),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(32.dp),
        )
    }
}