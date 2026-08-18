package com.umc.presentation.study.admin.group.component

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.umc.component.R

/**
 * 관리자 스터디 그룹에서 사용하는 공통 프로필 이미지
 *
 * 프로필 이미지 URL이 존재하면 서버 이미지를 표시하고,
 * 없는 경우 기본 프로필 아이콘을 표시합니다.
 */
@Composable
fun AdminStudyGroupProfileImage(
    profileImageUrl: String?,
    size: Int,
) {
    if (!profileImageUrl.isNullOrBlank()) {
        AsyncImage(
            model = profileImageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(size.dp)
                .clip(CircleShape),
        )
    } else {
        Icon(
            painter = painterResource(
                R.drawable.ic_profile_default
            ),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(size.dp),
        )
    }
}