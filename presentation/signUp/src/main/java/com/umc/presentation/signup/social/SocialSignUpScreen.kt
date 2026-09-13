package com.umc.presentation.signup.social

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
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
import com.umc.component.theme.green100
import com.umc.component.theme.green500
import com.umc.component.theme.grey000
import com.umc.component.theme.grey100
import com.umc.component.theme.grey200
import com.umc.component.theme.grey300
import com.umc.component.theme.grey400
import com.umc.component.theme.grey600
import com.umc.component.theme.grey900
import com.umc.component.theme.grey950
import com.umc.component.theme.indigo500
import com.umc.component.theme.indigo700
import com.umc.component.theme.red100
import com.umc.component.theme.red500
import com.umc.domain.model.enums.EmailVerifyType
import kotlinx.coroutines.flow.collectLatest
import com.umc.component.base.CollectUiEvents

@Composable
fun SocialSignUpRoute(
    oAuthVerificationToken: String = "",
    viewModel: SocialSignUpViewModel = hiltViewModel(),
    navigateToBack: () -> Unit = {},
    navigateToNext: (oAuthVerificationToken: String, emailVerificationToken: String) -> Unit = { _, _ -> },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var toastData by remember { mutableStateOf<UToastData?>(null) }

    LaunchedEffect(Unit) {
        viewModel.setOAuthVerificationToken(oAuthVerificationToken)
    }

    CollectUiEvents(viewModel.uiEvent) { event ->
        when (event) {
            is SocialSignUpEvent.MoveToNextEvent ->
                navigateToNext(event.oAuthVerificationToken, event.emailVerificationToken)
            is SocialSignUpEvent.ShowVerifyToast ->
                toastData = UToastData(AppStrings.SIGN_UP_CODE_SENT_TOAST, UToastState.CHECK)
            is SocialSignUpEvent.ShowVerifyCompleteToast ->
                toastData = UToastData(AppStrings.SIGN_UP_EMAIL_VERIFY_COMPLETE, UToastState.CHECK)
            is SocialSignUpEvent.ShowErrorToast ->
                toastData = UToastData(event.message, UToastState.ERROR)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SocialSignUpScreen(
            uiState = uiState,
            onClickBack = navigateToBack,
            onEmailChanged = viewModel::onEmailChanged,
            onCodeChanged = viewModel::onCodeChanged,
            onClickVerify = viewModel::onClickVerify,
            onClickConfirm = viewModel::onClickConfirm,
            onClickNext = viewModel::onClickNext,
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
fun SocialSignUpScreen(
    uiState: SocialSignUpState = SocialSignUpState(),
    onClickBack: () -> Unit = {},
    onEmailChanged: (String) -> Unit = {},
    onCodeChanged: (String) -> Unit = {},
    onClickVerify: () -> Unit = {},
    onClickConfirm: () -> Unit = {},
    onClickNext: () -> Unit = {},
) {
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
            text = AppStrings.SIGN_UP,
            style = UmcTypographyTokens.Title1Bold,
            color = grey950(),
            modifier = Modifier.padding(start = 24.dp, top = 16.dp),
        )

        UText(
            text = AppStrings.SIGN_UP_SOCIAL_SUB_TITLE,
            style = UmcTypographyTokens.Headline,
            color = grey600(),
            modifier = Modifier.padding(start = 24.dp, top = 16.dp),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            EmailVerifySection(
                email = uiState.email,
                code = uiState.code,
                verifyType = uiState.verifyType,
                onEmailChanged = onEmailChanged,
                onCodeChanged = onCodeChanged,
                onClickVerify = onClickVerify,
                onClickConfirm = onClickConfirm,
            )

            Spacer(modifier = Modifier.weight(1f))

            UButton(
                text = AppStrings.NEXT,
                onClick = onClickNext,
                enabled = uiState.enableNextButton,
                backgroundColor = if (uiState.enableNextButton) indigo500() else grey100(),
                pressedColor = indigo700(),
                textColor = if (uiState.enableNextButton) grey000() else grey300(),
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

/** 이메일 입력 + 인증 요청 / 인증번호 입력 + 인증 확인 섹션 */
@Composable
private fun EmailVerifySection(
    email: String,
    code: String,
    verifyType: EmailVerifyType,
    onEmailChanged: (String) -> Unit = {},
    onCodeChanged: (String) -> Unit = {},
    onClickVerify: () -> Unit = {},
    onClickConfirm: () -> Unit = {},
) {
    val isEmailError = verifyType == EmailVerifyType.ERROR
    // 인증 요청 이후에는 이메일·인증번호 입력이 잠김
    val isEmailLocked = verifyType == EmailVerifyType.REQUEST || verifyType == EmailVerifyType.VERIFY
    val isVerified = verifyType == EmailVerifyType.VERIFY

    val codeFocusRequester = remember { FocusRequester() }

    // 인증 요청 성공 시 인증번호 입력 필드로 포커스 이동
    LaunchedEffect(verifyType) {
        if (verifyType == EmailVerifyType.REQUEST) {
            codeFocusRequester.requestFocus()
        }
    }

    FieldLabel(text = AppStrings.EMAIL)

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UTextField(
            value = email,
            onValueChange = onEmailChanged,
            enabled = !isEmailLocked,
            placeholder = AppStrings.SIGN_UP_EMAIL_PLACEHOLDER,
            textStyle = UmcTypographyTokens.Headline,
            textColor = when {
                isEmailError -> red500()
                isEmailLocked -> grey400()
                else -> grey950()
            },
            backgroundColor = when {
                isEmailError -> red100()
                isEmailLocked -> grey100()
                else -> grey000()
            },
            strokeColor = if (isEmailError) red500() else grey200(),
            focusStrokeColor = if (isEmailError) red500() else grey900(),
            cornerRadius = 10.dp,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.weight(1f),
        )

        Spacer(modifier = Modifier.width(16.dp))

        val isVerifyEnabled = verifyType == EmailVerifyType.NONE && email.isNotEmpty()

        UButton(
            text = AppStrings.SIGN_UP_VERIFY_REQUEST,
            onClick = onClickVerify,
            enabled = isVerifyEnabled,
            backgroundColor = if (isVerifyEnabled) indigo500() else grey100(),
            pressedColor = indigo700(),
            textColor = if (isVerifyEnabled) grey000() else grey300(),
            textStyle = UmcTypographyTokens.HeadlineBold,
            cornerRadius = 10.dp,
            contentPadding = PaddingValues(horizontal = 16.dp),
            modifier = Modifier.fillMaxHeight(),
        )
    }

    if (isEmailError) {
        UText(
            text = AppStrings.SIGN_UP_ERROR_EMAIL,
            style = UmcTypographyTokens.Footnote,
            color = red500(),
            modifier = Modifier.padding(top = 4.dp),
        )
    }

    if (verifyType == EmailVerifyType.REQUEST || verifyType == EmailVerifyType.VERIFY) {
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UTextField(
                value = code,
                onValueChange = onCodeChanged,
                enabled = !isVerified,
                placeholder = AppStrings.SIGN_UP_VERIFY_CODE_6_PLACEHOLDER,
                textStyle = UmcTypographyTokens.Headline,
                textColor = if (isVerified) grey400() else grey950(),
                backgroundColor = if (isVerified) grey100() else grey000(),
                strokeColor = grey200(),
                focusStrokeColor = grey900(),
                cornerRadius = 10.dp,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(codeFocusRequester),
            )

            Spacer(modifier = Modifier.width(16.dp))

            UButton(
                text = AppStrings.SIGN_UP_VERIFY_CONFIRM,
                onClick = onClickConfirm,
                enabled = !isVerified,
                backgroundColor = if (isVerified) grey100() else green100(),
                textColor = if (isVerified) grey300() else green500(),
                textStyle = UmcTypographyTokens.HeadlineBold,
                cornerRadius = 10.dp,
                contentPadding = PaddingValues(horizontal = 16.dp),
                modifier = Modifier.fillMaxHeight(),
            )
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
private fun SocialSignUpScreenPreview() {
    SocialSignUpScreen()
}

@Preview(showBackground = true)
@Composable
private fun SocialSignUpScreenRequestPreview() {
    SocialSignUpScreen(
        uiState = SocialSignUpState(
            email = "example@univ.ac.kr",
            verifyType = EmailVerifyType.REQUEST,
        ),
    )
}
