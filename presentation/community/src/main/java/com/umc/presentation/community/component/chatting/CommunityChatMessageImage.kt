package com.umc.presentation.community.component.chatting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.umc.component.R
import com.umc.component.theme.AppStrings
import com.umc.component.theme.black
import com.umc.component.theme.grey200
import com.umc.component.theme.grey950
import com.umc.component.theme.main100
import com.umc.component.theme.main500

/** The transfer indicator remains until delivery even if the local thumbnail has loaded. */
@Composable
internal fun CommunityChatMessageImage(
    model: String,
    modifier: Modifier = Modifier,
    isSending: Boolean = false,
    transferFailed: Boolean = false,
    showLoadingIndicator: Boolean = true,
    cornerRadius: Dp = 12.dp,
) {
    var retryGeneration by remember(model) { mutableIntStateOf(0) }
    var loading by remember(model, retryGeneration) { mutableStateOf(true) }
    var loadFailed by remember(model, retryGeneration) { mutableStateOf(false) }
    Box(
        modifier = modifier.clip(RoundedCornerShape(cornerRadius)).background(grey200()),
        contentAlignment = Alignment.Center,
    ) {
        key(model, retryGeneration) {
            AsyncImage(
                model = model,
                contentDescription = AppStrings.CHAT_CD_IMAGE,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                onLoading = { loading = true; loadFailed = false },
                onSuccess = { loading = false; loadFailed = false },
                onError = { loading = false; loadFailed = true },
            )
        }
        if (!transferFailed && showLoadingIndicator && (isSending || loading)) {
            CommunityChatImageLoadingIndicator()
        } else if (!transferFailed && loadFailed) {
            IconButton(onClick = { retryGeneration++ }) {
                Icon(
                    painter = painterResource(R.drawable.ic_refresh),
                    contentDescription = AppStrings.CHAT_CD_RELOAD_IMAGE,
                    tint = grey950(),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
internal fun CommunityChatImageLoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(64.dp).background(black().copy(alpha = 0.2f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(40.dp),
            color = main500(),
            trackColor = main100(),
            strokeWidth = 4.dp,
        )
    }
}
