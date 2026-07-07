package com.umc.presentation.login.emaillogin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.umc.component.component.UToastData
import com.umc.component.component.UToastHost
import com.umc.component.component.UToastState
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
import kotlinx.coroutines.flow.collectLatest

@Composable
fun EmailLoginRoute(
    viewModel: EmailLoginViewModel = hiltViewModel(),
    navigateToBack: () -> Unit = {},
    navigateToMain: () -> Unit = {},
    navigateToFindPassword: () -> Unit = {},
    navigateToInputCode: () -> Unit = {},
    navigateToSignUp: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var toastData by remember { mutableStateOf<UToastData?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is EmailLoginEvent.MoveToMainEvent -> navigateToMain()
                is EmailLoginEvent.MoveToInputCodeEvent -> navigateToInputCode()
                is EmailLoginEvent.ShowErrorToast ->
                    toastData = UToastData(event.message, UToastState.ERROR)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        EmailLoginScreen(
            uiState = uiState,
            onClickBack = navigateToBack,
            onEmailChanged = viewModel::updateEmail,
            onPasswordChanged = viewModel::updatePassword,
            onClickPasswordVisible = viewModel::togglePasswordVisible,
            onClickLogin = viewModel::login,
            onClickFindPassword = navigateToFindPassword,
            onClickSignUp = navigateToSignUp,
        )

        UToastHost(
            data = toastData,
            onDismiss = { toastData = null },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 56.dp),
        )
    }
}

@Composable
fun EmailLoginScreen(
    uiState: EmailLoginState = EmailLoginState(),
    onClickBack: () -> Unit = {},
    onEmailChanged: (String) -> Unit = {},
    onPasswordChanged: (String) -> Unit = {},
    onClickPasswordVisible: () -> Unit = {},
    onClickLogin: () -> Unit = {},
    onClickFindPassword: () -> Unit = {},
    onClickSignUp: () -> Unit = {},
) {
    val focusManager = LocalFocusManager.current

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
                // 아이콘을 항상 배치해 필드 높이를 고정하고, 입력값이 없을 땐 투명 처리로 숨김
                nextIcon = painterResource(id = R.drawable.ic_delete_filled),
                nextIconTint = if (uiState.email.isNotEmpty()) grey300() else Color.Transparent,
                onClickNextIcon = if (uiState.email.isNotEmpty()) {
                    { onEmailChanged("") }
                } else {
                    null
                },
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
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { onClickFindPassword() },
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

            Spacer(modifier = Modifier.height(16.dp))

            // 이메일 회원가입 진입 링크
            UText(
                text = AppStrings.SIGN_UP,
                style = UmcTypographyTokens.Footnote,
                color = grey500(),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { onClickSignUp() },
            )

            Spacer(modifier = Modifier.height(24.dp))
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
