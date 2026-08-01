package com.umc.presentation.community.component.create

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.umc.component.component.UText
import com.umc.component.component.UTextField
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey500
import com.umc.component.theme.grey950
import androidx.compose.foundation.text.KeyboardOptions

@Composable
fun CommunityThreadForm(
    title: String,
    description: String,
    onTitleChanged: (String) -> Unit,
    onDescriptionChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        UText(
            text = "제목",
            style = UmcTypographyTokens.HeadlineBold,
            color = grey950(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        UTextField(
            value = title,
            onValueChange = onTitleChanged,
            placeholder = "제목을 입력하세요",
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
            ),
        )

        Spacer(modifier = Modifier.height(32.dp))

        UText(
            text = "스레드 특징",
            style = UmcTypographyTokens.HeadlineBold,
            color = grey950(),
        )

        Spacer(modifier = Modifier.height(8.dp))

        UTextField(
            value = description,
            onValueChange = onDescriptionChanged,
            placeholder = "상세 내용을 입력하세요",
            modifier = Modifier
                .fillMaxWidth()
                .height(144.dp),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Default,
            ),
            verticalAlignment = androidx.compose.ui.Alignment.Top,
        )
    }
}