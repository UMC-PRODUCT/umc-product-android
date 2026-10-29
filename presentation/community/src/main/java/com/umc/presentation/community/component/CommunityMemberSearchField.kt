package com.umc.presentation.community.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.umc.component.R
import com.umc.component.component.UTextField
import com.umc.component.theme.grey000
import com.umc.component.theme.grey100
import com.umc.component.theme.grey500
import com.umc.component.theme.grey900

@Composable
internal fun CommunityMemberSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    UTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = "이름, 닉네임, 파트로 검색",
        modifier = modifier.fillMaxWidth().height(48.dp),
        prevIcon = painterResource(R.drawable.ic_search),
        prevIconTint = grey500(),
        prevIconSize = 24.dp,
        backgroundColor = grey100(),
        focusBackgroundColor = grey000(),
        strokeColor = grey100(),
        focusStrokeColor = grey900(),
        cornerRadius = 8.dp,
    )
}
