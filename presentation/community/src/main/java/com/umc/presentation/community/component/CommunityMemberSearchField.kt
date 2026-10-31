package com.umc.presentation.community.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.R
import com.umc.component.component.UTextField
import com.umc.component.theme.grey50
import com.umc.component.theme.grey100
import com.umc.component.theme.grey500
import com.umc.component.theme.grey900

@Composable
internal fun CommunityMemberSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "이름, 닉네임, 파트로 검색",
    fieldHeight: Dp = 48.dp,
    showClearButton: Boolean = false,
) {
    UTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = placeholder,
        modifier = modifier.fillMaxWidth().height(fieldHeight),
        singleLine = true,
        textStyle = if (showClearButton) UmcTypographyTokens.Body else UmcTypographyTokens.Callout,
        prevIcon = if (!showClearButton || value.isEmpty()) painterResource(R.drawable.ic_search) else null,
        prevIconTint = grey500(),
        prevIconSize = 24.dp,
        nextIcon = if (showClearButton && value.isNotEmpty()) painterResource(R.drawable.ic_clear_circle) else null,
        onClickNextIcon = if (showClearButton && value.isNotEmpty()) ({ onValueChange("") }) else null,
        backgroundColor = grey100(),
        focusBackgroundColor = grey50(),
        strokeColor = grey100(),
        focusStrokeColor = grey900(),
        cornerRadius = 8.dp,
    )
}
