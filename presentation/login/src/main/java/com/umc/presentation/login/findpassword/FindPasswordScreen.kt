package com.umc.presentation.login.findpassword

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
fun FindPasswordRoute(
    viewModel: FindPasswordViewModel = hiltViewModel(),
    navigateToBack: () -> Unit = {},
    navigateToLogin: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var toastData by remember { mutableStateOf<UToastData?>(null) }

    CollectUiEvents(viewModel.uiEvent) { event ->
        when (event) {
            is FindPasswordEvent.ShowVerifyToast ->
                toastData = UToastData(AppStrings.SIGN_UP_CODE_SENT_TOAST, UToastState.CHECK)
            is FindPasswordEvent.ShowVerifyCompleteToast ->
                toastData = UToastData(AppStrings.SIGN_UP_EMAIL_VERIFY_COMPLETE, UToastState.CHECK)
            is FindPasswordEvent.ShowErrorToast ->
                toastData = UToastData(event.message, UToastState.ERROR)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isCompleted) {
            FindPasswordCompleteScreen(
                onClickBack = navigateToLogin,
                onClickLogin = navigateToLogin,
            )
        } else {
            FindPasswordScreen(
                uiState = uiState,
                onClickBack = navigateToBack,
                onEmailChanged = viewModel::onEmailChanged,
                onCodeChanged = viewModel::onCodeChanged,
                onClickVerify = viewModel::onClickVerify,
                onClickConfirm = viewModel::onClickConfirm,
                onPasswordChanged = viewModel::onPasswordChanged,
                onPasswordCheckChanged = viewModel::onPasswordCheckChanged,
                onClickComplete = viewModel::onClickComplete,
            )
        }

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
fun FindPasswordScreen(
    uiState: FindPasswordState = FindPasswordState(),
    onClickBack: () -> Unit = {},
    onEmailChanged: (String) -> Unit = {},
    onCodeChanged: (String) -> Unit = {},
    onClickVerify: () -> Unit = {},
    onClickConfirm: () -> Unit = {},
    onPasswordChanged: (String) -> Unit = {},
    onPasswordCheckChanged: (String) -> Unit = {},
    onClickComplete: () -> Unit = {},
) {
    // 인증 완료 후에는 비밀번호 변경 단계로 타이틀/서브타이틀이 전환됨
    val isVerified = uiState.verifyType == EmailVerifyType.VERIFY

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
            text = if (isVerified) AppStrings.CHANGE_PASSWORD_TITLE else AppStrings.EMAIL_LOGIN_FIND_PASSWORD,
            style = UmcTypographyTokens.Title1Bold,
            color = grey950(),
            modifier = Modifier.padding(start = 24.dp, top = 16.dp),
        )

        UText(
            text = if (isVerified) AppStrings.CHANGE_PASSWORD_SUB_TITLE else AppStrings.FIND_PASSWORD_SUB_TITLE,
            style = UmcTypographyTokens.Body,
            color = grey600(),
            modifier = Modifier.padding(start = 24.dp, top = 16.dp),
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
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

            if (isVerified) {
                Spacer(modifier = Modifier.height(24.dp))

                FieldLabel(text = AppStrings.CHANGE_PASSWORD_NEW_LABEL)

                Spacer(modifier = Modifier.height(8.dp))

                UTextField(
                    value = uiState.password,
                    onValueChange = onPasswordChanged,
                    placeholder = AppStrings.SIGN_UP_PASSWORD_PLACEHOLDER,
                    textStyle = UmcTypographyTokens.Body,
                    textColor = grey950(),
                    strokeColor = grey200(),
                    focusStrokeColor = grey900(),
                    cornerRadius = 10.dp,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(24.dp))

                FieldLabel(text = AppStrings.PASSWORD_CHECK)

                Spacer(modifier = Modifier.height(8.dp))

                UTextField(
                    value = uiState.passwordCheck,
                    onValueChange = onPasswordCheckChanged,
                    placeholder = AppStrings.SIGN_UP_PASSWORD_CHECK_PLACEHOLDER,
                    textStyle = UmcTypographyTokens.Body,
                    textColor = grey950(),
                    strokeColor = grey200(),
                    focusStrokeColor = grey900(),
                    cornerRadius = 10.dp,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Column(modifier = Modifier.padding(horizontal = 24.dp)) {
            UButton(
                text = AppStrings.COMPLETE,
                onClick = onClickComplete,
                enabled = uiState.enableCompleteButton,
                backgroundColor = if (uiState.enableCompleteButton) indigo500() else grey100(),
                pressedColor = indigo700(),
                textColor = if (uiState.enableCompleteButton) grey000() else grey300(),
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

/** 비밀번호 변경 완료 화면 */
@Composable
fun FindPasswordCompleteScreen(
    onClickBack: () -> Unit = {},
    onClickLogin: () -> Unit = {},
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

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Icon(
                modifier = Modifier.size(120.dp),
                painter = painterResource(id = R.drawable.ic_success_big_circle),
                contentDescription = null,
            )

            Spacer(modifier = Modifier.height(40.dp))

            UText(
                text = AppStrings.CHANGE_PASSWORD_COMPLETE_TITLE,
                style = UmcTypographyTokens.Title2Bold,
                color = grey950(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            UText(
                text = AppStrings.CHANGE_PASSWORD_COMPLETE_CONTENT,
                style = UmcTypographyTokens.Subheadline,
                color = grey600(),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.weight(1f))

            UButton(
                text = AppStrings.GO_LOGIN,
                onClick = onClickLogin,
                backgroundColor = indigo500(),
                pressedColor = indigo700(),
                textColor = grey000(),
                textStyle = UmcTypographyTokens.HeadlineBold,
                cornerRadius = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

/** 이메일 입력 + 인증 요청 / 인증번호 입력 + 인증 확인 섹션 (비밀번호 찾기용 - 라벨에 * 없음) */
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

    FieldLabel(text = AppStrings.FIND_PASSWORD_EMAIL_LABEL)

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
            textStyle = UmcTypographyTokens.Body,
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
                textStyle = UmcTypographyTokens.Body,
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

/** 비밀번호 찾기 화면 전용 라벨 (필수 표시 * 없음) */
@Composable
private fun FieldLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    UText(
        text = text,
        style = UmcTypographyTokens.HeadlineBold,
        color = grey950(),
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun FindPasswordScreenPreview() {
    FindPasswordScreen()
}

@Preview(showBackground = true)
@Composable
private fun FindPasswordScreenVerifiedPreview() {
    FindPasswordScreen(
        uiState = FindPasswordState(
            email = "example@univ.ac.kr",
            code = "0000",
            verifyType = EmailVerifyType.VERIFY,
            password = "umc12341234",
            passwordCheck = "umc12341234",
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun FindPasswordCompleteScreenPreview() {
    FindPasswordCompleteScreen()
}
