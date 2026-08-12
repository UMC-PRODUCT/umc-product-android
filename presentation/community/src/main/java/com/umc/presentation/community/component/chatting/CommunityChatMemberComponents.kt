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
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.green100
import com.umc.component.theme.green700
import com.umc.component.theme.grey100
import com.umc.component.theme.grey200
import com.umc.component.theme.grey600
import com.umc.component.theme.indigo100
import com.umc.component.theme.indigo600
import com.umc.component.theme.red100
import com.umc.component.theme.red600
import com.umc.component.theme.white
import com.umc.component.theme.yellow100
import com.umc.component.theme.yellow500
import com.umc.domain.model.enums.UserPart

@Composable
internal fun CommunityChatProfileImage(imageUrl: String?) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(white(), CircleShape)
            .border(1.dp, grey200(), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            modifier = Modifier.fillMaxWidth(),
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
) {
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(background)
            .padding(horizontal = 7.dp, vertical = 4.dp),
        color = foreground,
        fontSize = 10.sp,
        style = UmcTypographyTokens.Caption1Bold,
    )
}

@Composable
internal fun communityChatPartTag(part: UserPart): Triple<String, Color, Color> = when (part) {
    UserPart.IOS -> Triple("iOS", yellow100(), yellow500())
    UserPart.ANDROID -> Triple("Android", green100(), green700())
    UserPart.PLAN -> Triple("PM", indigo100(), indigo600())
    UserPart.DESIGN -> Triple("Design", red100(), red600())
    UserPart.WEB -> Triple("Web", indigo100(), indigo600())
    UserPart.NODEJS -> Triple("Node.js", green100(), green700())
    UserPart.SPRINGBOOT -> Triple("Spring", green100(), green700())
    UserPart.ADMIN,
    UserPart.UNKNOWN,
        -> Triple(part.label, grey100(), grey600())
}
