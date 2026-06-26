package com.umc.failcode.code

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.component.UDialog
import com.umc.component.component.UText
import com.umc.component.component.UTextField
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.neutral000
import com.umc.component.theme.neutral600
import com.umc.component.theme.neutral800
import com.umc.component.theme.primary500
import com.umc.component.theme.primary700
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SignUpFailCodeRoute(
    viewModel: SignUpFailCodeViewModel = hiltViewModel(),
    navigateToBack: () -> Unit = {},
    navigateToHome: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                SignUpFailCodeEvent.MoveToBack -> navigateToBack()
                SignUpFailCodeEvent.MoveToHome -> navigateToHome()
                is SignUpFailCodeEvent.ShowErrorDialog -> errorMessage = event.message
            }
        }
    }

    errorMessage?.let { message ->
        UDialog(
            title = message,
            confirmText = AppStrings.CONFIRM,
            onDismissRequest = { errorMessage = null },
            onConfirm = { errorMessage = null },
        )
    }

    SignUpFailCodeScreen(
        uiState = uiState,
        onClickBack = viewModel::onClickBack,
        onCodeChanged = viewModel::onCodeChanged,
        onClickRegister = viewModel::register,
    )
}

@Composable
fun SignUpFailCodeScreen(
    uiState: SignUpFailCodeState = SignUpFailCodeState(),
    onClickBack: () -> Unit = {},
    onCodeChanged: (String) -> Unit = {},
    onClickRegister: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(neutral000()),
    ) {
        Icon(
            modifier = Modifier
                .padding(start = 4.dp, top = 8.dp)
                .padding(12.dp)
                .clickable { onClickBack() },
            painter = painterResource(R.drawable.ic_back),
            contentDescription = null,
            tint = Color.Unspecified,
        )

        UText(
            text = AppStrings.SIGN_UP_FAIL_PREV_USER,
            style = UmcTypographyTokens.Title1Bold,
            color = neutral800(),
            modifier = Modifier.padding(start = 24.dp, top = 16.dp),
        )

        UText(
            text = AppStrings.SIGN_UP_FAIL_PREV_USER_HINT,
            style = UmcTypographyTokens.Body,
            color = neutral600(),
            modifier = Modifier.padding(start = 24.dp, top = 16.dp),
        )

        UText(
            text = AppStrings.SIGN_UP_FAIL_CODE_LABEL,
            style = UmcTypographyTokens.HeadlineBold,
            color = neutral800(),
            modifier = Modifier.padding(start = 24.dp, top = 80.dp),
        )

        UTextField(
            value = uiState.code,
            onValueChange = { if (it.length <= 6) onCodeChanged(it) },
            placeholder = AppStrings.SIGN_UP_FAIL_CODE_PLACEHOLDER,
            textStyle = UmcTypographyTokens.Body,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp),
        )

        Spacer(modifier = Modifier.weight(1f))

        UButton(
            text = AppStrings.SIGN_UP_FAIL_CODE_SUBMIT,
            onClick = onClickRegister,
            backgroundColor = primary500(),
            pressedColor = primary700(),
            textColor = neutral000(),
            textStyle = UmcTypographyTokens.HeadlineBold,
            contentPadding = PaddingValues(vertical = 16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SignUpFailCodeScreenPreview() {
    SignUpFailCodeScreen()
}

@Preview(showBackground = true)
@Composable
private fun SignUpFailCodeScreenFilledPreview() {
    SignUpFailCodeScreen(uiState = SignUpFailCodeState(code = "123456"))
}
