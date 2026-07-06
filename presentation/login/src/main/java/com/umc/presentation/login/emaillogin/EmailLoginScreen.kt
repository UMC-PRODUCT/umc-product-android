package com.umc.presentation.login.emaillogin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.umc.component.R
import com.umc.component.component.UButton
import com.umc.component.component.UText
import com.umc.component.component.UTextField
import com.umc.component.theme.AppStrings
import com.umc.component.theme.UmcTypographyTokens
import com.umc.component.theme.grey000
import com.umc.component.theme.grey100
import com.umc.component.theme.grey200
import com.umc.component.theme.grey300
import com.umc.component.theme.grey500
import com.umc.component.theme.grey900
import com.umc.component.theme.grey950
import com.umc.component.theme.indigo500
import com.umc.component.theme.red100
import com.umc.component.theme.red500

@Composable
fun EmailLoginRoute(
    viewModel: EmailLoginViewModel = hiltViewModel(),
    navigateToBack: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    EmailLoginScreen(
        uiState = uiState,
        onClickBack = navigateToBack,
        onEmailChanged = viewModel::updateEmail,
        onPasswordChanged = viewModel::updatePassword,
        onClickPasswordVisible = viewModel::togglePasswordVisible,
        onClickLogin = viewModel::login,
    )
}

@Composable
fun EmailLoginScreen(
    uiState: EmailLoginState = EmailLoginState(),
    onClickBack: () -> Unit = {},
    onEmailChanged: (String) -> Unit = {},
    onPasswordChanged: (String) -> Unit = {},
    onClickPasswordVisible: () -> Unit = {},
    onClickLogin: () -> Unit = {},
) {
    val focusManager = LocalFocusManager.current

    // 이메일 필드 포커스 + 입력값 존재 시에만 지우기 아이콘 노출
    val emailInteractionSource = remember { MutableInteractionSource() }
    val isEmailFocused by emailInteractionSource.collectIsFocusedAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(grey000()),
    ) {
        Icon(
            modifier = Modifier
                .padding(start = 4.dp, top = 8.dp)
                .padding(12.dp)
                .clickable { onClickBack() },
            painter = painterResource(id = R.drawable.ic_back),
            contentDescription = null,
            tint = Color.Unspecified,
        )

        UText(
            text = AppStrings.LOGIN_UMC_ACCOUNT,
            style = UmcTypographyTokens.Title1Bold,
            color = grey950(),
            modifier = Modifier.padding(start = 24.dp, top = 16.dp),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            FieldLabel(text = AppStrings.EMAIL)

            Spacer(modifier = Modifier.height(8.dp))

            UTextField(
                value = uiState.email,
                onValueChange = onEmailChanged,
                placeholder = AppStrings.EMAIL_LOGIN_EMAIL_PLACEHOLDER,
                textStyle = UmcTypographyTokens.Headline,
                textColor = if (uiState.isLoginFailed) red500() else grey950(),
                backgroundColor = if (uiState.isLoginFailed) red100() else grey000(),
                strokeColor = if (uiState.isLoginFailed) red500() else grey200(),
                focusStrokeColor = if (uiState.isLoginFailed) red500() else grey900(),
                cornerRadius = 10.dp,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                ),
                nextIcon = if (isEmailFocused && uiState.email.isNotEmpty()) {
                    painterResource(id = R.drawable.ic_delete_filled)
                } else {
                    null
                },
                nextIconTint = grey300(),
                onClickNextIcon = { onEmailChanged("") },
                interactionSource = emailInteractionSource,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(24.dp))

            FieldLabel(text = AppStrings.PASSWORD)

            Spacer(modifier = Modifier.height(8.dp))

            UTextField(
                value = uiState.password,
                onValueChange = onPasswordChanged,
                placeholder = AppStrings.EMAIL_LOGIN_PASSWORD_PLACEHOLDER,
                textStyle = UmcTypographyTokens.Headline,
                textColor = if (uiState.isLoginFailed) red500() else grey950(),
                backgroundColor = if (uiState.isLoginFailed) red100() else grey000(),
                strokeColor = if (uiState.isLoginFailed) red500() else grey200(),
                focusStrokeColor = if (uiState.isLoginFailed) red500() else grey900(),
                cornerRadius = 10.dp,
                visualTransformation = if (uiState.isPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation(mask = '*')
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() },
                ),
                nextIcon = painterResource(
                    id = if (uiState.isPasswordVisible) {
                        R.drawable.ic_password_show
                    } else {
                        R.drawable.ic_password_hide
                    }
                ),
                nextIconTint = grey500(),
                onClickNextIcon = onClickPasswordVisible,
                modifier = Modifier.fillMaxWidth(),
            )

            if (uiState.isLoginFailed) {
                Spacer(modifier = Modifier.height(8.dp))

                UText(
                    text = AppStrings.EMAIL_LOGIN_FAIL_MESSAGE,
                    style = UmcTypographyTokens.Footnote,
                    color = red500(),
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            UText(
                text = AppStrings.EMAIL_LOGIN_FIND_PASSWORD,
                style = UmcTypographyTokens.Footnote,
                color = grey500(),
                modifier = Modifier.align(Alignment.End),
            )

            Spacer(modifier = Modifier.weight(1f))

            UButton(
                text = AppStrings.LOGIN,
                onClick = onClickLogin,
                enabled = uiState.enableLoginButton,
                backgroundColor = if (uiState.enableLoginButton) indigo500() else grey100(),
                textColor = if (uiState.enableLoginButton) grey000() else grey300(),
                textStyle = UmcTypographyTokens.HeadlineBold,
                cornerRadius = 10.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FieldLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        UText(
            text = text,
            style = UmcTypographyTokens.HeadlineBold,
            color = grey950(),
        )
        UText(
            text = " *",
            style = UmcTypographyTokens.HeadlineBold,
            color = red500(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmailLoginScreenPreview() {
    EmailLoginScreen()
}

@Preview(showBackground = true)
@Composable
private fun EmailLoginScreenFailPreview() {
    EmailLoginScreen(
        uiState = EmailLoginState(
            email = "example@univ.ac.kr",
            password = "umc12341234",
            isLoginFailed = true,
        ),
    )
}
