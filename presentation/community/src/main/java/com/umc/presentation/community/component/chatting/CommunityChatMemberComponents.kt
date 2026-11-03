package com.umc.presentation.community.component.chatting

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.umc.component.R
import com.umc.component.theme.grey200
import com.umc.component.theme.white
import com.umc.component.component.UPartChipColors
import com.umc.component.component.partChipColors
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.domain.model.enums.UserPart

@Composable
internal fun CommunityChatProfileImage(imageUrl: String?, useThreadStyle: Boolean = false) {
    Box(
        modifier = Modifier
            .size(if (useThreadStyle) 32.dp else 34.dp)
            .clip(CircleShape)
            .background(white(), CircleShape)
            .border(1.dp, grey200(), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            modifier = if (useThreadStyle) Modifier.size(20.dp) else Modifier.fillMaxWidth(),
            painter = painterResource(R.drawable.ic_person),
            contentDescription = null,
            contentScale = ContentScale.Fit,
        )
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = AppStrings.CHAT_CD_PROFILE_IMAGE,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
internal fun CommunityChatMemberTag(
    text: String,
    background: Color,
    foreground: Color,
    border: Color? = null, //1dp 테두리. null 이면 그리지 않는다
) {
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(background)
            .then(if (border != null) Modifier.border(1.dp, border, RoundedCornerShape(4.dp)) else Modifier)
            .padding(horizontal = 7.dp, vertical = 4.dp),
        color = foreground,
        fontSize = 10.sp,
        style = UmcTypographyTokens.Caption1Bold,
    )
}

/** 참여자 파트 태그의 이름과 색 */
internal data class CommunityChatPartTag(
    val label: String,
    val colors: UPartChipColors,
)

@Composable
internal fun communityChatPartTag(part: UserPart): CommunityChatPartTag {
    // 라벨은 UserPart 하나만 쓰고, 색은 파트 칩(UInfoChip)과 같은 정의를 쓴다
    return CommunityChatPartTag(label = part.label, colors = partChipColors(part))
}
